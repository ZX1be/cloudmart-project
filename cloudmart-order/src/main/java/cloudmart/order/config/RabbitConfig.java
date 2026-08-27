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
        return new Queue(SECKILL_QUEUE, true);
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
    public Queue orderStockQueue() {
        return new Queue(ORDER_STOCK_QUEUE, true);
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
