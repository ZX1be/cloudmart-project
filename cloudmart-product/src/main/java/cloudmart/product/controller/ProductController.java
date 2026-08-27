package cloudmart.product.controller;

import cloudmart.product.dto.ProductQueryDTO;
import cloudmart.product.entity.Product;
import cloudmart.product.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.exception.BizException;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/deduct")
    public Result<Void> deductStock(@PathVariable Long id,
            @RequestParam Integer quantity){
        productService.deductStock(id, quantity);
        return Result.success();
    }

    @PostMapping("/{id}/restore")
    public Result<Void> restoreStock(@PathVariable Long id,
            @RequestParam Integer quantity){
        productService.restoreStock(id, quantity);
        return Result.success();
    }
}
