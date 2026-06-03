package com.agile.notificationservice.consumer;

import com.agile.notificationservice.dto.DeveloperOverloadEvent;
import com.agile.notificationservice.dto.SprintOverloadEvent;
import com.agile.notificationservice.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification-service.pm-events")
    public void handle(Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("eventType");

            switch (eventType) {
                case "SPRINT_OVERLOAD" -> emailService.sendSprintOverloadAlert(
                    objectMapper.convertValue(payload, SprintOverloadEvent.class));
                case "DEVELOPER_OVERLOAD" -> emailService.sendDeveloperOverloadAlert(
                    objectMapper.convertValue(payload, DeveloperOverloadEvent.class));
                default ->
                    log.warn("Unknown notification event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage());
            // Do NOT rethrow — message will route to dead-letter exchange
        }
    }
}
