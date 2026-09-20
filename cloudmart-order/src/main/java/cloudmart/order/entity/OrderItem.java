package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;     // 商品名快照
    private String productImage;    // 商品图片快照
    private BigDecimal price;       // 下单时单价
    private Integer quantity;
    private BigDecimal amount;  // 小计
    private Integer stockStatus; //0待处理 1已扣减 2扣减失败
}
