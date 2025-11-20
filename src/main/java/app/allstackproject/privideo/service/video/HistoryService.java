package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_CREATE_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.service.video.LogService.SEGMENT_SECONDS;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.dto.admin.VideoIntervalLogItem;
import app.allstackproject.privideo.dto.history.HistoryResponse;
import app.allstackproject.privideo.dto.history.VideoHistory;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;
    private final LogService logService;
    private final CdnUrlProvider cdnUrlProvider;

    public List<VideoIntervalLogItem> readMyVideoReport(Long memberId, Long orgId, Long videoId) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        Video video = videoRepository.findByIdAndOrganizationId(videoId, orgId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_IN_ORGANIZATION));

        if (!video.getCreator().getId().equals(memberId)) {
            throw new ApiException(VIDEO_CREATE_NOT_FOUND);
        }

        int totalSegCnt = (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS);

        List<Long> viewCounts = logService.getSegViewCounts(videoId, totalSegCnt);
        List<Long> quitCounts = logService.getSegQuitCounts(videoId, totalSegCnt);

        Long totalViews = viewCounts.stream().mapToLong(Long::longValue).sum();
        Long totalQuits = quitCounts.stream().mapToLong(Long::longValue).sum();

        List<VideoIntervalLogItem> intervals = IntStream.range(0, totalSegCnt)
                .mapToObj(i -> {
                    Long views = i < viewCounts.size() ? viewCounts.get(i) : 0L;
                    Long quits = i < quitCounts.size() ? quitCounts.get(i) : 0L;

                    Long viewRate = totalViews > 0 ? (views * 100) / totalViews : 0L;

                    Long quitRate = totalQuits > 0 ? (quits * 100) / totalQuits : 0L;

                    return new VideoIntervalLogItem((long) i, views, quits, viewRate, quitRate);
                })
                .collect(Collectors.toList());

        return intervals;
    }

    @Transactional(readOnly = true)
    public HistoryResponse getUserVideos(Long memberId, Long orgId) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        List<VideoHistory> histories = historyRepository.findByMemberId(memberId);
        histories.forEach(history -> history.setImg(cdnUrlProvider.generateFileUrl(history.getImg())));
        return HistoryResponse.of(histories);
    }
}

