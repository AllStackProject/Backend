package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.enumStatus.AiResultType.FEEDBACK;
import static app.allstackproject.privideo.common.enumStatus.AiResultType.NONE;
import static app.allstackproject.privideo.common.enumStatus.AiResultType.QUIZ;
import static app.allstackproject.privideo.common.enumStatus.AiResultType.SUMMARY;
import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.HISTORY_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.IS_NOT_IMAGE_FILE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_WATCHED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_ACCESSIBLE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.service.video.LogService.SEGMENT_SECONDS;

import app.allstackproject.privideo.common.enumStatus.AiResultType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.common.util.S3Util;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import app.allstackproject.privideo.dto.video.CreateVideoRequest;
import app.allstackproject.privideo.dto.video.CreateVideoResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionInfo;
import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.dto.video.VideoInfo;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.scrap.ScrapRepository;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.video.CategoryRepository;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.quiz.QuizRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final OrganizationRepository organizationRepository;
    private final S3Util s3Util;
    private final CdnUrlProvider cdnUrlProvider;

    public JoinVideoSessionResult joinVideoSession(Long memberId, Long orgId, Long videoId) {
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        Video video = videoRepository.findById(videoId)
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
        List<String> categories = categoryRepository.findAllByVideoId(videoId);

        AiResultType aiType = NONE;
        List<QuizInfo> quizInfos = new ArrayList<>();
        String aiFeedback = "", aiSummary = "";

        if (video.getIsAiFunction()) {
            if (video.getAiFeedback() != null) {
                aiType = FEEDBACK;
            } else if (video.getAiSummary() != null) {
                aiType = SUMMARY;
            } else {
                quizInfos = quizRepository.findByVideoId(videoId);
                if (!quizInfos.isEmpty()) {
                    aiType = QUIZ;
                }
            }
        }

        boolean isScrapped = false;
        if (scrapRepository.existsByMemberIdAndVideoId(memberId, videoId)) {
            isScrapped = true;
        }

        List<Long> segViewCnts = logService.getSegViewCounts(videoId,
                (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS));

        boolean isFirstWatch = true;
        Optional<History> history = historyRepository.findByMemberIdAndVideoId(memberId, videoId);
        // 시청 기록 있는지 확인
        if (history.isPresent()) {
            if (history.get().isComplete()) {
                isFirstWatch = false;
                return JoinVideoSessionResult.completed(sessionId, videoInfo, segViewCnts, video.getIsComment(),
                        isScrapped, categories, aiType, quizInfos, aiFeedback, aiSummary);
            }
            logService.incOrgViewBucket(orgId, Instant.now());
        } else {
            // 시청 기록 없다면(=최초 시청) 새로 생성
            History newHistory = History.create(member, video);
            historyRepository.save(newHistory);

            logService.incOrgViewBucket(orgId, Instant.now());

            // TODO: Redis에 해당 멤버 + 재시청 여부 + 영상 아이디에 대해 세션 키 저장
        }

        return JoinVideoSessionResult.create(sessionId, videoInfo, segViewCnts, video.getIsComment(), isScrapped,
                categories, aiType, quizInfos, aiFeedback, aiSummary);
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

        Video video = videoRepository.findById(videoId)
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

        History history = historyRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElseThrow(() -> new ApiException(HISTORY_NOT_FOUND));

        // TODO: Redis에서 세션 키 삭제
        if (isFirstWatch) {
            boolean watchEnd = watchedSegments.testBit(totalSegCnt - 1);
            history.update(leaveVideoSessionInfo.getWatchRate(), leaveVideoSessionInfo.getRecentPosition(), watchEnd);
        }

        history.updateLastWatchedAt();

        logService.incSegViewBucket(videoId, watchedSegments, totalSegCnt);

        if (leaveVideoSessionInfo.getIsQuit()) {
            // recentPositionSec 기준으로만 이탈 판단
            logService.incSegQuitBucket(videoId, leaveVideoSessionInfo.getRecentPosition(), totalSegCnt);
            video.quit();
        }

        return true;
    }

    public List<ReadAllVideoItem> getMemberVideos(Long memberId, Long orgId) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        List<ReadAllVideoItem> allVideoItems = videoRepository.findByOrgIdAndCreatorId(orgId, memberId);
        allVideoItems.forEach(item -> item.setThumbnailUrl(cdnUrlProvider.generateFileUrl(item.getThumbnailUrl())));
        return allVideoItems;
    }

    public CreateVideoResponse createVideo(Long memberId, Long orgId, CreateVideoRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        // 1) 원본 비디오 키 생성 (privideo-original 버킷, 업로드는 presigned URL로)
        //    규칙: org-{orgId}/{UUID}.mp4
        String originalKey = s3Util.generateVideoKey(orgId);

        // 2) 썸네일 키 생성 + 업로드 (privideo-img 버킷)
        //    규칙: org-{orgId}/thumbnail/{UUID}.{ext}
        MultipartFile thumbnailImg = request.getThumbnailImg();
        if (!s3Util.isImageFile(thumbnailImg)) {
            throw new ApiException(IS_NOT_IMAGE_FILE);
        }

        String thumbnailKey = s3Util.generateThumbnailKey(orgId, thumbnailImg.getOriginalFilename());
        s3Util.uploadImgWithKey(thumbnailImg, thumbnailKey);

        // 3) Video 엔티티 저장
        Video video = Video.create(
                organization,
                member,
                request.getTitle(),
                request.getDescription(),
                originalKey,
                thumbnailKey,
                request.getWholeTime(),
                request.getIsComment(),
                !request.getAiFunction().equals("NONE"),
                request.getExpiredAt()
        );
        videoRepository.save(video);

        // 4) HLS Prefix 계산해서 엔티티에 반영
        //    규칙: hls/org-{orgId}/{originalKey}
        String hlsPrefix = s3Util.generateHlsPrefix(orgId, originalKey);
        video.setHlsPrefix(hlsPrefix);

        // 5) 업로드용 Pre-signed URL 생성 (privideo-original, inputBucket)
        //    클라이언트가 이 URL로 비디오 파일 업로드
        URL presignedUrl = s3Util.generatePresignedUploadUrl(originalKey);

        // 6) 응답: presigned URL + 원본 비디오 키
        return CreateVideoResponse.of(presignedUrl.toString());
    }
}
