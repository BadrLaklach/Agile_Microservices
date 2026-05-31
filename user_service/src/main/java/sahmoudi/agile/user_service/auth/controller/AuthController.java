package sahmoudi.agile.user_service.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sahmoudi.agile.user_service.auth.dto.LoginRequest;
import sahmoudi.agile.user_service.auth.dto.RegisterRequest;
import sahmoudi.agile.user_service.auth.service.AuthService;
import sahmoudi.agile.user_service.user.dto.UserResponse;
import sahmoudi.agile.user_service.user.entity.User;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", result.cookie().toString())
                .body(mapToUserResponse(result.user()));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request.email(), request.password());
        return ResponseEntity.ok()
                .header("Set-Cookie", result.cookie().toString())
                .body(mapToUserResponse(result.user()));
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
