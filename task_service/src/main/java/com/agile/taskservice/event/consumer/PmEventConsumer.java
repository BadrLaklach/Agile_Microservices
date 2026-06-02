package com.agile.taskservice.event.consumer;

import com.agile.taskservice.event.payload.MemberInvitedEvent;
import com.agile.taskservice.event.payload.MemberRemovedEvent;
import com.agile.taskservice.model.MemberProjectView;
import com.agile.taskservice.repository.MemberProjectViewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PmEventConsumer {

    private final MemberProjectViewRepository memberRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "task-service.pm-events")
    public void handle(Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("eventType");

            switch (eventType) {
                case "MEMBER_INVITED" -> handleMemberInvited(
                    objectMapper.convertValue(payload, MemberInvitedEvent.class));
                case "MEMBER_REMOVED" -> handleMemberRemoved(
                    objectMapper.convertValue(payload, MemberRemovedEvent.class));
                default -> log.warn("Unknown PM event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process PM event: {}", e.getMessage());
        }
    }

    private void handleMemberInvited(MemberInvitedEvent event) {
        MemberProjectView view = MemberProjectView.builder()
            .id(MemberProjectView.buildId(event.userId(), event.projectId()))
            .userId(event.userId())
            .projectId(event.projectId())
            .role(event.role())
            .joinedAt(event.invitedAt())
            .build();
        memberRepository.save(view);
        log.info("Membership added: user={} project={}", event.userId(), event.projectId());
    }

    private void handleMemberRemoved(MemberRemovedEvent event) {
        memberRepository.deleteById(
            MemberProjectView.buildId(event.userId(), event.projectId()));
        log.info("Membership removed: user={} project={}", event.userId(), event.projectId());
    }
}
