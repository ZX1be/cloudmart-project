package cloudmart.product.vo;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductListVO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String images;
    private Integer stock;
    private Integer sales;
}