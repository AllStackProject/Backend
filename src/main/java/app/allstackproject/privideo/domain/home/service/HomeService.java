package app.allstackproject.privideo.domain.home.service;

import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType.GROUP;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.NOTICE_FORBIDDEN;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.NOTICE_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.domain.video.enums.FilterType;
import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.domain.home.dto.response.HomeVideoItem;
import app.allstackproject.privideo.domain.notice.dto.ReadAllNoticeItem;
import app.allstackproject.privideo.domain.home.dto.response.ReadHomeResponse;
import app.allstackproject.privideo.domain.notice.dto.ReadNoticeResponse;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.notice.entity.Notice;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.member.repository.MemberGroupMappingRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.notice.repository.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.notice.repository.NoticeRepository;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRedisRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final VideoRepository videoRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;
    private final CdnUrlProvider cdnUrlProvider;
    private final VideoRedisRepository videoRedisRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.enabled:true}")
    private boolean cacheEnabled;

    @Transactional(readOnly = true)
    public ReadHomeResponse readHome(Long memberId, Long orgId, String filterStr) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        FilterType filter = FilterType.from(filterStr);

        String nickname = member.getNickname();
        Boolean isAdmin = member.getPermissionCode() != 0L;
        String orgName = organization.getName();

        // 캐시에서 비디오 목록 조회 시도
        List<Map<String, Object>> cachedVideoData = cacheEnabled
                ? videoRedisRepository.getCachedHomeVideos(orgId, filter.name())
                : null;
        List<HomeVideoItem> homeVideoItems;

        if (cachedVideoData != null) {
            // 캐시 히트: 캐시된 데이터를 HomeVideoItem으로 변환
            homeVideoItems = convertCachedDataToHomeVideoItems(cachedVideoData, memberId);
        } else {
            // 캐시 미스: DB에서 조회
            List<HomeVideoItem> videoInfos = videoRepository.findHomeVideos(orgId, memberId, filter);
            videoInfos.forEach(info -> info.setThumbnailUrl(cdnUrlProvider.generateImgUrl(info.getThumbnailUrl())));

            List<Long> videoIds = videoInfos.stream()
                    .map(HomeVideoItem::getId)
                    .toList();

            Map<Long, List<String>> categoriesMap = videoRepository.findCategoriesForHomeVideos(memberId, videoIds);

            homeVideoItems = videoInfos.stream()
                    .map(v -> new HomeVideoItem(
                            v.getId(),
                            v.getTitle(),
                            v.getThumbnailUrl(),
                            v.getCreator(),
                            v.getWholeTime(),
                            v.getWatchCnt(),
                            v.getCreatedAt(),
                            v.getIsScrapped(),
                            categoriesMap.getOrDefault(v.getId(), List.of())
                    ))
                    .toList();

            // 캐시에 저장 (비디오 목록만 저장, 사용자별 정보는 제외)
            if (cacheEnabled) {
                List<Map<String, Object>> videoDataForCache = convertHomeVideoItemsToMap(homeVideoItems);
                videoRedisRepository.cacheHomeVideos(orgId, filter.name(), videoDataForCache);
            }
        }

        List<String> globalCategories = homeVideoItems.stream()
                .flatMap(v -> v.getCategories().stream())
                .distinct()
                .toList();

        return ReadHomeResponse.of(nickname, isAdmin, orgName, homeVideoItems, globalCategories);
    }

    private List<HomeVideoItem> convertCachedDataToHomeVideoItems(List<Map<String, Object>> cachedData, Long memberId) {
        List<HomeVideoItem> items = new ArrayList<>();
        for (Map<String, Object> data : cachedData) {
            // 스크랩 여부는 사용자별로 다르므로 다시 조회 필요
            // 여기서는 기본값으로 설정하고, 필요시 별도 조회
            Boolean isScrapped = data.get("isScrapped") != null 
                    ? Boolean.valueOf(data.get("isScrapped").toString()) 
                    : false;

            HomeVideoItem item = new HomeVideoItem(
                    Long.valueOf(data.get("id").toString()),
                    data.get("title").toString(),
                    cdnUrlProvider.generateImgUrl(data.get("thumbnailUrl").toString()),
                    data.get("creator").toString(),
                    Long.valueOf(data.get("wholeTime").toString()),
                    Long.valueOf(data.get("watchCnt").toString()),
                    objectMapper.convertValue(data.get("createdAt"), java.time.LocalDateTime.class),
                    isScrapped,
                    objectMapper.convertValue(data.get("categories"), new TypeReference<List<String>>() {})
            );
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> convertHomeVideoItemsToMap(List<HomeVideoItem> items) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (HomeVideoItem item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("title", item.getTitle());
            map.put("thumbnailUrl", item.getThumbnailUrl());
            map.put("creator", item.getCreator());
            map.put("wholeTime", item.getWholeTime());
            map.put("watchCnt", item.getWatchCnt());
            map.put("createdAt", item.getCreatedAt());
            map.put("categories", item.getCategories());
            result.add(map);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<HomeVideoItem> readSearchVideo(Long memberId, Long orgId, String keyword) {
        if (!memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)) {
            throw new ApiException(MEMBER_NOT_IN_ORGANIZATION);
        }

        List<HomeVideoItem> result = videoRepository.findSearchVideos(orgId, memberId, keyword);
        result.forEach(item -> item.setThumbnailUrl(cdnUrlProvider.generateImgUrl(item.getThumbnailUrl())));
        return result;
    }

    @Transactional(readOnly = true)
    public List<ReadAllNoticeItem> readAllNotice(Long orgId, Long memberId) {
        return noticeRepository.findAllVisibleByOrgIdAndMemberId(orgId, memberId);
    }

    public ReadNoticeResponse readNotice(Long orgId, Long memberId, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndOrganizationId(noticeId, orgId)
                .orElseThrow(() -> new ApiException(NOTICE_NOT_IN_ORGANIZATION));
        notice.watch();

        List<Long> noticeGroupIds = noticeMemberGroupMappingRepository.findAllByNoticeId(noticeId)
                .stream()
                .map(m -> m.getMemberGroup().getId())
                .toList();

        List<Long> myGroupIds = memberGroupMappingRepository.findAllByMemberId(memberId)
                .stream()
                .map(m -> m.getMemberGroup().getId())
                .toList();

        OpenScopeType scope;

        if (noticeGroupIds.isEmpty()) {
            scope = OpenScopeType.PUBLIC;
        } else if (noticeGroupIds.stream().anyMatch(myGroupIds::contains)) {
            scope = GROUP;
        } else {
            throw new ApiException(NOTICE_FORBIDDEN);
        }

        return ReadNoticeResponse.of(
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getWatchCnt(),
                scope
        );
    }
}
