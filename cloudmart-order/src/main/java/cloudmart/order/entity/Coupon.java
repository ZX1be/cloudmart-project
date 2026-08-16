package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;            // 优惠券名称
    private String type;            // FIXED=满减, PERCENT=折扣
    private BigDecimal discountValue; // 优惠值（满减=金额，折扣=百分比）
    private BigDecimal minAmount;    // 最低消费金额
    private Integer totalCount;     // 总发行量
    private Integer usedCount;      // 已领取/已使用数
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}