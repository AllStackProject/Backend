package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.ReadAllNotificationItem;
import app.allstackproject.privideo.repository.notice.NoticeRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeAdminService {

    private final OrganizationRepository organizationRepository;
    private final NoticeRepository noticeRepository;

    public List<ReadAllNotificationItem> readAllNotification(Long orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ApiException(ORGANIZATION_NOT_FOUND);
        }

        return noticeRepository.findAllByOrganizationId(orgId);
    }

}
