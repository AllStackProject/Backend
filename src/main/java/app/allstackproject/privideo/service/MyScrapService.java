package app.allstackproject.privideo.service;

import app.allstackproject.privideo.dto.UserScrapResponse;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.UserHistoryRepository;
import app.allstackproject.privideo.repository.UserScrapRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyScrapService {

    private final UserScrapRepository userScrapRepository;
    private final UserHistoryRepository userHistoryRepository;
    
    public UserScrapResponse getUserScraps(Long memberId) {

        List<Scrap> scrapList = userScrapRepository.findByMemberId(memberId);
        List<History> historyList = userHistoryRepository.findByMemberId(memberId);

        Map<Long, History> historyMapByVideoId = historyList.stream()
                .collect(Collectors.toMap(
                        history -> history.getVideo().getId(),
                        history -> history
                ));

        return UserScrapResponse.of(scrapList, historyMapByVideoId);
    }
}
