package app.allstackproject.privideo.repository.comment.custom;

import app.allstackproject.privideo.dto.video.CommentInfo;
import java.util.List;

public interface CommentRepositoryCustom {
    List<CommentInfo> findAllByVideoId(Long videoId);
}
