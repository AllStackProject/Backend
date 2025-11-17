package app.allstackproject.privideo.repository.video.custom;

import app.allstackproject.privideo.dto.admin.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import java.util.List;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    List<ReadAllVideoItem> findByOrgId(Long orgId);

    List<ReadAllVideoIntervalLogItem> findAllVideoIntervalLogByOrgId(Long orgId);
}
