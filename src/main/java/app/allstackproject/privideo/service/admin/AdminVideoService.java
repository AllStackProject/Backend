package app.allstackproject.privideo.service.admin;

import app.allstackproject.privideo.dto.admin.ReadAllVideoDto;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminVideoService {

    private final VideoRepository videoRepository;

    @Transactional(readOnly = true)
    public List<ReadAllVideoDto> readAllVideos(Long orgId) {
        return videoRepository.findByOrgId(orgId);
    }
}
