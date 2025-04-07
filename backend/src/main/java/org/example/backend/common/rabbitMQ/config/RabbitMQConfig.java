/**
 * File Name: RabbitMQConfig.java
 * Description: rabbitMQ的配置类
 * Author: holic512
 * Created Date: 2024-09-10
 * Version: 1.0
 * Usage:
 * 用来配置rabbitMQ的配置,其他类可以通过rabbitTemplate来调用
 */
package org.example.backend.common.rabbitMQ.config;

import org.example.backend.common.rabbitMQ.enums.MQExchangeType;
import org.example.backend.common.rabbitMQ.enums.MQQueueName;
import org.example.backend.common.rabbitMQ.enums.MQRoutingKey;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {


    private final ConnectionFactory rabbitConnectionFactory;
    private final RabbitProperties properties;

    // 定义交换机
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(MQExchangeType.DIRECT_EXCHANGE.getValue());
    }

    @Autowired
    RabbitMQConfig(ConnectionFactory connectionFactory, RabbitProperties properties) {
        this.rabbitConnectionFactory = connectionFactory;
        this.properties = properties;
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(rabbitConnectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 启用手动应答
        // 如果需要，还可以设置其他属性，如并发消费者数等
        return factory;
    }


    // 邮件队列
    @Bean
    public Queue emailQueue() {
        return new Queue(MQQueueName.EMAIL_QUEUE.getValue(), true, false, false);
    }

    // 将邮件队列与交换机绑定
    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with(MQRoutingKey.EMAIL_ROUTING_KEY.getKey());
    }


}
