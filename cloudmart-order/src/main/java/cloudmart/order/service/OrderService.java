package cloudmart.order.service;

import cloudmart.order.client.ProductClient;
import cloudmart.order.dto.CreateOrderDTO;
import cloudmart.order.dto.SeckillOrderMessage;
import cloudmart.order.entity.*;
import cloudmart.order.mapper.*;
import cloudmart.order.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.constant.MqConst;
import common.dto.OrderStockMessage;
import common.dto.ProductDTO;
import common.exception.BizException;
import common.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final CartItemService cartItemService;
    private final OrderItemMapper orderItemMapper;
    private final ProductClient productClient;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final SeckillActivityMapper seckillActivityMapper;
    private final RabbitTemplate rabbitTemplate;
    private final OrderIdempotentRecordMapper orderIdempotentRecordMapper;
    private final OutboxMapper outboxMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId,String idempotencyKey,CreateOrderDTO dto) {
        validateIdempotencyKey(idempotencyKey);
        String requestHash = buildRequestHash(dto);
        OrderIdempotentRecord record = new OrderIdempotentRecord();
        record.setUserId(userId);
        record.setIdempotentKey(idempotencyKey);
        record.setRequestHash(requestHash);


        int inserted = orderIdempotentRecordMapper.insertIgnore(record);
        if (inserted == 0) {
            OrderIdempotentRecord existing =
                    orderIdempotentRecordMapper.selectForUpdate(
                            userId, idempotencyKey);

            if (existing == null || existing.getOrderId() == null) {
                throw new BizException(500, "订单幂等记录状态异常");
            }

            if (!requestHash.equals(existing.getRequestHash())) {
                throw new BizException(
                        409,
                        "Idempotency-Key 已用于不同的订单请求"
                );
            }
            Order existingOrder = this.getById(existing.getOrderId());

            if (existingOrder == null) {
                throw new BizException(500, "幂等记录对应订单不存在");
            }

            return existingOrder;
        }


        List<CartItem> cartItems = cartItemService.list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSelected, 1));
        validateCartFingerprint(dto.getCartFingerprint(), cartItems);
        if (cartItems.isEmpty()) {
            throw new BizException("请选择要购买的商品");
        }

        // 通过 Feign 只读取商品信息（含价格），用于计算金额与快照
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, ProductDTO> productMap = new HashMap<>();
        for (CartItem item : cartItems) {
            ProductDTO product = productClient.getById(item.getProductId()).getData();
            if (product == null || product.getStatus() == 0) {
                throw new BizException("商品已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BizException("商品库存不足");
            }
            productMap.put(product.getId(), product);
            totalAmount = totalAmount.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        Long couponId = null;
        if (dto.getUserCouponId() != null) {
            discountAmount = applyCoupon(userId, dto.getUserCouponId(), totalAmount);
            couponId = userCouponMapper.selectById(dto.getUserCouponId()).getCouponId();
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(dto.getAddressId());
        order.setAddressSnapshot(dto.getAddressSnapshot());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount));
        order.setStatus("PENDING");
        order.setCouponId(couponId);
        this.save(order);
        int bound = orderIdempotentRecordMapper.bindOrderId(
                record.getId(), order.getId());

        if (bound != 1) {
            throw new BizException(500, "订单幂等记录回填失败");
        }
        for (CartItem item : cartItems) {
            ProductDTO product = productMap.get(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImages());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setAmount(product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(orderItem);
        }

        cartItemService.remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSelected, 1));

        for (CartItem item : cartItems) {
            OrderStockMessage event= OrderStockMessage.builder()
                    .orderNo(order.getOrderNo())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build();
            saveOrderOutbox(event, "STOCK_DEDUCT");
        }
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillOrder(SeckillOrderMessage message) {
        Order exist = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, message.getOrderNo()));
        if (exist != null) {
            return exist.getId();
        }

        SeckillActivity activity = seckillActivityMapper.selectById(message.getActivityId());
        if (activity == null || activity.getStatus() == 0) {
            throw new BizException("秒杀活动不存在");
        }

        ProductDTO product = productClient.getById(activity.getProductId()).getData();
        if (product == null || product.getStatus() == 0) {
            throw new BizException("商品不存在或已下架");
        }

        Order order = new Order();
        order.setOrderNo(message.getOrderNo());
        order.setUserId(message.getUserId());
        order.setAddressId(message.getAddressId());
        order.setAddressSnapshot("{}");
        order.setTotalAmount(activity.getSeckillPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(activity.getSeckillPrice());
        order.setStatus("PENDING");
        order.setStockStatus(0);
        this.save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImages());
        item.setPrice(activity.getSeckillPrice());
        item.setQuantity(1);
        item.setAmount(activity.getSeckillPrice());
        item.setStockStatus(0);
        orderItemMapper.insert(item);

        OrderStockMessage event = OrderStockMessage.builder()
                .orderNo(order.getOrderNo())
                .productId(product.getId())
                .quantity(1)
                .build();
        saveOrderOutbox(event, "STOCK_DEDUCT");

        return order.getId();
    }

    public void pay(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BizException("订单状态不允许支付");
        }
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
    }

    public void cancel(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BizException("只有待支付订单可以取消");
        }
        restoreStock(orderId);
        restoreCoupon(userId, order.getCouponId());
        order.setStatus("CANCELLED");
        this.updateById(order);
    }

    public void ship(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !"PAID".equals(order.getStatus())) {
            throw new BizException("订单状态不允许发货");
        }
        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }

    public void receive(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BizException("订单状态不允许确认收货");
        }
        order.setStatus("RECEIVED");
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);
    }

    public IPage<Order> pageByUserId(Long userId, Integer page, Integer size) {
        return this.page(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt));
    }

    public IPage<Order> pageAllOrders(Integer page, Integer size) {
        return this.page(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .orderByDesc(Order::getCreatedAt));
    }

    public OrderVO detail(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        OrderVO vo = new OrderVO();
        org.springframework.beans.BeanUtils.copyProperties(order, vo);
        vo.setItems(orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)));
        return vo;
    }

    private Order getOwnedOrder(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            productClient.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    private void restoreCoupon(Long userId, Long couponId) {
        if (couponId == null) {
            return;
        }
        UserCoupon userCoupon = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
                        .eq(UserCoupon::getStatus, "USED"));
        if (userCoupon != null) {
            userCoupon.setStatus("UNUSED");
            userCoupon.setUsedAt(null);
            userCouponMapper.updateById(userCoupon);
        }
    }

    private BigDecimal applyCoupon(Long userId, Long userCouponId, BigDecimal totalAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new BizException("优惠券不存在");
        }
        if (!"UNUSED".equals(userCoupon.getStatus())) {
            throw new BizException("优惠券已使用或已过期");
        }
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null || totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BizException("未达到优惠券最低消费金额");
        }
        userCoupon.setStatus("USED");
        userCoupon.setUsedAt(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);

        if ("FIXED".equals(coupon.getType())) {
            return coupon.getDiscountValue();
        }
        BigDecimal discount = BigDecimal.ONE.subtract(
                coupon.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        return totalAmount.multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return timestamp + random;
    }
    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 64) {
            throw new BizException(400, "Idempotency-Key 无效");
        }
    }
    private String buildRequestHash(CreateOrderDTO dto) {
        String canonical = String.join(
                "\n",
                String.valueOf(dto.getAddressId()),
                Objects.toString(dto.getUserCouponId(), "null"),
                Objects.toString(dto.getAddressSnapshot(), ""),
                dto.getCartFingerprint()
        );

        return sha256(canonical);
    }

    private String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
    private void validateCartFingerprint(String clientFingerprint,
                                         List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            throw new BizException("请选择要购买的商品");
        }

        String actual = cartItems.stream()
                .sorted(Comparator.comparing(CartItem::getProductId))
                .map(item -> item.getProductId() + ":" + item.getQuantity())
                .collect(Collectors.joining("\n"));

        String actualFingerprint = sha256(actual);

        if (!actualFingerprint.equals(clientFingerprint)) {
            throw new BizException(
                    409,
                    "购物车内容已经变化，请重新确认订单"
            );
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void markStockDeducted(String orderNo, Long productId) {
        Order order= baseMapper.selectByOrderNoForUpdate(orderNo);
        if (order == null) {
            throw new BizException("订单不存在: " + orderNo);
        }

        orderItemMapper.update(
                null,
                Wrappers.<OrderItem>lambdaUpdate()
                        .eq(OrderItem::getOrderId, order.getId())
                        .eq(OrderItem::getProductId, productId)
                        .set(OrderItem::getStockStatus, 1)
        );

        Long unfinishedCount = orderItemMapper.selectCount(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getOrderId, order.getId())
                        .ne(OrderItem::getStockStatus, 1)
        );

        if (unfinishedCount == 0) {
            this.lambdaUpdate()
                    .eq(Order::getOrderNo, orderNo)
                    .set(Order::getStockStatus, 1)
                    .update();
        }
    }

    private void saveOrderOutbox(OrderStockMessage event, String messageType){
        String messageKey="STOCK_DEDUCT:"+event.getOrderNo()+":"+event.getProductId();
        int inserted= outboxMapper.insertLocal(messageKey,messageType,MqConst.ORDER_STOCK_EXCHANGE,MqConst.ORDER_STOCK_ROUTING,writeJson(event));
        if (inserted == 0) {
            log.info("订单Outbox已存在: {}", messageKey);
        }
    }

    private String writeJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new BizException(500, "库存结果消息序列化失败");
        }
    }




}
