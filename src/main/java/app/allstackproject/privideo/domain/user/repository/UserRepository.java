package app.allstackproject.privideo.domain.user.repository;

import app.allstackproject.privideo.shared.enums.BaseStatusType;
import app.allstackproject.privideo.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndStatus(String email, BaseStatusType status);

    Optional<User> findByEmailAndStatus(String email, BaseStatusType status);
}
