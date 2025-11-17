package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.util.TimeUtil.calculateStartDate;
import static app.allstackproject.privideo.service.video.LogService.SEGMENT_SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.AllMemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.GroupWatchCompleteRate;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.MemberWatchReport;
import app.allstackproject.privideo.dto.admin.MonthlyWatchItem;
import app.allstackproject.privideo.dto.admin.QuitLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllMemberItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.dto.admin.VideoIntervalLogItem;
import app.allstackproject.privideo.dto.admin.VideoWatchLogItem;
import app.allstackproject.privideo.entity.OrgViewLog;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import app.allstackproject.privideo.service.video.LogService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private final LogService logService;

    public List<AllMemberWatchLogItem> readAllMemberWatchLog(Long orgId) {
        List<ReadAllMemberItem> members = memberRepository.findByOrganizationId(orgId);

        Map<Long, Long> avgWatchRateMap = historyRepository.findMemberAvgWatchRateByOrgId(orgId)
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

    public MemberWatchReport readMemberWatchReport(Long orgId, Long memberId) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        LocalDateTime startDate = calculateStartDate(3);
        LocalDateTime endDate = LocalDateTime.now();

        Long totalCount = historyRepository.countByMemberIdAndIsCompleteIsTrueAndCompletedAtBetween(memberId, startDate,
                endDate);

        List<String> topCategories = historyRepository.findTopCategoriesByMemberIdWithinPeriod(memberId, startDate,
                endDate);
        List<MonthlyWatchItem> monthlyStats = historyRepository.findMonthlyStatsByMemberIdWithinPeriod(memberId,
                startDate, endDate);

        return MemberWatchReport.builder()
                .totalWatchedVideoCnt(totalCount)
                .mostWatchedCategories(topCategories)
                .monthlyWatchedCnts(monthlyStats)
                .build();
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
                "00-03",
                "03-06",
                "06-09",
                "09-12",
                "12-15",
                "15-18",
                "18-21",
                "21-24"
        );

        return bucketOrder.stream()
                .map(bucket -> bucketSumMap.getOrDefault(bucket, 0L))
                .collect(Collectors.toList());
    }

    public List<GroupWatchCompleteRate> readGroupWatchCompleteLog(Long orgId, String standardMonth) {
        YearMonth yearMonth = YearMonth.parse(standardMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return historyRepository.findGroupAvgWatchRateByOrgIdWithinPeriod(orgId, startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay());
    }

    private Long calculateTotalViews(OrgViewLog log) {
        if (log.getBuckets() == null) {
            return 0L;
        }

        return log.getBuckets().values().stream()
                .mapToLong(Integer::longValue)
                .sum();
    }

    public List<ReadAllVideoIntervalLogItem> readAllVideoIntervalLog(Long orgId) {
        return videoRepository.findAllVideoIntervalLogByOrgId(orgId);
    }

    public List<VideoIntervalLogItem> readVideoIntervalLog(Long orgId, Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_IN_ORGANIZATION));
        int totalSegCnt = (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS);

        List<Long> viewCounts = logService.getSegViewCounts(videoId, totalSegCnt);
        List<Long> quitCounts = logService.getSegQuitCounts(videoId, totalSegCnt);

        Long totalViews = viewCounts.stream().mapToLong(Long::longValue).sum();
        Long totalQuits = quitCounts.stream().mapToLong(Long::longValue).sum();

        List<VideoIntervalLogItem> intervals = IntStream.range(0, totalSegCnt)
                .mapToObj(i -> {
                    Long views = i < viewCounts.size() ? viewCounts.get(i) : 0L;
                    Long quits = i < quitCounts.size() ? quitCounts.get(i) : 0L;

                    Long viewRate = totalViews > 0 ? (views * 100) / totalViews : 0L;

                    Long quitRate = totalQuits > 0 ? (quits * 100) / totalQuits : 0L;

                    return new VideoIntervalLogItem((long) i, views, quits, viewRate, quitRate);
                })
                .collect(Collectors.toList());

        return intervals;
    }

    public List<QuitLogItem> readQuitLog(Long orgId) {
        int limit = 3;
        List<QuitLogItem> result = new ArrayList<>();

        result.addAll(videoRepository.findTopQuitRateVideosByOrgId(orgId, limit));
        result.addAll(videoRepository.findLowQuitRateVideosByOrgId(orgId, limit));

        return result;
    }
}
