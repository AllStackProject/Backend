package app.allstackproject.privideo.domain.admin.service;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;

import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.global.util.S3Util;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.comment.repository.CommentRepository;
import app.allstackproject.privideo.domain.history.repository.HistoryRepository;
import app.allstackproject.privideo.domain.quiz.repository.QuizRepository;
import app.allstackproject.privideo.domain.scrap.repository.ScrapRepository;
import app.allstackproject.privideo.domain.video.repository.VideoCategoryMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoAdminService {

    private final VideoRepository videoRepository;
    private final VideoCategoryMappingRepository videoCategoryMappingRepository;
    private final CdnUrlProvider cdnUrlProvider;
    private final S3Util s3Util;
    private final VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final HistoryRepository historyRepository;
    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public List<ReadAllVideoItem> readAllVideos(Long orgId) {
        List<ReadAllVideoItem> allVideoItems = videoRepository.findByOrgId(orgId);
        allVideoItems.forEach(item -> item.setThumbnailUrl(cdnUrlProvider.generateImgUrl(item.getThumbnailUrl())));
        return allVideoItems;
    }

    public boolean deleteVideo(Long orgId, Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!videoRepository.existsByIdAndOrganizationId(videoId, orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        s3Util.deleteFileByKey(video.getThumbnailKey(), true);
        s3Util.deleteFileByKey(video.getVideoKey(), false);

        commentRepository.deleteAllByVideoId(videoId);
        historyRepository.deleteAllByVideoId(videoId);
        quizRepository.deleteAllByVideoId(videoId);
        scrapRepository.deleteAllByVideoId(videoId);

        videoMemberGroupMappingRepository.deleteAllByVideoId(videoId);
        videoCategoryMappingRepository.deleteAllByVideoId(videoId);
        videoRepository.delete(video);

        return true;
    }
}
