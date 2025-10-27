package app.allstackproject.privideo.service;

import app.allstackproject.privideo.dto.UserHistoryResponse;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.repository.UserHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyVideoService {

    private final UserHistoryRepository userHistoryRepository;

    public UserHistoryResponse getUserVideos(Long userId) {
        List<History> histories = userHistoryRepository.findByMemberId(userId);
        return UserHistoryResponse.of(histories);
    }
}

