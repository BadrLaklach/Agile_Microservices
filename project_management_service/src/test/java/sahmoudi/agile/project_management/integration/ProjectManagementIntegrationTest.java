package sahmoudi.agile.project_management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import sahmoudi.agile.project_management.dto.request.AssignTaskRequest;
import sahmoudi.agile.project_management.dto.request.CreateProjectRequest;
import sahmoudi.agile.project_management.dto.request.CreateSprintRequest;
import sahmoudi.agile.project_management.dto.response.ProjectResponse;
import sahmoudi.agile.project_management.dto.response.SprintResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.config.name=application-test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectManagementIntegrationTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper;
        }
    }

    @LocalServerPort
    private int port;

    private HttpClient httpClient;
    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private sahmoudi.agile.project_management.client.TaskServiceClient taskServiceClient;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private sahmoudi.agile.project_management.client.UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Test
    void shouldCreateProjectAndListIt() throws Exception {
        UUID callerId = UUID.randomUUID();

        // Setup mock for the caller user
        when(userServiceClient.getUserById(eq(callerId), eq(callerId), eq("PO")))
            .thenReturn(new sahmoudi.agile.project_management.dto.response.UserResponse(callerId, "Caller", "Admin", "admin@test.com", "ADMIN", Instant.now()));

        CreateProjectRequest createReq = new CreateProjectRequest(
            "Integration Project", 
            "Desc", 
            "SCRUM", 
            java.time.LocalDate.now(), 
            java.time.LocalDate.now().plusDays(30)
        );
        
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createReq)))
                .build();
                
        HttpResponse<String> postRes = httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
        assertThat(postRes.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        
        ProjectResponse projectResponse = objectMapper.readValue(postRes.body(), ProjectResponse.class);
        assertThat(projectResponse.name()).isEqualTo("Integration Project");
        
        // List projects
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects"))
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .GET()
                .build();
                
        HttpResponse<String> getRes = httpClient.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertThat(getRes.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(getRes.body()).contains("Integration Project");
    }

    @Test
    void shouldCreateSprintAndAssignTask() throws Exception {
        UUID callerId = UUID.randomUUID();

        // Setup mock for the caller user
        when(userServiceClient.getUserById(eq(callerId), eq(callerId), eq("PO")))
            .thenReturn(new sahmoudi.agile.project_management.dto.response.UserResponse(callerId, "Caller", "Admin", "admin@test.com", "ADMIN", Instant.now()));

        // 1. Create Project
        CreateProjectRequest createProjReq = new CreateProjectRequest(
            "Sprint Test Project", 
            "Desc", 
            "SCRUM", 
            java.time.LocalDate.now(), 
            java.time.LocalDate.now().plusDays(30)
        );
        HttpRequest postProjReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createProjReq)))
                .build();
        HttpResponse<String> projRes = httpClient.send(postProjReq, HttpResponse.BodyHandlers.ofString());
        assertThat(projRes.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        ProjectResponse project = objectMapper.readValue(projRes.body(), ProjectResponse.class);
        
        // 2. Create Sprint
        CreateSprintRequest createSprintReq = new CreateSprintRequest(
            "Sprint 1", 
            "Finish integration",
            100,
            java.time.LocalDate.now(), 
            java.time.LocalDate.now().plusDays(14)
        );
        HttpRequest postSprintReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/sprints"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createSprintReq)))
                .build();
        HttpResponse<String> sprintRes = httpClient.send(postSprintReq, HttpResponse.BodyHandlers.ofString());
        assertThat(sprintRes.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        SprintResponse sprint = objectMapper.readValue(sprintRes.body(), SprintResponse.class);
        
        // 3. Mock Task Service PATCH response
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskServiceClient).updateTaskSprint(any(UUID.class), any(UUID.class), any(UUID.class), anyString());

        // 4. Assign Task to Sprint
        AssignTaskRequest assignReq = new AssignTaskRequest(taskId);
        HttpRequest assignTaskReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/sprints/" + sprint.id() + "/tasks"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(assignReq)))
                .build();
        
        HttpResponse<String> assignRes = httpClient.send(assignTaskReq, HttpResponse.BodyHandlers.ofString());
        assertThat(assignRes.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        
        // 5. Verify Mockito was called
        verify(taskServiceClient, times(1)).updateTaskSprint(eq(taskId), eq(sprint.id()), eq(callerId), eq("PO"));

        // 6. Test invite member
        UUID newMemberId = UUID.randomUUID();
        when(userServiceClient.getUserByEmail(eq("test@test.com"), eq(callerId), eq("PO")))
            .thenReturn(new sahmoudi.agile.project_management.dto.response.UserResponse(newMemberId, "test@test.com", "Test", "Test", "DEV", Instant.now()));
        sahmoudi.agile.project_management.dto.request.InviteMemberRequest inviteReq = 
            new sahmoudi.agile.project_management.dto.request.InviteMemberRequest("test@test.com", "DEV", false, null, null, null);
        HttpRequest inviteMemberReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/members"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(inviteReq)))
                .build();
        HttpResponse<String> inviteRes = httpClient.send(inviteMemberReq, HttpResponse.BodyHandlers.ofString());
        assertThat(inviteRes.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // 7. Start sprint
        HttpRequest startSprintReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/sprints/" + sprint.id() + "/start"))
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> startSprintRes = httpClient.send(startSprintReq, HttpResponse.BodyHandlers.ofString());
        assertThat(startSprintRes.statusCode()).isEqualTo(HttpStatus.OK.value());

        // 8. Remove Task
        doNothing().when(taskServiceClient).updateTaskSprint(eq(taskId), isNull(), eq(callerId), eq("PO"));
        HttpRequest removeTaskReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/sprints/" + sprint.id() + "/tasks/" + taskId))
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .DELETE()
                .build();
        HttpResponse<String> removeTaskRes = httpClient.send(removeTaskReq, HttpResponse.BodyHandlers.ofString());
        assertThat(removeTaskRes.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(taskServiceClient, times(1)).updateTaskSprint(eq(taskId), isNull(), eq(callerId), eq("PO"));

        // 9. Close sprint
        HttpRequest closeSprintReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/sprints/" + sprint.id() + "/close"))
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> closeSprintRes = httpClient.send(closeSprintReq, HttpResponse.BodyHandlers.ofString());
        assertThat(closeSprintRes.statusCode()).isEqualTo(HttpStatus.OK.value());

        // 10. Remove Member
        HttpRequest removeMemberReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/members/" + newMemberId))
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .DELETE()
                .build();
        HttpResponse<String> removeMemberRes = httpClient.send(removeMemberReq, HttpResponse.BodyHandlers.ofString());
        assertThat(removeMemberRes.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        // 11. Archive Project
        HttpRequest archiveProjectReq = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/projects/" + project.id() + "/archive"))
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", "PO")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> archiveProjectRes = httpClient.send(archiveProjectReq, HttpResponse.BodyHandlers.ofString());
        assertThat(archiveProjectRes.statusCode()).isEqualTo(HttpStatus.OK.value());

    }
}
