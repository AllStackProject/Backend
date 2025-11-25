package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.video.custom.VideoRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, VideoRepositoryCustom {
    boolean existsByIdAndOrganizationId(Long id, Long orgId);

    Optional<Video> findByIdAndOrganizationId(Long videoId, Long orgId);

    Optional<Video> findByVideoKey(String videoKey);
}
