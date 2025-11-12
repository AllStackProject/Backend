package app.allstackproject.privideo.repository.video.custom;

import app.allstackproject.privideo.dto.admin.ReadAllVideoDto;
import java.util.List;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    List<ReadAllVideoDto> findByOrgId(Long orgId);
}
