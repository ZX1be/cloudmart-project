package cloudmart.order.service;

import cloudmart.order.entity.*;
import cloudmart.order.mapper.CouponMapper;
import cloudmart.order.mapper.OrderItemMapper;
import cloudmart.order.mapper.OrderMapper;
import cloudmart.order.mapper.UserCouponMapper;
import cloudmart.product.entity.Product;
import cloudmart.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId,Long addressId,Long userCouponId){
        // 1. 获取购物车中选中的商品
        List<CartItem> cartItems=cartItemService.list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId,userId)
                .eq(CartItem::getSelected,1));
        if(cartItems.isEmpty()){
            throw new BizException("请选择你要购买的商品");
        }
        //2.计算总价+锁库存
        BigDecimal totalAmount=BigDecimal.ZERO;
        for(CartItem item:cartItems){
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BizException("商品【" + product.getName() + "】已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BizException("商品【" + product.getName() + "】库存不足");
            }
            // !!! 扣库存
            product.setStock(product.getStock()-item.getQuantity());
            product.setSales(product.getSales()+item.getQuantity());
            productMapper.updateById(product);
            totalAmount = totalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }
        //3.处理优惠券
        BigDecimal discountAmount=BigDecimal.ZERO;
        Long couponId=null;
        if(userCouponId!=null){
            discountAmount = applyCoupon(userId,userCouponId,totalAmount);
            if(userCouponId!=null){
                couponId=userCouponMapper.selectById(userCouponId).getCouponId();
            }
        }

        //4.创建订单
        Order order=new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount));
        order.setStatus("PENDING");
        order.setCouponId(couponId);
        this.save(order);

        //5.创建订单明细
        for (CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImages());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setAmount(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(orderItem);
        }

        // 6. 清空购物车中已下单的商品
        cartItemService.remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSelected, 1));

        return order;
    }
    /** 生成订单号 */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int)(Math.random() * 1000000));
        return timestamp + random;
    }
    /** 应用优惠券 */
    private BigDecimal applyCoupon(Long userId, Long userCouponId, BigDecimal totalAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new BizException("优惠券不存在");
        }
        if (!"UNUSED".equals(userCoupon.getStatus())) {
            throw new BizException("优惠券已使用或已过期");
        }
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BizException("未达到优惠券最低消费金额");
        }
        // 标记优惠券已使用
        userCoupon.setStatus("USED");
        userCoupon.setUsedAt(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);

        if ("FIXED".equals(coupon.getType())) {
            return coupon.getDiscountValue();
        } else {
            // 折扣：discountValue 是百分比，比如 80 表示 8 折
            BigDecimal discount = BigDecimal.ONE.subtract(
                    coupon.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            );
            return totalAmount.multiply(discount).setScale(2, RoundingMode.HALF_UP);
        }
    }
    //支付
    public void pay(Long userId,Long orderId){
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BizException("订单状态不允许支付");
        }
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
    }
    // 取消
    public void cancel(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BizException("只有待支付订单可以取消");
        }

        //！！！恢复库存
        List<OrderItem> items=orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId,orderId));
        for (OrderItem item:items){
            Product product = productMapper.selectById(item.getProductId());
            product.setStock(product.getStock() + item.getQuantity());
            productMapper.updateById(product);
        }
        order.setStatus("CANCELLED");
        this.updateById(order);

        //!!!退还优惠券
        if(order.getCouponId()!=null) {
            UserCoupon userCoupon = userCouponMapper.selectOne(
                    new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getCouponId, order.getCouponId())
                            .eq(UserCoupon::getUserId, userId)
                            .eq(UserCoupon::getStatus, "USED"));

            if (userCoupon != null) {
                userCoupon.setStatus("UNUSED");
                userCoupon.setUsedAt(null);
                userCouponMapper.updateById(userCoupon);
            }
        }
    }
    //发货
    public void ship(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !"PAID".equals(order.getStatus())) {
            throw new BizException("订单状态不允许发货");
        }
        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }
    // 确认收货
    public void receive(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BizException("订单状态不允许确认收货");
        }
        order.setStatus("RECEIVED");
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);
    }
}
