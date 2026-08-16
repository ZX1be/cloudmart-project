package cloudmart.product.vo;

import cloudmart.product.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends Product {
    private String categoryName;
}