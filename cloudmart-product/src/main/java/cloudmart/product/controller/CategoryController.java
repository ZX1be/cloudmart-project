package cloudmart.product.controller;

import cloudmart.product.entity.Category;
import cloudmart.product.service.CategoryService;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
