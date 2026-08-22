package cloudmart.order.controller;

import cloudmart.order.dto.CreateOrderDTO;
import cloudmart.order.entity.Order;
import cloudmart.order.service.OrderService;
import cloudmart.order.vo.OrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.exception.BizException;
import common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    public Result<Order> create(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(userId, dto));
    }
    @GetMapping
    public Result<IPage<Order>> list(@RequestHeader("X-User-Id") Long userId,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.pageByUserId(userId, page, size));
    }
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long id) {
        return Result.success(orderService.detail(userId, id));
    }
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@RequestHeader("X-User-Id") Long userId,
                            @PathVariable Long id) {
        orderService.pay(userId, id);
        return Result.success();
    }
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long id) {
        orderService.cancel(userId, id);
        return Result.success();
    }
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@RequestHeader("X-User-Id") Long userId,
                                @PathVariable Long id) {
        orderService.receive(userId, id);
        return Result.success();
    }
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@RequestHeader("X-Role") String role,
                             @PathVariable Long id) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
        orderService.ship(id);
        return Result.success();
    }
}
