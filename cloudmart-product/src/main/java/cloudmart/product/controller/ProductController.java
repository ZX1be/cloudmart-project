package cloudmart.product.controller;

import cloudmart.product.dto.ProductQueryDTO;
import cloudmart.product.entity.Product;
import cloudmart.product.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.exception.BizException;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public Result<IPage<Product>> list(ProductQueryDTO dto) {
        dto.setStatus(1);  // 只查上架的
        return Result.success(productService.queryPage(dto));
    }

    // 用户端：商品详情
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

}
