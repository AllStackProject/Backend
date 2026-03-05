package app.allstackproject.privideo.domain.video.service;

import static app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType.GROUP;
import static app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType.PUBLIC;
import static app.allstackproject.privideo.domain.video.enums.AiFunctionType.FEEDBACK;
import static app.allstackproject.privideo.domain.video.enums.AiFunctionType.NONE;
import static app.allstackproject.privideo.domain.video.enums.AiFunctionType.QUIZ;
import static app.allstackproject.privideo.domain.video.enums.AiFunctionType.SUMMARY;
import static app.allstackproject.privideo.domain.video.enums.S3ImgType.THUMBNAIL;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.FAIL;
import static app.allstackproject.privideo.domain.video.service.LogService.SEGMENT_SECONDS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.CATEGORY_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.HISTORY_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_AIRFLOW_STATUS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_MEMBER_GROUP_IDS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_PLAY_SESSION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.IS_NOT_IMAGE_FILE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.NOT_ALLOWED_MEMBER_GROUP_ACCESS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.PLAY_SESSION_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_WATCHING;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_CREATE_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_ACCESSIBLE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.comment.repository.CommentRepository;
import app.allstackproject.privideo.domain.history.entity.History;
import app.allstackproject.privideo.domain.history.repository.HistoryRepository;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import app.allstackproject.privideo.domain.member.entity.MemberGroupMapping;
import app.allstackproject.privideo.domain.member.repository.MemberGroupMappingRepository;
import app.allstackproject.privideo.domain.member.repository.MemberGroupRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.quiz.dto.QuizInfo;
import app.allstackproject.privideo.domain.quiz.repository.QuizRepository;
import app.allstackproject.privideo.domain.scrap.repository.ScrapRepository;
import app.allstackproject.privideo.domain.video.dto.request.CreateVideoRequest;
import app.allstackproject.privideo.domain.video.dto.request.LeaveVideoSessionInfo;
import app.allstackproject.privideo.domain.video.dto.request.ModifyVideoRequest;
import app.allstackproject.privideo.domain.video.dto.response.CreateVideoResponse;
import app.allstackproject.privideo.domain.video.dto.response.JoinVideoSessionResult;
import app.allstackproject.privideo.domain.video.dto.response.ReadVideoInfoResponse;
import app.allstackproject.privideo.domain.video.dto.response.VideoCategoryItem;
import app.allstackproject.privideo.domain.video.dto.response.VideoInfo;
import app.allstackproject.privideo.domain.video.dto.response.VideoMemberGroupItem;
import app.allstackproject.privideo.domain.video.entity.Category;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.video.entity.VideoCategoryMapping;
import app.allstackproject.privideo.domain.video.entity.VideoMemberGroupMapping;
import app.allstackproject.privideo.domain.video.enums.AiFunctionType;
import app.allstackproject.privideo.domain.video.enums.UploadStatusType;
import app.allstackproject.privideo.domain.video.repository.CategoryRepository;
import app.allstackproject.privideo.domain.video.repository.VideoCategoryMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRedisRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.global.util.S3Util;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.cache.enabled:true}")
    private boolean cacheEnabled;

    @Transactional(readOnly = true)
    public JoinVideoSessionResult prepareJoinVideoSession(Long memberId, Long orgId, Long videoId) {
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        String sessionId = UUID.nameUUIDFromBytes(
                (memberId.toString() + videoId.toString()).getBytes(StandardCharsets.UTF_8)
        ).toString();

        if (videoRedisRepository.existsWatchSession(sessionId)) {
            throw new ApiException(VIDEO_ALREADY_WATCHING);
        }

        if (!memberGroupRepository.isAccessibleToVideo(memberId, videoId)) {
            throw new ApiException(VIDEO_NOT_ACCESSIBLE);
        }

        String playbackUrl = s3Util.generatePlaybackUrl(video.getHlsPrefix());

        List<Long> segViewCnts = logService.getSegViewCounts(
                videoId,
                (int) Math.ceil((double) video.getWholeTime() / SEGMENT_SECONDS)
        );

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

        Long recentPositionSec = 0L;
        Optional<History> history = historyRepository.findByMemberIdAndVideoId(memberId, videoId);

        if (history.isPresent()) {
            recentPositionSec = history.get().getRecentPositionSec();
            if (history.get().isComplete()) {
                return JoinVideoSessionResult.completed(
                        sessionId, playbackUrl, VideoInfo.from(video, recentPositionSec), segViewCnts,
                        video.getIsComment(), isScrapped, categories, aiType, quizInfos, aiFeedback, aiSummary
                );
            }
        }

        return JoinVideoSessionResult.create(
                sessionId, playbackUrl, VideoInfo.from(video, recentPositionSec), segViewCnts,
                video.getIsComment(), isScrapped, categories, aiType, quizInfos, aiFeedback, aiSummary
        );
    }

    public void openWatchSession(String sessionId, Long memberId, Long orgId, Long videoId) {
        if (videoRedisRepository.existsWatchSession(sessionId)) {
            throw new ApiException(VIDEO_ALREADY_WATCHING);
        }

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        History history = historyRepository.findByMemberIdAndVideoId(memberId, videoId)
                .orElseGet(() -> History.create(
                        memberRepository.getReferenceById(memberId),
                        video
                ));

        if (history.getId() == null) {
            historyRepository.save(history);
        }

        video.watch();
        videoRedisRepository.createWatchSession(sessionId, memberId);

        if (!history.isComplete()) {
            logService.incOrgViewBucket(orgId, Instant.now());
        }
    }

    public String getFallbackPlaybackUrl(String hlsPrefix) {
        return s3Util.generatePlaybackUrl(hlsPrefix);
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

        String uuid = UUID.randomUUID().toString();
        // 1) 원본 비디오 키 생성 (privideo-original 버킷, 업로드는 presigned URL로)
        //    규칙: hls/org-{orgId}/{UUID}/video.mp4
        String originalKey = s3Util.generateVideoKey(orgId, uuid);

        // 2) 썸네일 키 생성 + 업로드 (privideo-img 버킷)
        //    규칙: images/org-{orgId}/thumbnail/{UUID}.{ext}
        MultipartFile thumbnailImg = request.getThumbnailImg();
        if (!s3Util.isImageFile(thumbnailImg)) {
            throw new ApiException(IS_NOT_IMAGE_FILE);
        }

        String thumbnailKey = s3Util.generateImgKey(orgId, thumbnailImg.getOriginalFilename(), uuid, THUMBNAIL);
        s3Util.uploadImgWithKey(thumbnailImg, thumbnailKey);

        // 3) HLS Prefix 계산
        //    규칙: hls/org-{orgId}/{UUID}
        String hlsPrefix = s3Util.generateHlsPrefix(originalKey);

        // 4) Video 엔티티 저장
        Video video = Video.create(
                organization,
                member,
                request.getTitle(),
                request.getDescription(),
                originalKey,
                thumbnailKey,
                hlsPrefix,
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

        // 5) 업로드용 URL 생성
        URL presignedUrl = s3Util.generatePresignedUploadUrl(originalKey);

        return CreateVideoResponse.of(presignedUrl.toString(), video.getId());
    }

    public SuccessResponse updateVideoEncodingResult(Long orgId, String videoUuid, String status) {
        String videoKey = s3Util.generateVideoKey(orgId, videoUuid);
        Video video = videoRepository.findByVideoKey(videoKey)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));
        Long videoId = video.getId();

        if (!video.getOrganization().getId().equals(orgId)) {
            throw new ApiException(VIDEO_NOT_IN_ORGANIZATION);
        }

        if (status.equals("SUCCESS")) {
            AiFunctionType aiFunction = video.getAiFunctionType();
            if (!aiFunction.equals(NONE)) {
                aiFunctionService.processAiFunction(videoId, videoKey, aiFunction);
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

    @Transactional(readOnly = true)
    public ReadVideoInfoResponse readVideoInfo(Long orgId, Long memberId, Long videoId) {
        // 캐시에서 비디오 정보 조회 시도
        Map<String, Object> cachedInfo = cacheEnabled
                ? videoRedisRepository.getCachedVideoInfo(videoId)
                : null;

        if (cachedInfo != null) {
            // 캐시 히트: 캐시된 데이터를 ReadVideoInfoResponse로 변환
            // 단, 사용자별 정보(멤버 그룹 등)는 매번 조회 필요
            Video video = videoRepository.findByIdAndOrganizationId(videoId, orgId)
                    .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));

            if (!video.getCreator().getId().equals(memberId)) {
                throw new ApiException(VIDEO_CREATE_NOT_FOUND);
            }

            // 사용자별 정보는 DB에서 조회
            List<VideoMemberGroupMapping> videoGroupMappings = videoMemberGroupMappingRepository.findAllByVideoId(
                    videoId);
            Set<Long> allMappingGroupIds = videoGroupMappings.stream()
                    .map(m -> m.getMemberGroup().getId())
                    .collect(Collectors.toSet());

            List<VideoCategoryMapping> videoCategoryMappings = videoCategoryMappingRepository.findAllByVideoId(videoId);
            Set<Long> allMappingCategoryIds = videoCategoryMappings.stream()
                    .map(m -> m.getCategory().getId())
                    .collect(Collectors.toSet());
            OpenScopeType openScope = allMappingGroupIds.isEmpty() ? PUBLIC : GROUP;

            List<MemberGroupMapping> myGroupMappings = memberGroupMappingRepository.findAllByMemberId(memberId);
            Map<Long, MemberGroup> myGroupsById = myGroupMappings.stream()
                    .map(MemberGroupMapping::getMemberGroup)
                    .collect(Collectors.toMap(
                            MemberGroup::getId,
                            g -> g,
                            (g1, g2) -> g1
                    ));

            List<Long> myGroupIds = new ArrayList<>(myGroupsById.keySet());
            if (myGroupIds.isEmpty()) {
                return ReadVideoInfoResponse.of(
                        (String) cachedInfo.get("title"),
                        (String) cachedInfo.get("description"),
                        (String) cachedInfo.get("thumbnailUrl"),
                        Long.valueOf(cachedInfo.get("watchCnt").toString()),
                        (java.time.LocalDate) cachedInfo.get("expiredAt"),
                        Boolean.valueOf(cachedInfo.get("isComment").toString()),
                        openScope,
                        List.of()
                );
            }

            List<Category> categories = categoryRepository.findByMemberGroupIdIn(myGroupIds);
            Map<Long, List<Category>> categoriesByGroupId = categories.stream()
                    .collect(Collectors.groupingBy(Category::getMemberGroupId));

            List<VideoMemberGroupItem> memberGroupItems = myGroupIds.stream()
                    .map(groupId -> {
                        MemberGroup group = myGroupsById.get(groupId);
                        boolean groupSelected = allMappingGroupIds.contains(groupId);

                        List<VideoCategoryItem> categoryItems = categoriesByGroupId
                                .getOrDefault(groupId, List.of())
                                .stream()
                                .map(c -> new VideoCategoryItem(
                                        c.getId(),
                                        c.getTitle(),
                                        allMappingCategoryIds.contains(c.getId())
                                ))
                                .toList();

                        return new VideoMemberGroupItem(
                                group.getId(),
                                group.getName(),
                                groupSelected,
                                categoryItems
                        );
                    })
                    .toList();

            return ReadVideoInfoResponse.of(
                    (String) cachedInfo.get("title"),
                    (String) cachedInfo.get("description"),
                    (String) cachedInfo.get("thumbnailUrl"),
                    Long.valueOf(cachedInfo.get("watchCnt").toString()),
                    (java.time.LocalDate) cachedInfo.get("expiredAt"),
                    Boolean.valueOf(cachedInfo.get("isComment").toString()),
                    openScope,
                    memberGroupItems
            );
        }

        // 캐시 미스: DB에서 조회
        Video video = videoRepository.findByIdAndOrganizationId(videoId, orgId)
                .orElseThrow(() -> new ApiException(VIDEO_NOT_FOUND));

        if (!video.getCreator().getId().equals(memberId)) {
            throw new ApiException(VIDEO_CREATE_NOT_FOUND);
        }
        String thumbnailUrl = cdnUrlProvider.generateImgUrl(video.getThumbnailKey());

        List<VideoMemberGroupMapping> videoGroupMappings = videoMemberGroupMappingRepository.findAllByVideoId(videoId);
        Set<Long> allMappingGroupIds = videoGroupMappings.stream()
                .map(m -> m.getMemberGroup().getId())
                .collect(Collectors.toSet());

        List<VideoCategoryMapping> videoCategoryMappings = videoCategoryMappingRepository.findAllByVideoId(videoId);
        Set<Long> allMappingCategoryIds = videoCategoryMappings.stream()
                .map(m -> m.getCategory().getId())
                .collect(Collectors.toSet());
        OpenScopeType openScope = allMappingGroupIds.isEmpty() ? PUBLIC : GROUP;

        List<MemberGroupMapping> myGroupMappings = memberGroupMappingRepository.findAllByMemberId(memberId);
        Map<Long, MemberGroup> myGroupsById = myGroupMappings.stream()
                .map(MemberGroupMapping::getMemberGroup)
                .collect(Collectors.toMap(
                        MemberGroup::getId,
                        g -> g,
                        (g1, g2) -> g1
                ));

        List<Long> myGroupIds = new ArrayList<>(myGroupsById.keySet());
        if (myGroupIds.isEmpty()) {
            ReadVideoInfoResponse response = ReadVideoInfoResponse.of(
                    video.getTitle(),
                    video.getDescription(),
                    thumbnailUrl,
                    video.getWatchCnt(),
                    video.getExpiredAt(),
                    video.getIsComment(),
                    openScope,
                    List.of()
            );

            // 캐시에 저장
            if (cacheEnabled) {
                Map<String, Object> videoInfoForCache = new HashMap<>();
                videoInfoForCache.put("title", video.getTitle());
                videoInfoForCache.put("description", video.getDescription());
                videoInfoForCache.put("thumbnailUrl", thumbnailUrl);
                videoInfoForCache.put("watchCnt", video.getWatchCnt());
                videoInfoForCache.put("expiredAt", video.getExpiredAt());
                videoInfoForCache.put("isComment", video.getIsComment());
                videoRedisRepository.cacheVideoInfo(videoId, videoInfoForCache);
            }

            return response;
        }

        List<Category> categories = categoryRepository.findByMemberGroupIdIn(myGroupIds);
        Map<Long, List<Category>> categoriesByGroupId = categories.stream()
                .collect(Collectors.groupingBy(Category::getMemberGroupId));

        List<VideoMemberGroupItem> memberGroupItems = myGroupIds.stream()
                .map(groupId -> {
                    MemberGroup group = myGroupsById.get(groupId);
                    boolean groupSelected = allMappingGroupIds.contains(groupId);

                    List<VideoCategoryItem> categoryItems = categoriesByGroupId
                            .getOrDefault(groupId, List.of())
                            .stream()
                            .map(c -> new VideoCategoryItem(
                                    c.getId(),
                                    c.getTitle(),
                                    allMappingCategoryIds.contains(c.getId())
                            ))
                            .toList();

                    return new VideoMemberGroupItem(
                            group.getId(),
                            group.getName(),
                            groupSelected,
                            categoryItems
                    );
                })
                .toList();

        ReadVideoInfoResponse response = ReadVideoInfoResponse.of(
                video.getTitle(),
                video.getDescription(),
                thumbnailUrl,
                video.getWatchCnt(),
                video.getExpiredAt(),
                video.getIsComment(),
                openScope,
                memberGroupItems
        );

        // 캐시에 저장
        if (cacheEnabled) {
            Map<String, Object> videoInfoForCache = new HashMap<>();
            videoInfoForCache.put("title", video.getTitle());
            videoInfoForCache.put("description", video.getDescription());
            videoInfoForCache.put("thumbnailUrl", thumbnailUrl);
            videoInfoForCache.put("watchCnt", video.getWatchCnt());
            videoInfoForCache.put("expiredAt", video.getExpiredAt());
            videoInfoForCache.put("isComment", video.getIsComment());
            videoRedisRepository.cacheVideoInfo(videoId, videoInfoForCache);
        }

        return response;
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

        // 비디오 수정 시 캐시 무효화
        videoRedisRepository.invalidateVideoCache(videoId);
        videoRedisRepository.invalidateHomeCache(orgId);

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

        // 비디오 삭제 시 캐시 무효화
        videoRedisRepository.invalidateVideoCache(videoId);
        videoRedisRepository.invalidateHomeCache(orgId);

        videoRepository.delete(video);

        return true;
    }
}
