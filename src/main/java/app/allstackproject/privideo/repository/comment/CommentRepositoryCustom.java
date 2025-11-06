package app.allstackproject.privideo.repository.comment;

import app.allstackproject.privideo.dto.video.CommentInfo;
import java.util.List;

public interface CommentRepositoryCustom {
    List<CommentInfo> findAllByVideoId(Long videoId);

    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);
}
