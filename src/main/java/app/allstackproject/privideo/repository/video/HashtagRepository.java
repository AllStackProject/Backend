package app.allstackproject.privideo.repository.video;

import app.allstackproject.privideo.entity.Hashtag;
import app.allstackproject.privideo.repository.video.custom.HashtagRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long>, HashtagRepositoryCustom {
}
