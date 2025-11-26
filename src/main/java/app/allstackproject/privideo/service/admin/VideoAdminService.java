package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.video.VideoCategoryMappingRepository;
import app.allstackproject.privideo.repository.video.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
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
    private final VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;

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

        videoMemberGroupMappingRepository.deleteAllByVideoId(videoId);
        videoCategoryMappingRepository.deleteAllByVideoId(videoId);
        videoRepository.delete(video);

        // TODO: S3에 저장된 파일도 지워야 함
        return true;
    }
}
