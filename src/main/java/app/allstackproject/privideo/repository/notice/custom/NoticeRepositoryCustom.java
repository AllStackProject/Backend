package app.allstackproject.privideo.repository.notice.custom;

import app.allstackproject.privideo.dto.admin.ReadAllNoticeItem;
import java.util.List;

public interface NoticeRepositoryCustom {
    List<ReadAllNoticeItem> findAllByOrganizationId(Long orgId);
}
