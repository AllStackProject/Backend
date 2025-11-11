package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.dto.history.HistoryResponse;
import app.allstackproject.privideo.dto.history.VideoHistory;
import app.allstackproject.privideo.repository.history.HistoryRepository;
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
        List<VideoHistory> histories = historyRepository.findByMemberIdAndOrganizationId(memberId, orgId);
        return HistoryResponse.of(histories);
    }
}

