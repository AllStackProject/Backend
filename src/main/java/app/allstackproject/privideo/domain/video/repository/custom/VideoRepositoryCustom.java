package app.allstackproject.privideo.domain.video.repository.custom;

import app.allstackproject.privideo.domain.video.enums.FilterType;
import app.allstackproject.privideo.domain.admin.dto.QuitLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.admin.dto.VideoRankItem;
import app.allstackproject.privideo.dto.home.HomeVideoItem;
import java.util.List;
import java.util.Map;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    List<ReadAllVideoItem> findByOrgId(Long orgId);

    List<ReadAllVideoItem> findByOrgIdAndCreatorId(Long orgId, Long memberId);

    List<ReadAllVideoIntervalLogItem> findAllVideoIntervalLogByOrgId(Long orgId);

    List<QuitLogItem> findTopQuitRateVideosByOrgId(Long orgId, int limit);

    List<QuitLogItem> findLowQuitRateVideosByOrgId(Long orgId, int limit);

    List<VideoRankItem> findTop5VideoRankByOrgId(Long orgId);

    List<HomeVideoItem> findHomeVideos(Long orgId, Long memberId, FilterType filter);

    Map<Long, List<String>> findCategoriesForHomeVideos(Long memberId, List<Long> videoIds);

    List<HomeVideoItem> findSearchVideos(Long orgId, Long memberId, String keyword);
}
