package cloudmart.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductQueryDTO {
    private Long categoryId;    // 分类筛选
    private String keyword;      // 关键词搜索
    private BigDecimal minPrice; // 最低价
    private BigDecimal maxPrice; // 最高价
    private Integer status;      // 上架/下架
    private String sortField;    // 排序字段：price/sales/created_at
    private String sortOrder;    // asc/desc
    private Integer page = 1;
    private Integer pageSize = 20;
}