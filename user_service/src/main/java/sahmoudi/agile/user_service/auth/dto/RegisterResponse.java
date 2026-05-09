package sahmoudi.agile.user_service.auth.dto;

import sahmoudi.agile.user_service.user.entity.Role;

public record RegisterResponse(
        Role role
) {
}
