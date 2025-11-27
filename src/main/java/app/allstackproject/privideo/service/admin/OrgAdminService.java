package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.S3ImgType.ORG;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CATEGORY_ALREADY_EXIST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CATEGORY_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DUPLICATE_CATEGORY_NAME;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_ALREADY_EXIST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.CdnUrlProvider;
import app.allstackproject.privideo.common.util.OrgCodeGenerator;
import app.allstackproject.privideo.common.util.S3Util;
import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.dto.admin.ReadAllCategoryItem;
import app.allstackproject.privideo.dto.admin.ReadAllMemberGroupItem;
import app.allstackproject.privideo.dto.admin.ReadAdminOrganizationInfoResponse;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.entity.Category;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.member.MemberGroupMappingRepository;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.notice.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.video.CategoryRepository;
import app.allstackproject.privideo.repository.video.VideoCategoryMappingRepository;
import app.allstackproject.privideo.repository.video.VideoMemberGroupMappingRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class OrgAdminService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final CategoryRepository categoryRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;
    private final VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;
    private final VideoCategoryMappingRepository videoCategoryMappingRepository;
    private final S3Util s3Util;
    private final CdnUrlProvider cdnUrlProvider;
    private final NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;

    public boolean modifyOrgInfo(Long orgId, MultipartFile img) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        String uuid = UUID.randomUUID().toString();
        String oldImgKey = organization.getImgKey();
        String newImgKey = s3Util.generateImgKey(orgId, img.getOriginalFilename(), uuid, ORG);

        s3Util.uploadImgWithKey(img, newImgKey);
        organization.setImgKey(newImgKey);
        s3Util.deleteFileByKey(oldImgKey, true);

        return true;
    }

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

    @Transactional(readOnly = true)
    public ReadAdminOrganizationInfoResponse readOrganizationInfo(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        String orgName = organization.getName();
        String imgUrl = cdnUrlProvider.generateImgUrl(organization.getImgKey());
        Long memberCnt = memberRepository.countByOrganizationIdAndJoinStatusAndStatus(orgId, APPROVED, ACTIVE);
        String orgCode = orgRedisRepository.getOrgCodeById(orgId);

        List<MemberGroupItem> memberGroups = memberGroupRepository.findAllByOrganizationId(orgId);

        if (memberGroups.isEmpty()) {
            return ReadAdminOrganizationInfoResponse.of(orgName, imgUrl, memberCnt, orgCode, List.of());
        }

        List<Long> groupIds = memberGroups.stream()
                .map(MemberGroupItem::getId)
                .toList();

        List<Category> allCategories = categoryRepository.findByMemberGroupIdIn(groupIds);

        Map<Long, List<Category>> categoriesByGroupId = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getMemberGroupId));

        List<ReadAllMemberGroupItem> memberGroupItems = memberGroups.stream()
                .map(group -> {
                    List<ReadAllCategoryItem> categories =
                            categoriesByGroupId.getOrDefault(group.getId(), List.of()).stream()
                                    .map(c -> new ReadAllCategoryItem(c.getId(), c.getTitle()))
                                    .toList();

                    return new ReadAllMemberGroupItem(
                            group.getId(),
                            group.getName(),
                            categories
                    );
                })
                .toList();

        return ReadAdminOrganizationInfoResponse.of(orgName, imgUrl, memberCnt, orgCode, memberGroupItems);
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
        memberGroupMappingRepository.deleteByMemberGroupId(groupId);
        videoMemberGroupMappingRepository.deleteAllByMemberGroupId(groupId);
        noticeMemberGroupMappingRepository.deleteAllByMemberGroupId(groupId);
        memberGroupRepository.delete(memberGroup);
        return true;
    }

    @Transactional(readOnly = true)
    public List<ReadAllCategoryItem> readAllCategory(Long orgId, Long groupId) {
        if (!memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)) {
            throw new ApiException(MEMBER_GROUP_NOT_FOUND);
        }

        return categoryRepository.findByMemberGroupId(groupId).stream()
                .map(c -> new ReadAllCategoryItem(c.getId(), c.getTitle()))
                .toList();
    }

    public boolean createCategory(Long orgId, Long groupId, String title) {
        if (!memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)) {
            throw new ApiException(MEMBER_GROUP_NOT_FOUND);
        }

        if (categoryRepository.existsByMemberGroupIdAndTitle(groupId, title)) {
            throw new ApiException(CATEGORY_ALREADY_EXIST);
        }

        categoryRepository.save(Category.create(title, groupId));
        return true;
    }

    public boolean modifyCategory(Long orgId, Long groupId, Long categoryId, String newTitle) {
        if (!memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)) {
            throw new ApiException(MEMBER_GROUP_NOT_FOUND);
        }

        if (categoryRepository.existsByMemberGroupIdAndTitle(groupId, newTitle)) {
            throw new ApiException(DUPLICATE_CATEGORY_NAME);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(CATEGORY_NOT_FOUND));
        category.modifyTitle(newTitle);

        return true;
    }

    public boolean deleteCategory(Long orgId, Long groupId, Long categoryId) {
        if (!memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId)) {
            throw new ApiException(MEMBER_GROUP_NOT_FOUND);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(CATEGORY_NOT_FOUND));
        videoCategoryMappingRepository.deleteAllByCategoryId(categoryId);
        categoryRepository.delete(category);

        return true;
    }
}
