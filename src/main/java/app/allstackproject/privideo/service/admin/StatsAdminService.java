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
import app.allstackproject.privideo.entity.OrgViewLog;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StatsAdminService {

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;
    private final VideoRepository videoRepository;
    private final MongoTemplate mongoTemplate;

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

    public List<Long> readDayWatchCompleteCnt(Long orgId, String standardMonth) {
        YearMonth yearMonth = YearMonth.parse(standardMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Query query = Query.query(Criteria.where("orgId").is(orgId)
                .and("date").gte(startDate.atStartOfDay()).lt(endDate.plusDays(1).atStartOfDay()));

        List<OrgViewLog> logs = mongoTemplate.find(query, OrgViewLog.class);

        Map<DayOfWeek, Long> dayOfWeekMap = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getDate().toLocalDate().getDayOfWeek(),
                        Collectors.summingLong(this::calculateTotalViews)
                ));

        return Stream.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                )
                .map(day -> dayOfWeekMap.getOrDefault(day, 0L))
                .collect(Collectors.toList());
    }

    public List<Long> readHourWatchCompleteCnt(Long orgId, String standardMonth) {
        YearMonth yearMonth = YearMonth.parse(standardMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Query query = Query.query(Criteria.where("orgId").is(orgId)
                .and("date").gte(startDate.atStartOfDay())
                .lt(endDate.plusDays(1).atStartOfDay()));

        List<OrgViewLog> logs = mongoTemplate.find(query, OrgViewLog.class);

        Map<String, Long> bucketSumMap = logs.stream()
                .map(OrgViewLog::getBuckets)
                .filter(buckets -> buckets != null)
                .flatMap(buckets -> buckets.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(e -> e.getValue().longValue())
                ));

        List<String> bucketOrder = List.of(
                "06-09",
                "09-12",
                "12-15",
                "15-18",
                "18-21",
                "21-24",
                "00-03",
                "03-06"
        );

        return bucketOrder.stream()
                .map(bucket -> bucketSumMap.getOrDefault(bucket, 0L))
                .collect(Collectors.toList());
    }

    private Long calculateTotalViews(OrgViewLog log) {
        return log.getBuckets().values().stream()
                .mapToLong(Integer::longValue)
                .sum();
    }
}
