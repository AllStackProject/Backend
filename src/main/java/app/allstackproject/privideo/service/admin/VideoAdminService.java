package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
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
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<ReadAllVideoItem> readAllVideos(Long orgId) {
        return videoRepository.findByOrgId(orgId);
    }

    public boolean deleteVideo(Long orgId, Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!videoRepository.existsByIdAndOrganizationId(videoId, orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        video.updateToInactive();
        return true;
    }
}
