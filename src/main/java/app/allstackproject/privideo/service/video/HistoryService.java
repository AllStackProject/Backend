package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.history.HistoryResponse;
import app.allstackproject.privideo.dto.history.VideoHistory;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public HistoryResponse getUserVideos(Long memberId, Long orgId) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        List<VideoHistory> histories = historyRepository.findByMemberId(memberId);
        return HistoryResponse.of(histories);
    }
}

