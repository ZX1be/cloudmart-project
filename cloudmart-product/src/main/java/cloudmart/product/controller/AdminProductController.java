package cloudmart.product.controller;

import cloudmart.product.dto.ProductQueryDTO;
import cloudmart.product.entity.Product;
import cloudmart.product.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.exception.BizException;
import common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public Result<IPage<Product>> list(@RequestHeader("X-Role") String role,
                                       ProductQueryDTO dto) {
        checkAdmin(role);
        return Result.success(productService.queryPage(dto));
    }

    @PostMapping
    public Result<Void> create(@RequestHeader("X-Role") String role,
                               @Valid @RequestBody Product product) {
        checkAdmin(role);
        productService.save(product);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("X-Role") String role,
                               @PathVariable Long id,
                               @Valid @RequestBody Product product) {
        checkAdmin(role);
        product.setId(id);
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("X-Role") String role,
                               @PathVariable Long id) {
        checkAdmin(role);
        productService.removeById(id);  // 逻辑删除
        return Result.success();
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
    }
}
