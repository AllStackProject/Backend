package app.allstackproject.privideo.repository.member;

import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.repository.member.custom.MemberGroupRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long>, MemberGroupRepositoryCustom {
    boolean existsByName(String name);

    Optional<MemberGroup> findByIdAndOrganizationId(Long id, Long orgId);

    boolean existsByIdAndOrganizationId(Long id, Long orgId);

    long countByIdInAndOrganizationId(List<Long> ids, Long orgId);

    List<MemberGroupItem> findAllByOrganizationId(Long orgId);
}
