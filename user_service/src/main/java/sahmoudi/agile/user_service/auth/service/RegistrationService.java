package sahmoudi.agile.user_service.auth.service;

import java.time.Duration;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sahmoudi.agile.user_service.auth.dto.RegisterRequest;
import sahmoudi.agile.user_service.auth.exception.EmailAlreadyExistsException;
import sahmoudi.agile.user_service.user.entity.User;
import sahmoudi.agile.user_service.user.repository.UserRepository;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationSeconds;

    public RegistrationService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               @Value("${app.jwt.expiration-seconds:604800}") long expirationSeconds) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public RegistrationResult register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateAccessToken(savedUser);
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofSeconds(expirationSeconds))
                .build();

        return new RegistrationResult(cookie, savedUser.getRole());
    }

    public record RegistrationResult(ResponseCookie cookie, sahmoudi.agile.user_service.user.entity.Role role) {
    }
}
