package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_SCRAP_REQUEST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_SCRAPPED;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.video.ScrapResponse;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.HistoryRepository;
import app.allstackproject.privideo.repository.ScrapRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;

    @Transactional(readOnly = true)
    public ScrapResponse getUserScraps(Long memberId, Long orgId) {

        List<Scrap> scrapList = scrapRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);
        List<History> historyList = historyRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);

        Map<Long, History> historyMapByVideoId = historyList.stream()
                .collect(Collectors.toMap(
                        history -> history.getVideo().getId(),
                        history -> history
                ));

        return ScrapResponse.of(scrapList, historyMapByVideoId);
    }

    public boolean addVideoScrap(Long memberId, Long orgId, Long videoId) {
        if (!scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId)) {
            throw new ApiException(INVALID_SCRAP_REQUEST);
        }

        try {
            if (scrapRepository.existsByMemberIdAndVideoId(memberId, videoId)) {
                throw new ApiException(VIDEO_ALREADY_SCRAPPED);
            }
            scrapRepository.save(Scrap.create(memberRepository.getReferenceById(memberId),
                    videoRepository.getReferenceById(videoId)));
        } catch (DataIntegrityViolationException e) {
            // 경쟁 상태에서 뒤늦게 들어온 요청
            throw new ApiException(VIDEO_ALREADY_SCRAPPED);
        }

        return true;
    }
}
