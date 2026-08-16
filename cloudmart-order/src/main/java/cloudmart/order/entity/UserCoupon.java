package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private String status;          // UNUSED/USED/EXPIRED
    private LocalDateTime usedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}