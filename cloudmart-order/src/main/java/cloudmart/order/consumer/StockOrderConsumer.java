package cloudmart.order.consumer;

import cloudmart.order.service.OrderService;
import com.rabbitmq.client.Channel;
import common.constant.MqConst;
import common.dto.StockDeductResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockOrderConsumer {
    private final OrderService orderService;

    @RabbitListener(queues = MqConst.STOCK_RESULT_QUEUE)
    public void onMessage(StockDeductResultMessage message,
                          Channel channel,
                          org.springframework.amqp.core.Message amqpMessage)
            throws Exception {
        long tag = amqpMessage.getMessageProperties().getDeliveryTag();

        try {
            orderService.markStockDeducted(
                    message.getOrderNo(),
                    message.getProductId()
            );
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("处理库存结果失败: {}", message, e);
            channel.basicNack(tag, false, false);
        }
    }
}
