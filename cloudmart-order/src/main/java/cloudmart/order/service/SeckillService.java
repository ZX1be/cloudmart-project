package cloudmart.order.service;

import cloudmart.order.entity.SeckillActivity;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillService {
    private final StringRedisTemplate redisTemplate;
    private static final String STOCK_KEY_PREFIX="seckill:stock";
    /**
     * 活动预热（管理员操作，或定时任务）
     */
    public void warmUp(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        redisTemplate.opsForValue().set(
                STOCK_KEY_PREFIX + activityId,
                String.valueOf(activity.getStock())
        );

}
