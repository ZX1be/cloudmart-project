package cloudmart.order.service;

import cloudmart.order.entity.OrderItem;
import cloudmart.order.entity.SeckillActivity;
import cloudmart.order.mapper.SeckillActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SeckillService extends ServiceImpl<SeckillActivityMapper,SeckillActivity> {
    private final StringRedisTemplate redisTemplate;
    private final OrderService orderService;
    private final DefaultRedisScript<Long> seckillScript;
    private static final String STOCK_KEY_PREFIX="seckill:stock";
    private static final String ORDER_FLAG_KEY = "seckill:order:flag:";

    public List<SeckillActivity> listAvailable() {
        return this.list(new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getStatus, 1)
                .le(SeckillActivity::getStartTime, LocalDateTime.now())
                .ge(SeckillActivity::getEndTime, LocalDateTime.now()));
    }

    //活动预热

    public void warmUp(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        redisTemplate.opsForValue().set(
                STOCK_KEY_PREFIX + activityId,
                String.valueOf(activity.getStock())
        );
    }

    //秒杀下单
    public Long seckill(Long userId,Long activityId,Long addressId) {
        // 1. 防重复下单
        String flagKey = ORDER_FLAG_KEY + activityId + ":" + userId;
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(flagKey, "1", Duration.ofMinutes(30));
        if (Boolean.FALSE.equals(flag)) {
            throw new BizException("您已参与过本次秒杀");
        }
        // 2. Redis 原子扣库存（Lua 脚本
        String  stockKey=STOCK_KEY_PREFIX + activityId;
        Long result=redisTemplate.execute(seckillScript, Collections.singletonList(stockKey),"1");
        if(result==null||result!=1L){
            redisTemplate.delete(flagKey);  // 恢复标记
            throw new BizException("秒杀商品已售罄");
        }
        // 3. 异步创建订单
        try {
            SeckillActivity activity = this.getById(activityId);
            return orderService.createSeckillOrder(userId, addressId, activity);
        } catch (Exception e) {
            // 下单失败，恢复 Redis 库存
            redisTemplate.opsForValue().increment(stockKey, 1);
            redisTemplate.delete(flagKey);
            throw new BizException("秒杀失败");
        }
    }

    }
