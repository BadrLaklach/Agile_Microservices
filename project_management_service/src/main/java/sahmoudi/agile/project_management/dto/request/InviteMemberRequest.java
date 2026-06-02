package sahmoudi.agile.project_management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record InviteMemberRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
        @NotNull @Pattern(regexp = "ADMIN|PO|SM|DEV|MA") String role,
        boolean isNew,
        String firstName,
        String lastName,
        String password
) {}
