package sahmoudi.agile.project_management.config;

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
    public static final String TASK_EVENTS_DLX = "task.events.dlx";
    public static final String PM_TASK_EVENTS_QUEUE = "pm-service.task-events";
    public static final String PM_TASK_EVENTS_DLQ = "pm-service.task-events.dlq";
    public static final String PM_EVENTS_EXCHANGE = "pm.events";

    @Bean
    public TopicExchange taskEventsExchange() {
        return ExchangeBuilder.topicExchange(TASK_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange pmEventsExchange() {
        return ExchangeBuilder.topicExchange(PM_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange taskEventsDlx() {
        return ExchangeBuilder.topicExchange(TASK_EVENTS_DLX).durable(true).build();
    }

    @Bean
    public Queue pmTaskEventsQueue() {
        return QueueBuilder.durable(PM_TASK_EVENTS_QUEUE)
            .withArgument("x-dead-letter-exchange", TASK_EVENTS_DLX)
            .build();
    }

    @Bean
    public Queue pmTaskEventsDlq() {
        return QueueBuilder.durable(PM_TASK_EVENTS_DLQ).build();
    }

    @Bean
    public Binding pmTaskEventsBinding(Queue pmTaskEventsQueue, TopicExchange taskEventsExchange) {
        return BindingBuilder.bind(pmTaskEventsQueue).to(taskEventsExchange).with("task.#");
    }

    @Bean
    public Binding pmTaskEventsDlqBinding(Queue pmTaskEventsDlq, TopicExchange taskEventsDlx) {
        return BindingBuilder.bind(pmTaskEventsDlq).to(taskEventsDlx).with("#");
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
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
