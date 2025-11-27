package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.VideoCategoryMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoCategoryMappingRepository extends JpaRepository<VideoCategoryMapping, Long> {
    void deleteAllByCategoryId(Long categoryId);

    void deleteAllByVideoId(Long videoId);

    List<VideoCategoryMapping> findAllByVideoId(Long videoId);
}
