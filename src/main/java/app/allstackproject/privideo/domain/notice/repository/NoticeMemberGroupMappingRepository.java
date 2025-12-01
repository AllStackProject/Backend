package app.allstackproject.privideo.domain.notice.repository;

import app.allstackproject.privideo.domain.notice.entity.NoticeMemberGroupMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeMemberGroupMappingRepository extends JpaRepository<NoticeMemberGroupMapping, Long> {
    List<NoticeMemberGroupMapping> findAllByNoticeId(Long noticeId);

    void deleteAllByNoticeId(Long noticeId);

    void deleteAllByMemberGroupId(Long memberGroupId);
}
