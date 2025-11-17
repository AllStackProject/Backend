package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.VideoCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoCategoryMappingRepository extends JpaRepository<VideoCategoryMapping, Long> {
    void deleteByCategoryId(Long categoryId);
}
