package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")   // MySQL 保留字，必须加反引号
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long addressId;
    private String addressSnapshot;   // 收货地址快照（JSON）
    private BigDecimal totalAmount;   // 商品总价
    private BigDecimal discountAmount;// 优惠金额
    private BigDecimal payAmount;     // 实付金额
    private String status;            // PENDING/PAID/SHIPPED/RECEIVED/COMPLETED/CANCELLED
    private Long couponId;            // 使用的优惠券ID
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
