package app.allstackproject.privideo.repository.member;

import app.allstackproject.privideo.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUserId(Long userId);
  
    Optional<Member> findByUserIdAndOrganizationId(Long userId, Long orgId);
}
