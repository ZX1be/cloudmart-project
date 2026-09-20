package cloudmart.order.config;

import common.constant.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String SECKILL_QUEUE = "seckill.order.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_ROUTING_KEY = "seckill.order";
    public static final String ORDER_STOCK_QUEUE = MqConst.ORDER_STOCK_QUEUE;
    public static final String ORDER_STOCK_EXCHANGE = MqConst.ORDER_STOCK_EXCHANGE;
    public static final String ORDER_STOCK_ROUTING = MqConst.ORDER_STOCK_ROUTING;

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(SECKILL_QUEUE)
                .deadLetterExchange(MqConst.SECKILL_DLX)
                .deadLetterRoutingKey(MqConst.SECKILL_DLQ_ROUTING)
                .build();
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder
                .bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);
    }

    @Bean
    public Queue seckillDlq() {
        return QueueBuilder.durable(MqConst.SECKILL_DLQ).build();
    }

    @Bean
    public DirectExchange seckillDlx() {
        return new DirectExchange(MqConst.SECKILL_DLX);
    }

    @Bean
    public Binding seckillDlqBinding() {
        return BindingBuilder.bind(seckillDlq())
                .to(seckillDlx())
                .with(MqConst.SECKILL_DLQ_ROUTING);
    }

    @Bean
    public Queue orderStockQueue() {
        return QueueBuilder.durable(ORDER_STOCK_QUEUE)
                .deadLetterExchange(MqConst.ORDER_STOCK_DLX)
                .deadLetterRoutingKey(MqConst.ORDER_STOCK_DLQ_ROUTING)
                .build();
    }

    @Bean
    public DirectExchange orderStockExchange() {
        return new DirectExchange(ORDER_STOCK_EXCHANGE);
    }

    @Bean
    public Binding orderStockBinding() {
        return BindingBuilder.bind(orderStockQueue())
                .to(orderStockExchange())
                .with(ORDER_STOCK_ROUTING);
    }

    @Bean
    public Queue orderStockDlq() {
        return QueueBuilder.durable(MqConst.ORDER_STOCK_DLQ).build();
    }

    @Bean
    public DirectExchange orderStockDlx() {
        return new DirectExchange(MqConst.ORDER_STOCK_DLX);
    }

    @Bean
    public Binding orderStockDlqBinding() {
        return BindingBuilder.bind(orderStockDlq())
                .to(orderStockDlx())
                .with(MqConst.ORDER_STOCK_DLQ_ROUTING);
    }

    @Bean
    public Queue stockResultQueue() {
        return QueueBuilder.durable(MqConst.STOCK_RESULT_QUEUE)
                .deadLetterExchange(MqConst.STOCK_RESULT_DLX)
                .deadLetterRoutingKey(MqConst.STOCK_RESULT_DLQ_ROUTING)
                .build();
    }
    @Bean
    public DirectExchange stockResultExchange() {
        return new DirectExchange(MqConst.STOCK_RESULT_EXCHANGE);
    }

    @Bean
    public Binding stockResultBinding() {
        return BindingBuilder
                .bind(stockResultQueue())
                .to(stockResultExchange())
                .with(MqConst.STOCK_RESULT_ROUTING);
    }

    @Bean
    public Queue stockResultDlq() {
        return QueueBuilder.durable(MqConst.STOCK_RESULT_DLQ).build();
    }

    @Bean
    public DirectExchange stockResultDlx() {
        return new DirectExchange(MqConst.STOCK_RESULT_DLX);
    }

    @Bean
    public Binding stockResultDlqBinding() {
        return BindingBuilder.bind(stockResultDlq())
                .to(stockResultDlx())
                .with(MqConst.STOCK_RESULT_DLQ_ROUTING);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
