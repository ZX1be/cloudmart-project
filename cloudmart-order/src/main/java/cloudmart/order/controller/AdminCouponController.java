package cloudmart.order.controller;

import cloudmart.order.entity.Coupon;
import cloudmart.order.service.CouponService;
import common.exception.BizException;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public Result<List<Coupon>> list(@RequestHeader("X-Role") String role) {
        checkAdmin(role);
        return Result.success(couponService.list());
    }

    @PostMapping
    public Result<Void> create(@RequestHeader("X-Role") String role,
                               @RequestBody Coupon coupon) {
        checkAdmin(role);
        couponService.save(coupon);
        return Result.success();
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
    }
}
