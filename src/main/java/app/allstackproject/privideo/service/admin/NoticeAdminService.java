package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.NOTICE_NOT_IN_ORGANIZATION;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.dto.admin.NoticeMemberGroupInfo;
import app.allstackproject.privideo.dto.admin.AdminReadAllNoticeItem;
import app.allstackproject.privideo.dto.admin.ReadNoticeResponse;
import app.allstackproject.privideo.entity.Notice;
import app.allstackproject.privideo.entity.NoticeMemberGroupMapping;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.notice.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.repository.notice.NoticeRepository;
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

    private final NoticeRepository noticeRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;

    @Transactional(readOnly = true)
    public List<AdminReadAllNoticeItem> readAllNotice(Long orgId) {
        return noticeRepository.findAllByOrganizationId(orgId);
    }

    @Transactional(readOnly = true)
    public ReadNoticeResponse readNotice(Long orgId, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndOrganizationId(noticeId, orgId)
                .orElseThrow(() -> new ApiException(NOTICE_NOT_IN_ORGANIZATION));

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

        return ReadNoticeResponse.of(
                notice.getTitle(),
                notice.getContent(),
                groupInfos
        );
    }

    public boolean deleteNotice(Long orgId, Long noticeId) {
        Notice notice = noticeRepository.findByIdAndOrganizationId(noticeId, orgId)
                .orElseThrow(() -> new ApiException(NOTICE_NOT_IN_ORGANIZATION));
        noticeRepository.delete(notice);
        return true;
    }
}
