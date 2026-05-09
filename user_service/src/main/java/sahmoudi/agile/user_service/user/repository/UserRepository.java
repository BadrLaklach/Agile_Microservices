package sahmoudi.agile.user_service.user.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import sahmoudi.agile.user_service.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmailIgnoreCase(String email);
}

