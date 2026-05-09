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
import sahmoudi.agile.user_service.auth.dto.RegisterResponse;
import sahmoudi.agile.user_service.auth.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthResult result = authService.register(request);
        RegisterResponse response = new RegisterResponse(result.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", result.cookie().toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<RegisterResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request.email(), request.password());
        RegisterResponse response = new RegisterResponse(result.role());
        return ResponseEntity.ok()
                .header("Set-Cookie", result.cookie().toString())
                .body(response);
    }
}
