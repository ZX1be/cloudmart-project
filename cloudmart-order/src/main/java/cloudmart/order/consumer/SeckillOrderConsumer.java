package cloudmart.order.consumer;

import cloudmart.order.config.RabbitConfig;
import cloudmart.order.dto.SeckillOrderMessage;
import cloudmart.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeckillOrderConsumer {
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;
    @RabbitListener(queues = RabbitConfig.SECKILL_QUEUE)
    public void onMessage(SeckillOrderMessage message,
                          Channel channel,
                          Message amqpMessage)
        throws Exception{
            long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
            try{
                orderService.createSeckillOrder(message);
                channel.basicAck(deliveryTag,false);
            }catch (Exception e){
                log.error("秒杀订单创建失败: {}",message,e);
                rollbackRedisStock(message);
                channel.basicNack(deliveryTag, false, false);
            }
    }

    private void rollbackRedisStock(SeckillOrderMessage message){
        String stockKey = "seckill:stock:"+message.getActivityId();
        String flagKey = "seckill:order:flag:" + message.getActivityId() + ":" + message.getUserId();
        redisTemplate.opsForValue().increment(stockKey, 1);
        redisTemplate.delete(flagKey);
    }
}
