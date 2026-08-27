package cloudmart.product.controller;

import cloudmart.product.entity.Category;
import cloudmart.product.service.CategoryService;
import common.exception.BizException;
import common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final RedisTemplate redisTemplate;

    @GetMapping
    public Result<List<Category>> list(@RequestHeader("X-Role") String role) {
        checkAdmin(role);
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<Void> create(@RequestHeader("X-Role") String role,
                               @Valid @RequestBody Category category) {
        checkAdmin(role);
        categoryService.save(category);
        redisTemplate.delete("category:tree");
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("X-Role") String role,
                               @PathVariable Long id,
                               @RequestBody Category category) {
        checkAdmin(role);
        category.setId(id);
        categoryService.updateById(category);
        redisTemplate.delete("category:tree");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("X-Role") String role,
                               @PathVariable Long id) {
        checkAdmin(role);
        categoryService.removeById(id);
        redisTemplate.delete("category:tree");
        return Result.success();
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
    }
}
