package app.allstackproject.privideo.domain.video.repository;

import app.allstackproject.privideo.domain.video.entity.Category;
import app.allstackproject.privideo.domain.video.repository.custom.CategoryRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
    List<Category> findByMemberGroupId(Long groupId);

    boolean existsByMemberGroupIdAndTitle(Long memberGroupId, String title);

    List<Category> findByMemberGroupIdIn(List<Long> groupIds);
}
