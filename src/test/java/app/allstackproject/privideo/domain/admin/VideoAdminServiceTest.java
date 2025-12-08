package app.allstackproject.privideo.domain.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.admin.service.VideoAdminService;
import app.allstackproject.privideo.domain.comment.repository.CommentRepository;
import app.allstackproject.privideo.domain.history.repository.HistoryRepository;
import app.allstackproject.privideo.domain.quiz.repository.QuizRepository;
import app.allstackproject.privideo.domain.scrap.repository.ScrapRepository;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.video.repository.VideoCategoryMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.global.util.S3Util;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VideoAdminServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private VideoCategoryMappingRepository videoCategoryMappingRepository;

    @Mock
    private CdnUrlProvider cdnUrlProvider;

    @Mock
    private S3Util s3Util;

    @Mock
    private VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ScrapRepository scrapRepository;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private VideoAdminService videoAdminService;

    @Test
    @DisplayName("readAllVideos - 썸네일 키를 CDN URL로 변환해서 반환한다")
    void readAllVideos_success() {
        // given
        Long orgId = 1L;

        ReadAllVideoItem item = new ReadAllVideoItem(
                1L,
                "테스트 영상",
                "thumb-key",
                LocalDateTime.now(),
                LocalDate.now().plusDays(1),
                "PUBLIC",
                10L
        );

        when(videoRepository.findByOrgId(orgId))
                .thenReturn(List.of(item));
        when(cdnUrlProvider.generateImgUrl("thumb-key"))
                .thenReturn("https://cdn.example.com/thumb-key");

        // when
        List<ReadAllVideoItem> result = videoAdminService.readAllVideos(orgId);

        // then
        assertEquals(1, result.size());
        ReadAllVideoItem resultItem = result.get(0);
        assertEquals("테스트 영상", resultItem.getTitle());
        assertEquals("https://cdn.example.com/thumb-key", resultItem.getThumbnailUrl());

        verify(videoRepository).findByOrgId(orgId);
        verify(cdnUrlProvider).generateImgUrl("thumb-key");
    }

    @Test
    @DisplayName("deleteVideo - 영상이 존재하지 않으면 ApiException(VIDEO_NOT_FOUND)")
    void deleteVideo_videoNotFound_throwsException() {
        // given
        Long orgId = 1L;
        Long videoId = 10L;

        when(videoRepository.findById(videoId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(ApiException.class,
                () -> videoAdminService.deleteVideo(orgId, videoId));

        verify(videoRepository).findById(videoId);
        verify(videoRepository, never()).existsByIdAndOrganizationId(anyLong(), anyLong());
        verifyNoInteractions(s3Util, commentRepository, historyRepository,
                quizRepository, scrapRepository,
                videoMemberGroupMappingRepository, videoCategoryMappingRepository);
    }

    @Test
    @DisplayName("deleteVideo - 영상이 해당 조직에 속해있지 않으면 ApiException(VIDEO_NOT_IN_ORGANIZATION)")
    void deleteVideo_videoNotInOrg_throwsException() {
        // given
        Long orgId = 1L;
        Long videoId = 10L;

        Video video = mock(Video.class);
        when(videoRepository.findById(videoId))
                .thenReturn(Optional.of(video));
        when(videoRepository.existsByIdAndOrganizationId(videoId, orgId))
                .thenReturn(false);

        // when & then
        assertThrows(ApiException.class,
                () -> videoAdminService.deleteVideo(orgId, videoId));

        verify(videoRepository).findById(videoId);
        verify(videoRepository).existsByIdAndOrganizationId(videoId, orgId);
        verifyNoInteractions(s3Util, commentRepository, historyRepository,
                quizRepository, scrapRepository,
                videoMemberGroupMappingRepository, videoCategoryMappingRepository);
    }

    @Test
    @DisplayName("deleteVideo - S3 파일과 관련 데이터 삭제 후 Video 삭제 성공")
    void deleteVideo_success() {
        // given
        Long orgId = 1L;
        Long videoId = 10L;

        Video video = mock(Video.class);
        when(video.getThumbnailKey()).thenReturn("thumb-key");
        when(video.getVideoKey()).thenReturn("video-key");

        when(videoRepository.findById(videoId))
                .thenReturn(Optional.of(video));
        when(videoRepository.existsByIdAndOrganizationId(videoId, orgId))
                .thenReturn(true);

        // when
        boolean result = videoAdminService.deleteVideo(orgId, videoId);

        // then
        assertTrue(result);

        verify(s3Util).deleteFileByKey("thumb-key", true);
        verify(s3Util).deleteFileByKey("video-key", false);

        verify(commentRepository).deleteAllByVideoId(videoId);
        verify(historyRepository).deleteAllByVideoId(videoId);
        verify(quizRepository).deleteAllByVideoId(videoId);
        verify(scrapRepository).deleteAllByVideoId(videoId);

        verify(videoMemberGroupMappingRepository).deleteAllByVideoId(videoId);
        verify(videoCategoryMappingRepository).deleteAllByVideoId(videoId);

        verify(videoRepository).delete(video);
    }
}