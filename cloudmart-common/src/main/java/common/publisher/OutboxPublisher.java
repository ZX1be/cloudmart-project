package common.publisher;

import common.entity.OutboxMessage;
import common.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${outbox.message-types:STOCK_DEDUCT,SECKILL_ORDER}")
    private List<String> messageTypes;

    @Scheduled(fixedDelayString = "${outbox.publish-interval:1000}")
    @Transactional(rollbackFor = Exception.class)
    public void publishPendingMessages() {
        if (messageTypes == null || messageTypes.isEmpty()) {
            return;
        }

        List<OutboxMessage> messages = outboxMapper.selectPendingBatch(messageTypes, 100);

        for (OutboxMessage message : messages) {
            try {
                MessageProperties properties = new MessageProperties();
                properties.setContentType("application/json");
                properties.setContentEncoding("UTF-8");
                properties.setMessageId(message.getMessageKey());

                Message amqpMessage = MessageBuilder
                        .withBody(message.getPayload().getBytes(StandardCharsets.UTF_8))
                        .andProperties(properties)
                        .build();

                CorrelationData correlationData =
                        new CorrelationData(message.getMessageKey());

                rabbitTemplate.send(
                        message.getTopic(),
                        message.getRoutingKey(),
                        amqpMessage,
                        correlationData
                );

                CorrelationData.Confirm confirm =
                        correlationData.getFuture().get(5, TimeUnit.SECONDS);

                if (confirm.isAck() && correlationData.getReturned() == null) {
                    outboxMapper.markSent(message.getId());
                } else {
                    outboxMapper.markRetry(message.getId());
                }
            } catch (Exception e) {
                log.error("Outbox发送失败: {}", message.getId(), e);
                outboxMapper.markRetry(message.getId());
            }
        }
    }
}
