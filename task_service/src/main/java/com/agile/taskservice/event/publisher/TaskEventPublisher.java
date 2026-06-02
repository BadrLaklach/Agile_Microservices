package com.agile.taskservice.event.publisher;

import com.agile.taskservice.event.payload.TaskCreatedEvent;
import com.agile.taskservice.event.payload.TaskDeletedEvent;
import com.agile.taskservice.event.payload.TaskStatusChangedEvent;
import com.agile.taskservice.event.payload.TaskUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "task.events";

    public void publishCreated(TaskCreatedEvent event) {
        send("task.created", event);
    }
    public void publishUpdated(TaskUpdatedEvent event) {
        send("task.updated", event);
    }
    public void publishStatusChanged(TaskStatusChangedEvent event) {
        send("task.status.changed", event);
    }
    public void publishDeleted(TaskDeletedEvent event) {
        send("task.deleted", event);
    }

    private void send(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
        } catch (Exception e) {
            log.error("Failed to publish [{}]: {}", routingKey, e.getMessage());
        }
    }
}
