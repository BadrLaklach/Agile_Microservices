package sahmoudi.agile.project_management.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sahmoudi.agile.project_management.dto.request.NotificationRequest;

@Slf4j
@Component
public class NotificationServiceClient {

    private final RestClient restClient;

    public NotificationServiceClient(@Value("${app.notification-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void send(NotificationRequest request) {
        try {
            restClient.post()
                .uri("/api/v1/notifications")
                .body(request)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Notification Service call failed — event: {}, error: {}",
                request.eventType(), e.getMessage());
        }
    }
}
