package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.AllMemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllMemberItem;
import app.allstackproject.privideo.dto.admin.VideoWatchLogItem;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StatsAdminService {

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;
    private final VideoRepository videoRepository;

    public List<AllMemberWatchLogItem> readAllMemberWatchLog(Long orgId) {
        List<ReadAllMemberItem> members = memberRepository.findByOrganizationId(orgId);

        Map<Long, Long> avgWatchRateMap = historyRepository.findAvgWatchRateByOrgId(orgId)
                .stream()
                .collect(toMap(
                        MemberAvgWatchRateDto::getMemberId,
                        MemberAvgWatchRateDto::getAvgWatchRate
                ));

        return members.stream()
                .map(member -> {
                    List<String> groupNames = member.getMemberGroups() == null
                            ? List.of()
                            : member.getMemberGroups().stream()
                                    .map(MemberGroupItem::getName)
                                    .collect(toList());

                    Long avgWatchRate = avgWatchRateMap.getOrDefault(member.getId(), 0L);

                    return new AllMemberWatchLogItem(
                            member.getId(),
                            member.getNickname(),
                            groupNames,
                            avgWatchRate
                    );
                })
                .collect(toList());
    }

    public List<MemberWatchLogItem> readMemberWatchLog(Long orgId, Long memberId) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        return historyRepository.findWatchLogByMemberId(memberId);
    }

    public List<AllVideoWatchLogItem> readAllVideoWatchLog(Long orgId) {
        return historyRepository.findAllVideoWatchLogByOrgId(orgId);
    }

    public List<VideoWatchLogItem> readVideoWatchLog(Long orgId, Long videoId) {
        if (!videoRepository.existsByIdAndOrganizationId(videoId, orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        return historyRepository.findVideoWatchLogByVideoId(videoId);
    }
}
