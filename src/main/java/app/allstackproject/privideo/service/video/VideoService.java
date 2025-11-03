package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_WATCHED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_ACCESSIBLE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.video.CommentInfo;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.dto.video.VideoInfo;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.CommentRepository;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.video.HashtagRepository;
import app.allstackproject.privideo.repository.HistoryRepository;
import app.allstackproject.privideo.repository.QuizRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final VideoRepository videoRepository;
    private final HistoryRepository historyRepository;
    private final LogService logService;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;
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

        // TODO: Redis에서 해당 member, video에 대해 열려있는 세션이 있는지 확인
        boolean sessionExists = true;
        if (sessionExists) {
            throw new ApiException(VIDEO_ALREADY_WATCHED);
        }

        if (!memberGroupRepository.isAccessibleToVideo(memberId, videoId)) {
            throw new ApiException(VIDEO_NOT_ACCESSIBLE);
        }

        video.watch();

        VideoInfo videoInfo = VideoInfo.from(video);
        List<CommentInfo> commentInfos = commentRepository.findAllByVideoId(videoId);
        List<QuizInfo> quizInfos = quizRepository.findAllByVideoId(videoId);
        List<String> hashtags = hashtagRepository.findAllByVideoId(videoId);

        boolean isFirstWatch = true;
        Optional<History> history = historyRepository.findByMemberIdAndVideoIdAndStatus(memberId, videoId, ACTIVE);
        // 시청 기록 있는지 확인
        if (history.isPresent()) {
            if (history.get().isComplete()) {
                isFirstWatch = false;
                return JoinVideoSessionResult.completed(sessionId, videoInfo, commentInfos.isEmpty(), hashtags,
                        commentInfos, quizInfos);
            }
            logService.incOrgViewBucket(orgId, Instant.now());
        } else {
            // 시청 기록 없다면(=최초 시청) 새로 생성
            History newHistory = History.create(member, video);
            historyRepository.save(newHistory);

            logService.incOrgViewBucket(orgId, Instant.now());

            // TODO: Redis에 해당 멤버 + 재시청 여부에 대해 세션 키 저장
        }

        return JoinVideoSessionResult.create(sessionId, videoInfo, commentInfos.isEmpty(), hashtags, commentInfos,
                quizInfos);
    }
}
