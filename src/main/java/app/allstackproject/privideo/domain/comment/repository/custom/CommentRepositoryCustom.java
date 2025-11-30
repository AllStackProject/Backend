package app.allstackproject.privideo.domain.comment.repository.custom;

import app.allstackproject.privideo.domain.comment.dto.response.CommentInfo;
import java.util.List;

public interface CommentRepositoryCustom {
    List<CommentInfo> findAllByVideoId(Long videoId);
}
