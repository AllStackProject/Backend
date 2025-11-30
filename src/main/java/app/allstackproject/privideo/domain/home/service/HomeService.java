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
import app.allstackproject.privideo.dto.home.HomeVideoItem;
import app.allstackproject.privideo.dto.home.ReadAllNoticeItem;
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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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

        List<HomeVideoItem> videoInfos = videoRepository.findHomeVideos(orgId, memberId, filter);
        videoInfos.forEach(info -> info.setThumbnailUrl(cdnUrlProvider.generateImgUrl(info.getThumbnailUrl())));

        List<Long> videoIds = videoInfos.stream()
                .map(HomeVideoItem::getId)
                .toList();

        Map<Long, List<String>> categoriesMap = videoRepository.findCategoriesForHomeVideos(memberId, videoIds);

        List<HomeVideoItem> homeVideoItems = videoInfos.stream()
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

        List<String> globalCategories = categoriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        return ReadHomeResponse.of(nickname, isAdmin, orgName, homeVideoItems, globalCategories);
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
