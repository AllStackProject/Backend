package app.allstackproject.privideo.repository.video.custom;

import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import java.util.List;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    List<ReadAllVideoItem> findByOrgId(Long orgId);
}
