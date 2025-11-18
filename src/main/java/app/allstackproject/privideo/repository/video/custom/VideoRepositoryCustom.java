package app.allstackproject.privideo.repository.video.custom;

import app.allstackproject.privideo.dto.admin.QuitLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import app.allstackproject.privideo.dto.admin.VideoRankItem;
import java.util.List;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    List<ReadAllVideoItem> findByOrgId(Long orgId);

    List<ReadAllVideoIntervalLogItem> findAllVideoIntervalLogByOrgId(Long orgId);

    List<QuitLogItem> findTopQuitRateVideosByOrgId(Long orgId, int limit);

    List<QuitLogItem> findLowQuitRateVideosByOrgId(Long orgId, int limit);

    List<VideoRankItem> findTop5VideoRankByOrgId(Long orgId);
}
