package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.Category;
import app.allstackproject.privideo.repository.video.custom.CategoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
}
