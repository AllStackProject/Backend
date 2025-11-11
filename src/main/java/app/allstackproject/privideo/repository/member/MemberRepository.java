package app.allstackproject.privideo.repository.member;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.repository.member.custom.MemberRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUserId(Long userId);

    Optional<Member> findByIdAndStatus(Long id, BaseStatusType status);

    Optional<Member> findByIdAndOrganizationId(Long id, Long orgId);

    Optional<Member> findByUserIdAndOrganizationId(Long userId, Long orgId);

    Optional<Member> findByOrganizationIdAndNicknameAndStatus(Long orgId, String nickname,
                                                              BaseStatusType baseStatusType);
}
