package cloudmart.product.controller;

import cloudmart.product.entity.Category;
import cloudmart.product.service.CategoryService;
import common.exception.BizException;
import common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<Void> create(@RequestHeader("X-Role") String role,
                               @Valid @RequestBody Category category) {
        checkAdmin(role);
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("X-Role") String role,
                               @PathVariable Long id,
                               @RequestBody Category category) {
        checkAdmin(role);
        category.setId(id);
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("X-Role") String role,
                               @PathVariable Long id) {
        checkAdmin(role);
        categoryService.removeById(id);
        return Result.success();
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
    }
}