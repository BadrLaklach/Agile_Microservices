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
import sahmoudi.agile.user_service.auth.exception.InvalidCredentialsException;
import sahmoudi.agile.user_service.user.entity.User;
import sahmoudi.agile.user_service.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationSeconds;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.jwt.expiration-seconds:604800}") long expirationSeconds) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
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
        return buildResult(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(found -> passwordEncoder.matches(password, found.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return buildResult(user);
    }

    private AuthResult buildResult(User user) {
        String token = jwtService.generateAccessToken(user);
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(expirationSeconds))
                .build();

        return new AuthResult(cookie, user);
    }

    public record AuthResult(ResponseCookie cookie, User user) {
    }
}
