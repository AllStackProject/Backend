package app.allstackproject.privideo.domain.member.repository;

import app.allstackproject.privideo.domain.admin.dto.MemberGroupItem;
import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import app.allstackproject.privideo.domain.member.repository.custom.MemberGroupRepositoryCustom;
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
