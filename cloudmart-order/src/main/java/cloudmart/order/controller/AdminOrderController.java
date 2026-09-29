package cloudmart.order.controller;

import cloudmart.order.entity.Order;
import cloudmart.order.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.exception.BizException;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<IPage<Order>> list(@RequestHeader("X-Role") String role,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        checkAdmin(role);
        return Result.success(orderService.pageAllOrders(page, size));
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
    }
}
