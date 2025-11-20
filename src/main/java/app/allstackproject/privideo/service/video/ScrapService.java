package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_SCRAP_REQUEST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_SCRAPPED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_SCRAPPED;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.dto.history.HistoryItem;
import app.allstackproject.privideo.dto.scrap.ScrapResponse;
import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.scrap.ScrapRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;
    private final CdnUrlProvider cdnUrlProvider;

    @Transactional(readOnly = true)
    public ScrapResponse getUserScraps(Long memberId, Long orgId) {
        List<HistoryItem> scrapList = scrapRepository.findByMemberIdAndOrganizationId(memberId, orgId);
        scrapList.forEach(scrap -> scrap.setImg(cdnUrlProvider.generateFileUrl(scrap.getImg())));
        return ScrapResponse.of(scrapList);
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

    public boolean deleteVideoScrap(Long memberId, Long orgId, Long videoId) {
        if (!scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId)) {
            throw new ApiException(INVALID_SCRAP_REQUEST);
        }

        int affected = scrapRepository.deleteByMemberIdAndVideoId(memberId, videoId);
        if (affected == 0) {
            throw new ApiException(VIDEO_NOT_SCRAPPED);
        }

        return affected == 1;
    }
}
