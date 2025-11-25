package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.OpenScopeType.PUBLIC;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_MEMBER_GROUP_IDS;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.NOTICE_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.enumStatus.OpenScopeType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.CreateNoticeRequest;
import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.dto.admin.NoticeMemberGroupInfo;
import app.allstackproject.privideo.dto.admin.AdminReadAllNoticeItem;
import app.allstackproject.privideo.dto.admin.AdminReadNoticeResponse;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.entity.Notice;
import app.allstackproject.privideo.entity.NoticeMemberGroupMapping;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.notice.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.repository.notice.NoticeRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeAdminService {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;

    @Transactional(readOnly = true)
    public List<AdminReadAllNoticeItem> readAllNotice(Long orgId) {
        return noticeRepository.findAllByOrganizationId(orgId);
    }

    public AdminReadNoticeResponse readNotice(Long orgId, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndOrganizationId(noticeId, orgId)
                .orElseThrow(() -> new ApiException(NOTICE_NOT_IN_ORGANIZATION));
        notice.watch();

        List<MemberGroupItem> allGroups = memberGroupRepository.findAllByOrganizationId(orgId);

        List<NoticeMemberGroupMapping> mappings = noticeMemberGroupMappingRepository.findAllByNoticeId(noticeId);

        Set<Long> selectedGroupIds = mappings.stream()
                .map(mapping -> mapping.getMemberGroup().getId())
                .collect(Collectors.toSet());

        List<NoticeMemberGroupInfo> groupInfos = allGroups.stream()
                .map(g -> new NoticeMemberGroupInfo(
                        g.getId(),
                        g.getName(),
                        selectedGroupIds.contains(g.getId())
                ))
                .toList();

        return AdminReadNoticeResponse.of(
                notice.getTitle(),
                notice.getContent(),
                groupInfos
        );
    }

    public boolean createNotice(Long orgId, Long memberId, CreateNoticeRequest createNoticeRequest) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member creator = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));
        Notice notice = Notice.create(organization, creator, createNoticeRequest.getTitle(),
                createNoticeRequest.getContent());

        noticeRepository.save(notice);

        List<Long> memberGroups = createNoticeRequest.getMemberGroups();
        OpenScopeType openScope = createNoticeRequest.getOpenScope();

        if (openScope == PUBLIC) {
            return true;
        }

        if (openScope == OpenScopeType.GROUP && memberGroups.isEmpty()) {
            throw new ApiException(INVALID_MEMBER_GROUP_IDS);
        }

        List<MemberGroup> groups = memberGroupRepository.findAllById(memberGroups);

        if (groups.size() != memberGroups.size()) {
            throw new ApiException(INVALID_MEMBER_GROUP_IDS);
        }

        List<NoticeMemberGroupMapping> mappings = groups.stream()
                .map(group -> NoticeMemberGroupMapping.create(group, notice))
                .toList();

        noticeMemberGroupMappingRepository.saveAll(mappings);

        return true;
    }

    public boolean deleteNotice(Long orgId, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndOrganizationId(noticeId, orgId)
                .orElseThrow(() -> new ApiException(NOTICE_NOT_IN_ORGANIZATION));
        noticeRepository.delete(notice);
        return true;
    }
}
