package cloudmart.product.consumer;

import cloudmart.product.service.ProductService;
import com.rabbitmq.client.Channel;
import common.constant.MqConst;
import common.dto.OrderStockMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStockConsumer {

    private final ProductService productService;

    @RabbitListener(queues = MqConst.ORDER_STOCK_QUEUE)
    public void onMessage(OrderStockMessage message, Channel channel, Message amqpMessage) throws Exception {
        long tag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            productService.deductStockIdempotent(
                    message.getOrderNo(), message.getProductId(), message.getQuantity());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("扣减商品库存失败: {}", message, e);
            channel.basicNack(tag, false, false);
        }
    }
}
