package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.Category;
import app.allstackproject.privideo.repository.video.custom.CategoryRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
    List<Category> findByMemberGroupId(Long groupId);

    boolean existsByMemberGroupIdAndTitle(Long memberGroupId, String title);

    List<Category> findByMemberGroupIdIn(List<Long> groupIds);

    List<Category> findAllByMemberGroupIdIn(List<Long> groupdIds);
}
