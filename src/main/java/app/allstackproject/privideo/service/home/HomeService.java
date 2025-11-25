package app.allstackproject.privideo.service.home;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.enumStatus.FilterType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.dto.home.HomeVideoItem;
import app.allstackproject.privideo.dto.home.ReadAllNoticeItem;
import app.allstackproject.privideo.dto.home.ReadHomeResponse;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.notice.NoticeRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
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

    public List<ReadAllNoticeItem> readAllNotice(Long orgId, Long memberId) {
        return noticeRepository.findAllVisibleByOrgIdAndMemberId(orgId, memberId);
    }
}
