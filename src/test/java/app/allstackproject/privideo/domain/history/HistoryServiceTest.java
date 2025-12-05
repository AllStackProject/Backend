package app.allstackproject.privideo.domain.history;

import static app.allstackproject.privideo.domain.video.service.LogService.SEGMENT_SECONDS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_CREATE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import app.allstackproject.privideo.domain.admin.dto.VideoIntervalLogItem;
import app.allstackproject.privideo.domain.history.repository.HistoryRepository;
import app.allstackproject.privideo.domain.history.service.HistoryService;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.domain.video.service.LogService;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @InjectMocks
    private HistoryService historyService;

    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private LogService logService;
    @Mock
    private CdnUrlProvider cdnUrlProvider;

    @Test
    @DisplayName("멤버가 조직에 속해있지 않으면 MEMBER_NOT_IN_ORGANIZATION 예외")
    void readMyVideoReport_memberNotInOrg() {
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        given(memberRepository.existsByIdAndOrganizationIdAndStatus(
                memberId, orgId, app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE))
                .willReturn(false);

        ApiException ex = assertThrows(ApiException.class,
                () -> historyService.readMyVideoReport(memberId, orgId, videoId));

        assertThat(ex.getResponseStatus()).isEqualTo(MEMBER_NOT_IN_ORGANIZATION);
    }

    @Test
    @DisplayName("다른 사용자가 만든 영상이면 VIDEO_CREATE_NOT_FOUND 예외")
    void readMyVideoReport_notCreator() {
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        Member memberCreator = org.mockito.Mockito.mock(Member.class);
        Member otherMember = org.mockito.Mockito.mock(Member.class);
        Video video = org.mockito.Mockito.mock(Video.class);

        given(memberRepository.existsByIdAndOrganizationIdAndStatus(
                memberId, orgId, app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE))
                .willReturn(true);

        given(videoRepository.findByIdAndOrganizationId(videoId, orgId))
                .willReturn(Optional.of(video));

        given(video.getCreator()).willReturn(memberCreator);
        given(memberCreator.getId()).willReturn(999L);

        ApiException ex = assertThrows(ApiException.class,
                () -> historyService.readMyVideoReport(memberId, orgId, videoId));

        assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_CREATE_NOT_FOUND);
    }

    @Test
    @DisplayName("정상 조회 시 구간별 리포트 리스트 반환")
    void readMyVideoReport_success() {
        Long memberId = 1L;
        Long orgId = 10L;
        Long videoId = 100L;

        Member memberCreator = org.mockito.Mockito.mock(Member.class);
        Video video = org.mockito.Mockito.mock(Video.class);

        given(memberRepository.existsByIdAndOrganizationIdAndStatus(
                memberId, orgId, app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE))
                .willReturn(true);

        given(videoRepository.findByIdAndOrganizationId(videoId, orgId))
                .willReturn(Optional.of(video));

        given(video.getCreator()).willReturn(memberCreator);
        given(memberCreator.getId()).willReturn(memberId);

        long wholeTime = 2L * SEGMENT_SECONDS;
        int segCnt = (int) Math.ceil((double) wholeTime / SEGMENT_SECONDS);

        List<Long> viewCounts = LongStream.of(10L, 20L).boxed().collect(Collectors.toList());
        List<Long> quitCounts = LongStream.of(1L, 2L).boxed().collect(Collectors.toList());

        given(video.getWholeTime()).willReturn(wholeTime);
        given(logService.getSegViewCounts(videoId, segCnt)).willReturn(viewCounts);
        given(logService.getSegQuitCounts(videoId, segCnt)).willReturn(quitCounts);
        given(logService.getSegViewCounts(videoId, 2)).willReturn(viewCounts);
        given(logService.getSegQuitCounts(videoId, 2)).willReturn(quitCounts);

        List<VideoIntervalLogItem> result = historyService.readMyVideoReport(memberId, orgId, videoId);

        assertThat(result).hasSize(2);
    }
}
