package app.allstackproject.privideo.repository.member;

import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.repository.member.custom.MemberGroupRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long>, MemberGroupRepositoryCustom {
}
