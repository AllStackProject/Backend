package app.allstackproject.privideo.repository.notice;

import app.allstackproject.privideo.entity.NoticeMemberGroupMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeMemberGroupMappingRepository extends JpaRepository<NoticeMemberGroupMapping, Long> {
    List<NoticeMemberGroupMapping> findAllByNoticeId(Long noticeId);
}
