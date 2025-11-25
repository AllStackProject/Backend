package app.allstackproject.privideo.repository.notice.custom;

import app.allstackproject.privideo.dto.admin.AdminReadAllNoticeItem;
import app.allstackproject.privideo.dto.home.ReadAllNoticeItem;
import java.util.List;

public interface NoticeRepositoryCustom {
    List<AdminReadAllNoticeItem> findAllByOrganizationId(Long orgId);

    List<ReadAllNoticeItem> findAllVisibleByOrgIdAndMemberId(Long orgId, Long memberId);
}
