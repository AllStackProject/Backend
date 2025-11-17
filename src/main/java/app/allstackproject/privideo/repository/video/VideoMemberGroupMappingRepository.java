package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.VideoMemberGroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoMemberGroupMappingRepository extends JpaRepository<VideoMemberGroupMapping, Long> {
    void deleteByMemberGroupId(Long groupId);
}
