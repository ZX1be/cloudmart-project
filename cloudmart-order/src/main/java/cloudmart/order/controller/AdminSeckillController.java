package cloudmart.order.controller;

import cloudmart.order.entity.SeckillActivity;
import cloudmart.order.service.SeckillService;
import cloudmart.order.vo.SeckillActivityVO;
import common.exception.BizException;
import common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/seckill")
@RequiredArgsConstructor
public class AdminSeckillController {

    private final SeckillService seckillService;

    @GetMapping
    public Result<List<SeckillActivityVO>> list(@RequestHeader("X-Role") String role) {
        checkAdmin(role);
        List<SeckillActivityVO> vos = seckillService.list().stream().map(activity -> {
            SeckillActivityVO vo = new SeckillActivityVO();
            BeanUtils.copyProperties(activity, vo);
            vo.setRealStock(seckillService.liveStock(activity.getId()));
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    @PostMapping
    public Result<Void> create(@RequestHeader("X-Role") String role,
                               @RequestBody SeckillActivity activity) {
        checkAdmin(role);
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        seckillService.save(activity);
        return Result.success();
    }

    @PostMapping("/{id}/warmup")
    public Result<Void> warmup(@RequestHeader("X-Role") String role,
                               @PathVariable Long id) {
        checkAdmin(role);
        seckillService.warmUp(id);
        return Result.success();
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BizException(403, "无权限");
        }
    }
}
