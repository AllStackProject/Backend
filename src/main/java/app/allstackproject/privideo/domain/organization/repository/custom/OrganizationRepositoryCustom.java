package app.allstackproject.privideo.domain.organization.repository.custom;

import app.allstackproject.privideo.dto.organization.ReadOrgResult;
import java.util.List;

public interface OrganizationRepositoryCustom {
    List<ReadOrgResult> findAllByUserId(Long userId);
}
