package app.allstackproject.privideo.domain.notice.repository.custom;

import app.allstackproject.privideo.domain.admin.dto.AdminReadAllNoticeItem;
import app.allstackproject.privideo.domain.notice.dto.ReadAllNoticeItem;
import java.util.List;

public interface NoticeRepositoryCustom {
    List<AdminReadAllNoticeItem> findAllByOrganizationId(Long orgId);

    List<ReadAllNoticeItem> findAllVisibleByOrgIdAndMemberId(Long orgId, Long memberId);
}
