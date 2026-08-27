package cloudmart.order.controller;

import cloudmart.order.entity.SeckillActivity;
import cloudmart.order.service.SeckillService;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @GetMapping("/activities")
    public Result<List<SeckillActivity>> activities() {
        return Result.success(seckillService.listAvailable());
    }

    @PostMapping("/{activityId}/buy")
    public Result<String> buy(@RequestHeader("X-User-Id") Long userId,
                              @PathVariable Long activityId,
                              @RequestParam Long addressId) {
        seckillService.seckill(userId, activityId, addressId);
        return Result.success("排队成功，请稍后查询订单");
    }
}
