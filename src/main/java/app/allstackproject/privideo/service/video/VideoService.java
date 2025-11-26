package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.enumStatus.AiFunctionType.FEEDBACK;
import static app.allstackproject.privideo.common.enumStatus.AiFunctionType.NONE;
import static app.allstackproject.privideo.common.enumStatus.AiFunctionType.QUIZ;
import static app.allstackproject.privideo.common.enumStatus.AiFunctionType.SUMMARY;
import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.S3ImgType.THUMBNAIL;
import static app.allstackproject.privideo.common.enumStatus.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.common.enumStatus.UploadStatusType.FAIL;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CATEGORY_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.HISTORY_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_AIRFLOW_STATUS;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_MEMBER_GROUP_IDS;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_PLAY_SESSION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.IS_NOT_IMAGE_FILE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.NOT_ALLOWED_MEMBER_GROUP_ACCESS;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.PLAY_SESSION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_WATCHING;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_CREATE_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_ACCESSIBLE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.service.video.LogService.SEGMENT_SECONDS;

import app.allstackproject.privideo.common.enumStatus.AiFunctionType;
import app.allstackproject.privideo.common.enumStatus.UploadStatusType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.common.util.S3Util;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import app.allstackproject.privideo.dto.video.CreateVideoRequest;
import app.allstackproject.privideo.dto.video.CreateVideoResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionInfo;
import app.allstackproject.privideo.dto.video.ModifyVideoRequest;
import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.dto.video.VideoInfo;
import app.allstackproject.privideo.entity.Category;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.entity.VideoCategoryMapping;
import app.allstackproject.privideo.entity.VideoMemberGroupMapping;
import app.allstackproject.privideo.repository.comment.CommentRepository;
import app.allstackproject.privideo.repository.member.MemberGroupMappingRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.scrap.ScrapRepository;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.video.CategoryRepository;
import app.allstackproject.privideo.repository.history.HistoryRepository;
import app.allstackproject.privideo.repository.quiz.QuizRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoCategoryMappingRepository;
import app.allstackproject.privideo.repository.video.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.repository.video.VideoRedisRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    private final OrganizationRepository organizationRepository;
    private final S3Util s3Util;
    private final CdnUrlProvider cdnUrlProvider;
    private final VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;
    private final VideoCategoryMappingRepository videoCategoryMappingRepository;
    private final QuizRepository quizRepository;
    private final CommentRepository commentRepository;
    private final AiFunctionService aiFunctionService;
    private final MemberGroupMappingRepository memberGroupMappingRepository;
    private final VideoRedisRepository videoRedisRepository;

    public JoinVideoSessionResult joinVideoSession(Long memberId, Long orgId, Long videoId) {
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        String sessionId = UUID.nameUUIDFromBytes(
                (memberId.toString() + videoId.toString()).getBytes(StandardCharsets.UTF_8)).toString();

        if (videoRedisRepository.existsWatchSession(sessionId)) {
            throw new ApiException(VIDEO_ALREADY_WATCHING);
        }

        if (!memberGroupRepository.isAccessibleToVideo(memberId, videoId)) {
            throw new ApiException(VIDEO_NOT_ACCESSIBLE);
        }

        videoRedisRepository.createWatchSession(sessionId, memberId);
        video.watch();

        String playbackUrl = s3Util.generatePlaybackUrl(video.getHlsPrefix());

        VideoInfo videoInfo = VideoInfo.from(video);

        List<Long> segViewCnts = logService.getSegViewCounts(videoId,
                (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS));

        boolean isScrapped = scrapRepository.existsByMemberIdAndVideoId(memberId, videoId);
        List<String> categories = categoryRepository.findAllByVideoId(videoId);

        AiFunctionType aiType = video.getAiFunctionType();
        List<QuizInfo> quizInfos = new ArrayList<>();
        String aiFeedback = "", aiSummary = "";

        if (aiType.equals(SUMMARY)) {
            aiSummary = video.getAiSummary();
        } else if (aiType.equals(FEEDBACK)) {
            aiFeedback = video.getAiFeedback();
        } else if (aiType.equals(QUIZ)) {
            quizInfos = quizRepository.findAllByVideoId(videoId);
        }

        Optional<History> history = historyRepository.findByMemberIdAndVideoId(memberId, videoId);
        if (history.isPresent()) {
            if (history.get().isComplete()) {
                return JoinVideoSessionResult.completed(sessionId, playbackUrl, videoInfo, segViewCnts,
                        video.getIsComment(), isScrapped, categories, aiType, quizInfos, aiFeedback, aiSummary);
            }
        } else {
            History newHistory = History.create(member, video);
            historyRepository.save(newHistory);
        }

        logService.incOrgViewBucket(orgId, Instant.now());

        return JoinVideoSessionResult.create(sessionId, playbackUrl, videoInfo, segViewCnts, video.getIsComment(),
                isScrapped, categories, aiType, quizInfos, aiFeedback, aiSummary);
    }

    public boolean leaveVideoSession(LeaveVideoSessionInfo leaveVideoSessionInfo) {
        String sessionId = leaveVideoSessionInfo.getSessionId();
        if (!videoRedisRepository.existsWatchSession(sessionId)) {
            throw new ApiException(PLAY_SESSION_NOT_FOUND);
        }

        Long memberId = videoRedisRepository.getMemberIdByWatchSession(sessionId);
        Long videoId = leaveVideoSessionInfo.getVideoId();

        String sessionIdForValidate = UUID.nameUUIDFromBytes(
                (memberId.toString() + videoId.toString()).getBytes(StandardCharsets.UTF_8)).toString();
        if (!sessionId.equals(sessionIdForValidate)) {
            throw new ApiException(INVALID_PLAY_SESSION);
        }

        Long orgId = leaveVideoSessionInfo.getOrgId();
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

        videoRedisRepository.deleteWatchSession(sessionId);

        History history = historyRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElseThrow(() -> new ApiException(HISTORY_NOT_FOUND));

        BigInteger watchedSegments = new BigInteger(leaveVideoSessionInfo.getWatchSegments(), 2);
        int totalSegCnt = (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS);

        if (!history.isComplete()) {
            boolean hadEnd = watchedSegments.testBit(totalSegCnt - 1);
            history.update(leaveVideoSessionInfo.getWatchRate(), leaveVideoSessionInfo.getRecentPosition(), hadEnd);
        }

        history.updateLastWatchedAt();

        logService.incSegViewBucket(videoId, watchedSegments, totalSegCnt);

        if (leaveVideoSessionInfo.getIsQuit()) {
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
        allVideoItems.forEach(item -> item.setThumbnailUrl(cdnUrlProvider.generateImgUrl(item.getThumbnailUrl())));
        return allVideoItems;
    }

    public CreateVideoResponse createVideo(Long memberId, Long orgId, CreateVideoRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        AiFunctionType aiFunction = AiFunctionType.from(request.getAiFunction());

        // 1) 원본 비디오 키 생성 (privideo-original 버킷, 업로드는 presigned URL로)
        //    규칙: org-{orgId}/{UUID}/original.mp4
        String originalKey = s3Util.generateVideoKey(orgId);

        // 2) 썸네일 키 생성 + 업로드 (privideo-img 버킷)
        //    규칙: images/org-{orgId}/thumbnail/{UUID}.{ext}
        MultipartFile thumbnailImg = request.getThumbnailImg();
        if (!s3Util.isImageFile(thumbnailImg)) {
            throw new ApiException(IS_NOT_IMAGE_FILE);
        }

        String thumbnailKey = s3Util.generateImgKey(orgId, thumbnailImg.getOriginalFilename(), THUMBNAIL);
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
                aiFunction,
                request.getExpiredAt()
        );
        videoRepository.save(video);

        List<Long> reqMemberGroups = request.getMemberGroups();
        List<Long> reqCategories = request.getCategories();

        Set<Long> myGroupIds = memberGroupMappingRepository.findAllByMemberId(memberId)
                .stream()
                .map(mapping -> mapping.getMemberGroup().getId())
                .collect(Collectors.toSet());

        List<MemberGroup> groups = memberGroupRepository.findAllById(reqMemberGroups);
        if (groups.size() != reqMemberGroups.size()) {
            throw new ApiException(INVALID_MEMBER_GROUP_IDS);
        }

        for (MemberGroup group : groups) {
            if (!group.getOrganization().getId().equals(orgId)) {
                throw new ApiException(MEMBER_GROUP_NOT_IN_ORGANIZATION);
            }
            if (!myGroupIds.contains(group.getId())) {
                throw new ApiException(NOT_ALLOWED_MEMBER_GROUP_ACCESS);
            }
        }

        List<Category> categories = categoryRepository.findAllById(reqCategories);
        if (categories.size() != reqCategories.size()) {
            throw new ApiException(CATEGORY_NOT_FOUND);
        }

        for (Category category : categories) {
            Long categoryGroupId = category.getMemberGroupId();
            if (categoryGroupId != null && !reqMemberGroups.contains(categoryGroupId)) {
                throw new ApiException(CATEGORY_NOT_FOUND);
            }
        }

        List<VideoMemberGroupMapping> groupMappings = groups.stream()
                .map(group -> VideoMemberGroupMapping.create(video, group))
                .toList();
        videoMemberGroupMappingRepository.saveAll(groupMappings);

        List<VideoCategoryMapping> categoryMappings = categories.stream()
                .map(category -> VideoCategoryMapping.create(video, category))
                .toList();
        videoCategoryMappingRepository.saveAll(categoryMappings);

        // 4) HLS Prefix 계산해서 엔티티에 반영
        //    규칙: hls/{originalKey}
        String hlsPrefix = s3Util.generateHlsPrefix(originalKey);
        video.setHlsPrefix(hlsPrefix);

        // 5) 업로드용 URL 생성
        URL presignedUrl = s3Util.generatePresignedUploadUrl(originalKey);

        return CreateVideoResponse.of(presignedUrl.toString(), video.getId());
    }

    public SuccessResponse updateVideoEncodingResult(Long orgId, String videoUuid, String status) {
        String videoKey = s3Util.composeVideoKey(orgId, videoUuid);
        Video video = videoRepository.findByVideoKey(videoKey)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        Long videoId = video.getId();

        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        if (status.equals("SUCCESS")) {
            AiFunctionType aiFunction = video.getAiFunctionType();
            if (!aiFunction.equals(NONE)) {
                log.info("AI 기능 처리 시작: videoId={}, function={}", videoId, aiFunction);
                aiFunctionService.processAiFunction(videoId, video.getHlsPrefix(), aiFunction);
            }

            video.setUploadStatus(COMPLETE);
        } else if (status.equals("FAILED")) {
            videoMemberGroupMappingRepository.deleteAllByVideoId(videoId);
            videoCategoryMappingRepository.deleteAllByVideoId(videoId);
            video.setUploadStatus(FAIL);

            s3Util.deleteFileByKey(video.getVideoKey(), false);
            s3Util.deleteFileByKey(video.getThumbnailKey(), true);
        } else {
            throw new ApiException(INVALID_AIRFLOW_STATUS);
        }

        return SuccessResponse.of(true);
    }

    public UploadStatusType readVideoEncodingResult(Long memberId, Long orgId, Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }
        if (!video.getCreator().getId().equals(memberId)) {
            throw new ApiException(VIDEO_CREATE_NOT_FOUND);
        }

        UploadStatusType uploadStatus = video.getUploadStatus();

        if (uploadStatus.equals(FAIL)) {
            videoRepository.delete(video);
        }

        return uploadStatus;
    }

    public boolean modifyVideo(Long orgId, Long memberId, Long videoId, ModifyVideoRequest modifyVideoRequest) {
        Video video = videoRepository.findByIdAndOrganizationId(videoId, orgId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getCreator().getId().equals(memberId)) {
            throw new ApiException(VIDEO_CREATE_NOT_FOUND);
        }

        List<Long> reqMemberGroups = modifyVideoRequest.getMemberGroups();
        List<Long> reqCategories = modifyVideoRequest.getCategories();

        Set<Long> myGroupIds = memberGroupMappingRepository.findAllByMemberId(memberId)
                .stream()
                .map(mapping -> mapping.getMemberGroup().getId())
                .collect(Collectors.toSet());

        List<MemberGroup> groups = memberGroupRepository.findAllById(reqMemberGroups);
        if (groups.size() != reqMemberGroups.size()) {
            throw new ApiException(INVALID_MEMBER_GROUP_IDS);
        }

        for (MemberGroup group : groups) {
            if (!group.getOrganization().getId().equals(orgId)) {
                throw new ApiException(MEMBER_GROUP_NOT_IN_ORGANIZATION);
            }

            if (!myGroupIds.contains(group.getId())) {
                throw new ApiException(NOT_ALLOWED_MEMBER_GROUP_ACCESS);
            }
        }

        List<Category> categories = categoryRepository.findAllById(reqCategories);
        if (categories.size() != reqCategories.size()) {
            throw new ApiException(CATEGORY_NOT_FOUND);
        }

        for (Category category : categories) {
            Long categoryGroupId = category.getMemberGroupId();
            if (categoryGroupId != null && !reqMemberGroups.contains(categoryGroupId)) {
                throw new ApiException(CATEGORY_NOT_FOUND);
            }
        }

        video.modify(
                modifyVideoRequest.getDescription(),
                modifyVideoRequest.getIsComment(),
                modifyVideoRequest.getExpiredAt()
        );

        videoMemberGroupMappingRepository.deleteAllByVideoId(videoId);
        videoCategoryMappingRepository.deleteAllByVideoId(videoId);

        List<VideoMemberGroupMapping> groupMappings = groups.stream()
                .map(group -> VideoMemberGroupMapping.create(video, group))
                .toList();
        videoMemberGroupMappingRepository.saveAll(groupMappings);

        List<VideoCategoryMapping> categoryMappings = categories.stream()
                .map(category -> VideoCategoryMapping.create(video, category))
                .toList();
        videoCategoryMappingRepository.saveAll(categoryMappings);

        return true;
    }

    public boolean deleteVideo(Long orgId, Long memberId, Long videoId) {
        Video video = videoRepository.findByIdAndOrganizationId(videoId, orgId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getCreator().getId().equals(memberId)) {
            throw new ApiException(VIDEO_CREATE_NOT_FOUND);
        }

        s3Util.deleteFileByKey(video.getThumbnailKey(), true);
        s3Util.deleteFileByKey(video.getVideoKey(), false);

        commentRepository.deleteAllByVideoId(videoId);
        historyRepository.deleteAllByVideoId(videoId);
        quizRepository.deleteAllByVideoId(videoId);
        scrapRepository.deleteAllByVideoId(videoId);

        videoMemberGroupMappingRepository.deleteAllByVideoId(videoId);
        videoCategoryMappingRepository.deleteAllByVideoId(videoId);
        videoRepository.delete(video);

        return true;
    }
}
