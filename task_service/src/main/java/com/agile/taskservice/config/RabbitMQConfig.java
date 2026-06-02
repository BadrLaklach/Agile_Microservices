package com.agile.taskservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    public static final String TASK_EVENTS_EXCHANGE = "task.events";
    public static final String PM_EVENTS_EXCHANGE   = "pm.events";
    public static final String PM_EVENTS_QUEUE      = "task-service.pm-events";
    public static final String PM_EVENTS_DLX        = "pm.events.dlx";

    @Bean
    public TopicExchange taskEventsExchange() {
        return ExchangeBuilder.topicExchange(TASK_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange pmEventsExchange() {
        return ExchangeBuilder.topicExchange(PM_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange pmEventsDlx() {
        return ExchangeBuilder.topicExchange(PM_EVENTS_DLX).durable(true).build();
    }

    @Bean
    public Queue pmEventsQueue() {
        return QueueBuilder.durable(PM_EVENTS_QUEUE)
            .withArgument("x-dead-letter-exchange", PM_EVENTS_DLX)
            .build();
    }

    @Bean
    public Binding pmEventsBinding(Queue pmEventsQueue, TopicExchange pmEventsExchange) {
        return BindingBuilder
            .bind(pmEventsQueue)
            .to(pmEventsExchange)
            .with("member.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            log.info("PUBLISHING RABBITMQ EVENT Payload: {}", new String(message.getBody()));
            return message;
        });
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAfterReceivePostProcessors(message -> {
            log.info("CONSUMED RABBITMQ EVENT Queue: {}, Payload: {}", 
                message.getMessageProperties().getConsumerQueue(),
                new String(message.getBody()));
            return message;
        });
        return factory;
    }
}
