package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cart_item")
public class CartItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer selected; //1为选中，0为未选
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createdAt;


}
