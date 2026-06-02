package sahmoudi.agile.project_management.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import sahmoudi.agile.project_management.event.payload.MemberInvitedEvent;
import sahmoudi.agile.project_management.event.payload.MemberRemovedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String PM_EXCHANGE = "pm.events";

    public void publishMemberInvited(MemberInvitedEvent event) {
        send("member.invited", event);
    }

    public void publishMemberRemoved(MemberRemovedEvent event) {
        send("member.removed", event);
    }

    private void send(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(PM_EXCHANGE, routingKey, payload);
        } catch (Exception e) {
            log.error("Failed to publish PM event [{}]: {}", routingKey, e.getMessage());
        }
    }
}
