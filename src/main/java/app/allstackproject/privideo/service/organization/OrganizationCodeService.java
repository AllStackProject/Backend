package app.allstackproject.privideo.service.organization;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CODE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CREATE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationCodeService {

    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;

    //조직 코드 재발급
    @Transactional
    public String regenereteOrgCode(Long orgId, String newCode) {

        if (newCode == null || newCode.isBlank()) {
            throw new ApiException(INVALID_ORG_CREATE);
        }

        //orgId로 org 찾고 없으면 예외
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        String oldCode = orgRedisRepository.regenerateCode(orgId, newCode);

        org.updateCode(newCode);

        //JPA save()
        organizationRepository.save(org);
        log.info("조직 코드 재발급 완료 - orgId: {}, {} -> {}",
                orgId, oldCode, newCode);

        return oldCode;
    }

    //조직 생성 시 코드 발급
    @Transactional
    public void createOrgCode(Long orgId, String code) {

        //Redis에 코드 저장
        orgRedisRepository.createOrgCode(orgId, code);

        log.info("조직 코드 생성 완료 - orgId: {}, code: {}", orgId, code);
    }

    //조직 코드로 ID 조회
    public Long getOrgIdByCode(String code) {
        Long orgId = orgRedisRepository.getOrgIdByCode(code);

        if (orgId != null) {
            log.debug("Redis - code: {}, orgId: {}", code, orgId);
            return orgId;
        }

        //Redis 검색실패, RDB 조회
        Organization org = organizationRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(INVALID_ORG_CODE));

        //Redis 캐싱
        try {
            orgRedisRepository.createOrgCode(org.getId(), code);
            log.debug("Redis 캐싱 성공 - code: {}, orgId: {}", code, org.getId());
        } catch (Exception e) {
            log.error("Redis 캐싱 실패 - code: {}, 에러: {}", code, e.getMessage());
        }

        return org.getId();
    }
}
