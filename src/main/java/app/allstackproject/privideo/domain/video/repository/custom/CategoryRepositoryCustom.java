package app.allstackproject.privideo.domain.video.repository.custom;

import java.util.List;

public interface CategoryRepositoryCustom {
    List<String> findAllByVideoId(Long videoId);
}
