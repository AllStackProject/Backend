package app.allstackproject.privideo.repository.video.custom;

import java.util.List;

public interface CategoryRepositoryCustom {
    List<String> findAllByVideoId(Long videoId);
}
