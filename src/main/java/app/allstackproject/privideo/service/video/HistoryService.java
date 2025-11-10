package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.dto.video.HistoryResponse;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final HistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public HistoryResponse getUserVideos(Long memberId, Long orgId) {
        List<History> histories = historyRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);
        return HistoryResponse.of(histories);
    }
}

