package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.HISTORY_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_WATCHED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_ACCESSIBLE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.service.video.LogService.SEGMENT_SECONDS;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionInfo;
import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.dto.video.VideoInfo;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.scrap.ScrapRepository;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.video.CategoryRepository;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.quiz.QuizRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final VideoRepository videoRepository;
    private final HistoryRepository historyRepository;
    private final LogService logService;
    private final CategoryRepository categoryRepository;
    private final ScrapRepository scrapRepository;
    private final QuizRepository quizRepository;

    public JoinVideoSessionResult joinVideoSession(Long memberId, Long orgId, Long videoId) {
        Member member = memberRepository.findByIdAndStatus(memberId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));
        if (!member.getOrganization().getId().equals(orgId)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        Video video = videoRepository.findByIdAndStatus(videoId, ACTIVE)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        String sessionId = UUID.nameUUIDFromBytes((memberId.toString()).getBytes(StandardCharsets.UTF_8)).toString();

        // TODO: Redis에서 해당 member에 대해 열려있는 세션이 있는지 확인
        boolean sessionExists = false;
        if (sessionExists) {
            throw new ApiException(VIDEO_ALREADY_WATCHED);
        }

        if (!memberGroupRepository.isAccessibleToVideo(memberId, videoId)) {
            throw new ApiException(VIDEO_NOT_ACCESSIBLE);
        }

        video.watch();

        VideoInfo videoInfo = VideoInfo.from(video);
        List<QuizInfo> quizInfos = quizRepository.findByVideoId(videoId);
        List<String> categories = categoryRepository.findAllByVideoId(videoId);

        boolean isScrapped = false;
        if (scrapRepository.existsByMemberIdAndVideoId(memberId, videoId)) {
            isScrapped = true;
        }

        List<Long> segViewCnts = logService.getSegViewCounts(videoId,
                (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS));

        boolean isFirstWatch = true;
        Optional<History> history = historyRepository.findByMemberIdAndVideoIdAndStatus(memberId, videoId, ACTIVE);
        // 시청 기록 있는지 확인
        if (history.isPresent()) {
            if (history.get().isComplete()) {
                isFirstWatch = false;
                return JoinVideoSessionResult.completed(sessionId, videoInfo, segViewCnts, video.isComment(),
                        isScrapped, categories, quizInfos);
            }
            logService.incOrgViewBucket(orgId, Instant.now());
        } else {
            // 시청 기록 없다면(=최초 시청) 새로 생성
            History newHistory = History.create(member, video);
            historyRepository.save(newHistory);

            logService.incOrgViewBucket(orgId, Instant.now());

            // TODO: Redis에 해당 멤버 + 재시청 여부 + 영상 아이디에 대해 세션 키 저장
        }

        return JoinVideoSessionResult.create(sessionId, videoInfo, segViewCnts, video.isComment(), isScrapped,
                categories, quizInfos);
    }

    public boolean leaveVideoSession(LeaveVideoSessionInfo leaveVideoSessionInfo) {
        Long memberId = leaveVideoSessionInfo.getMemberId();
        Long orgId = leaveVideoSessionInfo.getOrgId();
        Long videoId = leaveVideoSessionInfo.getVideoId();

        Member member = memberRepository.findByIdAndStatus(memberId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));
        if (!member.getOrganization().getId().equals(orgId)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        Video video = videoRepository.findByIdAndStatus(videoId, ACTIVE)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        /**
         * TODO: Redis에서 기존 재생 정보 확인
         * 1. 해당 member에 대해 열려있는 세션이 없다면 SESSION_NOT_FOUND 예외 발생
         * 2. 시청 join했던 영상과 다르면 INVALID_REQUEST 예외 발생 (=시청 시작한 적 없는 영상에 대해 종료 요청)
         * 3. 위 경우들에 해당하지 않는다면 올바른 요청
         */

        // TODO: Redis에서 재시청인지 확인
        boolean isFirstWatch = true;
        BigInteger watchedSegments = new BigInteger(leaveVideoSessionInfo.getWatchSegments(), 2);
        int totalSegCnt = (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS);

        // TODO: Redis에서 세션 키 삭제
        if (isFirstWatch) {
            History history = historyRepository.findByMemberIdAndVideoIdAndStatus(memberId, videoId, ACTIVE)
                    .orElseThrow(() -> new ApiException(HISTORY_NOT_FOUND));

            boolean watchEnd = watchedSegments.testBit(totalSegCnt - 1);
            history.update(leaveVideoSessionInfo.getWatchRate(), leaveVideoSessionInfo.getRecentPosition(), watchEnd);
        }

        logService.incSegViewBucket(videoId, watchedSegments, totalSegCnt);

        if (leaveVideoSessionInfo.getIsQuit()) {
            // recentPositionSec 기준으로만 이탈 판단
            logService.incSegQuitBucket(videoId, leaveVideoSessionInfo.getRecentPosition(), totalSegCnt);
            video.quit();
        }

        return true;
    }
}
