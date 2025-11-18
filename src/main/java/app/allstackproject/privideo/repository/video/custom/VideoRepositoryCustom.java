package app.allstackproject.privideo.repository.video.custom;

import app.allstackproject.privideo.common.enumStatus.FilterType;
import app.allstackproject.privideo.dto.admin.QuitLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import app.allstackproject.privideo.dto.admin.VideoRankItem;
import app.allstackproject.privideo.dto.home.HomeVideoItem;
import java.util.List;
import java.util.Map;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    List<ReadAllVideoItem> findByOrgId(Long orgId);

    List<ReadAllVideoIntervalLogItem> findAllVideoIntervalLogByOrgId(Long orgId);

    List<QuitLogItem> findTopQuitRateVideosByOrgId(Long orgId, int limit);

    List<QuitLogItem> findLowQuitRateVideosByOrgId(Long orgId, int limit);

    List<VideoRankItem> findTop5VideoRankByOrgId(Long orgId);

    List<HomeVideoItem> findHomeVideos(Long orgId, Long memberId, FilterType filter);

    Map<Long, List<String>> findCategoriesForHomeVideos(Long memberId, List<Long> videoIds);
}
