package cloudmart.product.config;

import common.constant.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue orderStockQueue() {
        return QueueBuilder.durable(MqConst.ORDER_STOCK_QUEUE)
                .deadLetterExchange(MqConst.ORDER_STOCK_DLX)
                .deadLetterRoutingKey(MqConst.ORDER_STOCK_DLQ_ROUTING)
                .build();
    }

    @Bean
    public DirectExchange orderStockExchange() {
        return new DirectExchange(MqConst.ORDER_STOCK_EXCHANGE);
    }
    @Bean
    public DirectExchange stockResultExchange() {
        return new DirectExchange(MqConst.STOCK_RESULT_EXCHANGE);
    }
    @Bean
    public Binding orderStockBinding() {
        return BindingBuilder.bind(orderStockQueue())
                .to(orderStockExchange())
                .with(MqConst.ORDER_STOCK_ROUTING);
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
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
