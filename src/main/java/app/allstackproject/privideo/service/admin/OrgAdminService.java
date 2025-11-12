package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CATEGORY_ALREADY_EXIST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CATEGORY_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DUPLICATE_CATEGORY_NAME;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_ALREADY_EXIST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.OrgCodeGenerator;
import app.allstackproject.privideo.dto.admin.ReadAllCategoryDto;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.entity.Category;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.video.CategoryRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrgAdminService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final CategoryRepository categoryRepository;

    public OrgCodeResponse regenerateOrgCode(Long memberId, Long orgId) {
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (member.getJoinStatus() != APPROVED) {
            throw new ApiException(MEMBER_NOT_FOUND);
        }

        String newCode = OrgCodeGenerator.generateCode(orgId);

        orgRedisRepository.regenerateCode(orgId, newCode);

        return new OrgCodeResponse(newCode);
    }

    public boolean createMemberGroup(Long orgId, String memberGroupName) {
        if (memberGroupRepository.existsByName(memberGroupName)) {
            throw new ApiException(MEMBER_GROUP_ALREADY_EXIST);
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        memberGroupRepository.save(MemberGroup.create(organization, memberGroupName));
        return true;
    }

    public boolean deleteMemberGroup(Long orgId, Long groupId) {
        MemberGroup memberGroup = memberGroupRepository.findByIdAndOrganizationId(groupId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_GROUP_NOT_FOUND));
        memberGroupRepository.delete(memberGroup);
        return true;
    }

    @Transactional(readOnly = true)
    public List<ReadAllCategoryDto> readAllCategory(Long orgId, Long groupId) {
        memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_GROUP_NOT_FOUND));

        return categoryRepository.findByMemberGroupId(groupId).stream()
                .map(c -> new ReadAllCategoryDto(c.getId(), c.getTitle()))
                .toList();
    }

    public boolean createCategory(Long orgId, Long groupId, String title) {
        memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_GROUP_NOT_FOUND));

        if (categoryRepository.existsByMemberGroupIdAndTitle(groupId, title)) {
            throw new ApiException(CATEGORY_ALREADY_EXIST);
        }

        categoryRepository.save(Category.create(title, groupId));
        return true;
    }

    public boolean modifyCategory(Long orgId, Long groupId, Long categoryId, String newTitle) {
        memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_GROUP_NOT_FOUND));

        if (categoryRepository.existsByMemberGroupIdAndTitle(groupId, newTitle)) {
            throw new ApiException(DUPLICATE_CATEGORY_NAME);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(CATEGORY_NOT_FOUND));
        category.modifyTitle(newTitle);

        return true;
    }
}
