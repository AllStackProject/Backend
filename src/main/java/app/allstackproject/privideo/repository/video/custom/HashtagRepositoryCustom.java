package app.allstackproject.privideo.repository.video.custom;

import java.util.List;

public interface HashtagRepositoryCustom {
    List<String> findAllByVideoId(Long videoId);
}
