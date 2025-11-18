package app.allstackproject.privideo.repository.organization.custom;

import app.allstackproject.privideo.dto.organization.ReadOrgResult;
import java.util.List;

public interface OrganizationRepositoryCustom {
    List<ReadOrgResult> findAllByUserId(Long userId);
}
