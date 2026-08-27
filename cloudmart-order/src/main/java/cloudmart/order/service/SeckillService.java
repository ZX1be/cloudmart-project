package cloudmart.order.service;

import cloudmart.order.config.RabbitConfig;
import cloudmart.order.dto.SeckillOrderMessage;
import cloudmart.order.entity.SeckillActivity;
import cloudmart.order.mapper.SeckillActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SeckillService extends ServiceImpl<SeckillActivityMapper,SeckillActivity> {
    private final StringRedisTemplate redisTemplate;
    private final OrderService orderService;
    private final DefaultRedisScript<Long> seckillScript;
    private final RabbitTemplate rabbitTemplate;
    private static final String STOCK_KEY = "seckill:stock";
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
        if (activity == null) {
            throw new BizException("秒杀活动不存在");
        }
        redisTemplate.opsForValue().set(
                STOCK_KEY + activityId,
                String.valueOf(activity.getStock()));
    }

    /** 读取 Redis 实时剩余库存 */
    public Integer liveStock(Long activityId) {
        String v = redisTemplate.opsForValue().get(STOCK_KEY + activityId);
        return v == null ? null : Integer.valueOf(v);
    }

    //秒杀下单
    public void seckill(Long userId, Long activityId, Long addressId) {
        SeckillActivity activity = this.getById(activityId);
        if (activity == null || activity.getStatus() == 0) {
            throw new BizException("秒杀活动不存在");
        }
        String flagKey = ORDER_FLAG_KEY + activityId + ":" + userId;
        Boolean flag = redisTemplate.opsForValue()
                .setIfAbsent(flagKey, "1", Duration.ofMinutes(30));
        if (Boolean.FALSE.equals(flag)) {
            throw new BizException("您已参与过本次秒杀");
        }
        String stockKey = STOCK_KEY + activityId;
        Long result = redisTemplate.execute(
                seckillScript,
                Collections.singletonList(stockKey),
                "1");
        if (result == null || result != 1L) {
            redisTemplate.delete(flagKey);
            throw new BizException("秒杀商品已售罄");
        }
        SeckillOrderMessage message = SeckillOrderMessage.builder()
                .userId(userId)
                .activityId(activityId)
                .addressId(addressId)
                .orderNo(generateSeckillOrderNo(userId, activityId))
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.SECKILL_EXCHANGE,
                    RabbitConfig.SECKILL_ROUTING_KEY,
                    message);
        } catch (Exception e) {
            log.error("秒杀消息发送失败", e);
            redisTemplate.opsForValue().increment(stockKey, 1);
            redisTemplate.delete(flagKey);
            throw new BizException("秒杀失败，请重试");
        }
    }
    private String generateSeckillOrderNo(Long userId, Long activityId) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SECKILL" + timestamp + userId + activityId
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

}
