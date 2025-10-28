package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.dto.video.HistoryResponse;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyVideoService {

    private final HistoryRepository historyRepository;

    public HistoryResponse getUserVideos(Long memberId, Long orgId) {
        List<History> histories = historyRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);
        return HistoryResponse.of(histories);
    }
}

