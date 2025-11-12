package app.allstackproject.privideo.repository.member;

import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.repository.member.custom.MemberGroupRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long>, MemberGroupRepositoryCustom {
    boolean existsByName(String name);

    Optional<MemberGroup> findByIdAndOrganizationId(Long id, Long orgId);

    boolean existsByIdAndOrganizationId(Long id, Long orgId);
}
