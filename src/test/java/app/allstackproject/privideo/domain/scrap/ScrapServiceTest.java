package app.allstackproject.privideo.domain.scrap;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_SCRAP_REQUEST;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_SCRAPPED;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_SCRAPPED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.allstackproject.privideo.domain.history.dto.HistoryItem;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.scrap.dto.ScrapResponse;
import app.allstackproject.privideo.domain.scrap.entity.Scrap;
import app.allstackproject.privideo.domain.scrap.repository.ScrapRepository;
import app.allstackproject.privideo.domain.scrap.service.ScrapService;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ScrapServiceTest {

    @InjectMocks
    private ScrapService scrapService;

    @Mock
    private ScrapRepository scrapRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private CdnUrlProvider cdnUrlProvider;

    @Test
    @DisplayName("getUserScraps - 썸네일 키를 CDN URL 로 변환해서 반환")
    void getUserScraps_convertImg() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;

        HistoryItem item1 = new HistoryItem();
        item1.setImg("img1.png");
        HistoryItem item2 = new HistoryItem();
        item2.setImg("img2.png");

        given(scrapRepository.findByMemberIdAndOrganizationId(memberId, orgId))
                .willReturn(List.of(item1, item2));
        given(cdnUrlProvider.generateImgUrl("img1.png")).willReturn("https://cdn/img1.png");
        given(cdnUrlProvider.generateImgUrl("img2.png")).willReturn("https://cdn/img2.png");

        // when
        ScrapResponse res = scrapService.getUserScraps(memberId, orgId);

        // then
        assertThat(res.getAllScrap())
                .extracting(HistoryItem::getImg)
                .containsExactly("https://cdn/img1.png", "https://cdn/img2.png");
    }

    @Test
    @DisplayName("addVideoScrap - member/org/video 조합이 유효하지 않으면 INVALID_SCRAP_REQUEST")
    void addVideoScrap_invalidRequest() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(false);

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> scrapService.addVideoScrap(memberId, orgId, videoId));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(INVALID_SCRAP_REQUEST);
    }

    @Test
    @DisplayName("addVideoScrap - 이미 스크랩 되어 있으면 VIDEO_ALREADY_SCRAPPED")
    void addVideoScrap_alreadyScrapped() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(true);
        given(scrapRepository.existsByMemberIdAndVideoId(memberId, videoId))
                .willReturn(true);

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> scrapService.addVideoScrap(memberId, orgId, videoId));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_ALREADY_SCRAPPED);
    }

    @Test
    @DisplayName("addVideoScrap - 경쟁 상태로 DataIntegrityViolationException 발생 시에도 VIDEO_ALREADY_SCRAPPED")
    void addVideoScrap_raceCondition() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(true);
        given(scrapRepository.existsByMemberIdAndVideoId(memberId, videoId))
                .willReturn(false);

        Member memberRef = mock(Member.class);
        Video videoRef = mock(Video.class);
        given(memberRepository.getReferenceById(memberId)).willReturn(memberRef);
        given(videoRepository.getReferenceById(videoId)).willReturn(videoRef);

        given(scrapRepository.save(any(Scrap.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> scrapService.addVideoScrap(memberId, orgId, videoId));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_ALREADY_SCRAPPED);
    }

    @Test
    @DisplayName("addVideoScrap - 정상 스크랩 시 true 반환")
    void addVideoScrap_success() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(true);
        given(scrapRepository.existsByMemberIdAndVideoId(memberId, videoId))
                .willReturn(false);

        Member memberRef = mock(Member.class);
        Video videoRef = mock(Video.class);
        given(memberRepository.getReferenceById(memberId)).willReturn(memberRef);
        given(videoRepository.getReferenceById(videoId)).willReturn(videoRef);
        given(scrapRepository.save(any(Scrap.class)))
                .willReturn(mock(Scrap.class));

        // when
        boolean result = scrapService.addVideoScrap(memberId, orgId, videoId);

        // then
        assertThat(result).isTrue();
        verify(scrapRepository).save(any(Scrap.class));
    }

    @Test
    @DisplayName("deleteVideoScrap - member/org/video 조합이 유효하지 않으면 INVALID_SCRAP_REQUEST")
    void deleteVideoScrap_invalidRequest() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(false);

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> scrapService.deleteVideoScrap(memberId, orgId, videoId));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(INVALID_SCRAP_REQUEST);
    }

    @Test
    @DisplayName("deleteVideoScrap - 삭제된 행이 없으면 VIDEO_NOT_SCRAPPED")
    void deleteVideoScrap_notScrapped() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(true);
        given(scrapRepository.deleteByMemberIdAndVideoId(memberId, videoId))
                .willReturn(0);

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> scrapService.deleteVideoScrap(memberId, orgId, videoId));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_NOT_SCRAPPED);
    }

    @Test
    @DisplayName("deleteVideoScrap - 정상 삭제 시 true 반환")
    void deleteVideoScrap_success() {
        // given
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(scrapRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId))
                .willReturn(true);
        given(scrapRepository.deleteByMemberIdAndVideoId(memberId, videoId))
                .willReturn(1);

        // when
        boolean result = scrapService.deleteVideoScrap(memberId, orgId, videoId);

        // then
        assertThat(result).isTrue();
        verify(scrapRepository).deleteByMemberIdAndVideoId(memberId, videoId);
    }
}