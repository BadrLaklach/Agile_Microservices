package com.agile.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PM_EVENTS_EXCHANGE          = "pm.events";
    public static final String NOTIFICATION_QUEUE          = "notification-service.pm-events";
    public static final String NOTIFICATION_DLX            = "pm.events.dlx";

    @Bean
    public TopicExchange pmEventsExchange() {
        return ExchangeBuilder.topicExchange(PM_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
            .build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue,
                                        TopicExchange pmEventsExchange) {
        return BindingBuilder
            .bind(notificationQueue)
            .to(pmEventsExchange)
            .with("notification.#");
    }

    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        return mapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
