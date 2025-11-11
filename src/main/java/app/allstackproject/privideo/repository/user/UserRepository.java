package app.allstackproject.privideo.repository.user;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndStatus(String email, BaseStatusType status);

    Optional<User> findByEmailAndStatus(String email, BaseStatusType status);
}
