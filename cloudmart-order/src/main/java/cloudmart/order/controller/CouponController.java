package cloudmart.order.controller;

import cloudmart.order.entity.Coupon;
import cloudmart.order.entity.UserCoupon;
import cloudmart.order.service.CouponService;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    @GetMapping
    public Result<List<Coupon>> list() {
        return Result.success(couponService.listAvailable());
    }

    @PostMapping("/{id}/claim")
    public Result<Void> claim(@RequestHeader("X-User-Id") Long userId,
                              @PathVariable Long id) {
        couponService.claim(userId, id);
        return Result.success();
    }

    @GetMapping("/mine")
    public Result<List<UserCoupon>> mine(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(couponService.listMine(userId));
    }
}
