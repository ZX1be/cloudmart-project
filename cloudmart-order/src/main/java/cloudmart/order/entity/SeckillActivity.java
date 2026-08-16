package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private BigDecimal seckillPrice; // 秒杀价
    private Integer stock;           // 秒杀库存
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}