package app.allstackproject.privideo.repository.notice.custom;

import app.allstackproject.privideo.dto.admin.ReadAllNotificationItem;
import java.util.List;

public interface NoticeRepositoryCustom {
    List<ReadAllNotificationItem> findAllByOrganizationId(Long orgId);
}
