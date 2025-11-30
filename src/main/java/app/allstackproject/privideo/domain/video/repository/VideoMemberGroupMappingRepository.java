package app.allstackproject.privideo.domain.video.repository;

import app.allstackproject.privideo.domain.video.entity.VideoMemberGroupMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoMemberGroupMappingRepository extends JpaRepository<VideoMemberGroupMapping, Long> {
    void deleteAllByMemberGroupId(Long groupId);

    void deleteAllByVideoId(Long videoId);

    List<VideoMemberGroupMapping> findAllByVideoId(Long videoId);
}
