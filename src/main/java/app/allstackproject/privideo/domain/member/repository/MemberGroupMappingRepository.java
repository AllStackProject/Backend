package app.allstackproject.privideo.domain.member.repository;

import app.allstackproject.privideo.domain.member.entity.MemberGroupMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGroupMappingRepository extends JpaRepository<MemberGroupMapping, Long> {
    void deleteByMemberId(Long id);

    void deleteByMemberGroupId(Long groupId);

    List<MemberGroupMapping> findAllByMemberId(Long memberId);
}
