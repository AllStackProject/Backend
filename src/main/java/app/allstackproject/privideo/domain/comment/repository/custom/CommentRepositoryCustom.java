package app.allstackproject.privideo.domain.comment.repository.custom;

import app.allstackproject.privideo.dto.video.CommentInfo;
import java.util.List;

public interface CommentRepositoryCustom {
    List<CommentInfo> findAllByVideoId(Long videoId);
}
