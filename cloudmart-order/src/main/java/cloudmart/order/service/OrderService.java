package cloudmart.order.service;

import cloudmart.order.dto.CreateOrderDTO;
import cloudmart.order.entity.*;
import cloudmart.order.mapper.CouponMapper;
import cloudmart.order.mapper.OrderItemMapper;
import cloudmart.order.mapper.OrderMapper;
import cloudmart.order.mapper.UserCouponMapper;
import cloudmart.order.vo.OrderVO;
import cloudmart.product.entity.Product;
import cloudmart.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final CartItemService cartItemService;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, CreateOrderDTO dto) {
        List<CartItem> cartItems = cartItemService.list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSelected, 1));
        if (cartItems.isEmpty()) {
            throw new BizException("请选择要购买的商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BizException("商品已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BizException("商品库存不足");
            }
            product.setStock(product.getStock() - item.getQuantity());
            product.setSales(product.getSales() + item.getQuantity());
            productMapper.updateById(product);
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

        for (CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
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
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillOrder(Long userId, Long addressId, SeckillActivity activity) {
        Product product = productMapper.selectById(activity.getProductId());
        if (product == null || product.getStatus() == 0 || activity.getStatus() == 0) {
            throw new BizException("秒杀活动或商品不可用");
        }
        if (product.getStock() < 1) {
            throw new BizException("商品库存不足");
        }
        product.setStock(product.getStock() - 1);
        product.setSales(product.getSales() + 1);
        productMapper.updateById(product);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setAddressSnapshot("{}");
        order.setTotalAmount(activity.getSeckillPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(activity.getSeckillPrice());
        order.setStatus("PENDING");
        this.save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImages());
        item.setPrice(activity.getSeckillPrice());
        item.setQuantity(1);
        item.setAmount(activity.getSeckillPrice());
        orderItemMapper.insert(item);
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
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
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
}