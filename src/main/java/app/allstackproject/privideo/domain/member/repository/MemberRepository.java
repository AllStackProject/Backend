package app.allstackproject.privideo.domain.member.repository;

import app.allstackproject.privideo.shared.enums.BaseStatusType;
import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.custom.MemberRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUserIdAndStatus(Long userId, BaseStatusType status);

    Optional<Member> findByIdAndStatus(Long id, BaseStatusType status);

    Optional<Member> findByIdAndOrganizationIdAndStatus(Long id, Long orgId, BaseStatusType status);

    Optional<Member> findByUserIdAndOrganizationIdAndStatus(Long userId, Long orgId, BaseStatusType status);

    Optional<Member> findByOrganizationIdAndNicknameAndStatus(Long orgId, String nickname, BaseStatusType status);

    boolean existsByIdAndOrganizationIdAndStatus(Long id, Long orgId, BaseStatusType status);

    Long countByOrganizationIdAndJoinStatusAndStatus(Long orgId, JoinStatusType joinStatus, BaseStatusType status);

    void deleteAllByOrganizationId(Long orgId);
}
