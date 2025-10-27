package app.allstackproject.privideo.service;

import app.allstackproject.privideo.dto.ScrapResponse;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.HistoryRepository;
import app.allstackproject.privideo.repository.ScrapRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyScrapService {

    private final ScrapRepository scrapRepository;
    private final HistoryRepository historyRepository;

    public ScrapResponse getUserScraps(Long memberId, Long orgId) {

        List<Scrap> scrapList = scrapRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);
        List<History> historyList = historyRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);

        Map<Long, History> historyMapByVideoId = historyList.stream()
                .collect(Collectors.toMap(
                        history -> history.getVideo().getId(),
                        history -> history
                ));

        return ScrapResponse.of(scrapList, historyMapByVideoId);
    }
}
