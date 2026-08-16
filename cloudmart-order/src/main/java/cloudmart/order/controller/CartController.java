package cloudmart.order.controller;

import cloudmart.order.entity.CartItem;
import cloudmart.order.service.CartItemService;
import common.exception.BizException;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartItemService cartItemService;
    @GetMapping
    public Result<List<CartItem>> list(@RequestHeader("X-User-Id") Long userId){
        return Result.success(cartItemService.listByUserId(userId));
    }
    @PostMapping
    public Result<Void> add(@RequestHeader("X-User-Id") Long userId,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity){
        cartItemService.add(userId,productId,quantity);
        return Result.success();
    }
    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable Long id,
                                       @RequestParam Integer quantity) {
        CartItem item = cartItemService.getById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException("购物车项不存在");
        }
        item.setQuantity(quantity);
        cartItemService.updateById(item);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long id) {
        CartItem item = cartItemService.getById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException("购物车项不存在");
        }
        cartItemService.removeById(id);
        return Result.success();
    }
}

