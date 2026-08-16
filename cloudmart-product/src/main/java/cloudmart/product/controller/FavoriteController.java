package cloudmart.product.controller;

import cloudmart.product.entity.Favorite;
import cloudmart.product.service.FavoriteService;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public Result<Void> add(@RequestHeader("X-User-Id") Long userId,
                            @PathVariable Long productId) {
        favoriteService.add(userId, productId);
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> remove(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long productId) {
        favoriteService.remove(userId, productId);
        return Result.success();
    }

    @GetMapping
    public Result<List<Favorite>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(favoriteService.listByUserId(userId));
    }
}
