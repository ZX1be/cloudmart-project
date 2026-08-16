package cloudmart.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
