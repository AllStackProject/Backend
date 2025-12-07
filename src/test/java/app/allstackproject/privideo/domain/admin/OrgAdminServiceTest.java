package app.allstackproject.privideo.domain.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.allstackproject.privideo.domain.admin.dto.MemberGroupItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAdminOrganizationInfoResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadAllCategoryItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberGroupItem;
import app.allstackproject.privideo.domain.admin.service.OrgAdminService;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import app.allstackproject.privideo.domain.member.repository.MemberGroupMappingRepository;
import app.allstackproject.privideo.domain.member.repository.MemberGroupRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.notice.repository.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import app.allstackproject.privideo.domain.organization.dto.response.OrgCodeResponse;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.video.entity.Category;
import app.allstackproject.privideo.domain.video.repository.CategoryRepository;
import app.allstackproject.privideo.domain.video.repository.VideoCategoryMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.global.util.S3Util;
import app.allstackproject.privideo.shared.enums.BaseStatusType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class OrgAdminServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    OrganizationRepository organizationRepository;
    @Mock
    OrgRedisRepository orgRedisRepository;
    @Mock
    MemberGroupRepository memberGroupRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    MemberGroupMappingRepository memberGroupMappingRepository;
    @Mock
    VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;
    @Mock
    VideoCategoryMappingRepository videoCategoryMappingRepository;
    @Mock
    S3Util s3Util;
    @Mock
    CdnUrlProvider cdnUrlProvider;
    @Mock
    NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;

    @InjectMocks
    OrgAdminService orgAdminService;

    // ========= modifyOrgInfo =========

    @Test
    @DisplayName("modifyOrgInfo - 조직 이미지 변경 성공")
    void modifyOrgInfo_success() {
        Long orgId = 1L;
        MultipartFile img = mock(MultipartFile.class);
        Organization org = mock(Organization.class);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(org.getImgKey()).thenReturn("old-key");
        when(img.getOriginalFilename()).thenReturn("logo.png");
        when(s3Util.generateImgKey(eq(orgId), anyString(), anyString(), any()))
                .thenReturn("new-key");

        boolean result = orgAdminService.modifyOrgInfo(orgId, img);

        assertTrue(result);
        verify(s3Util).uploadImgWithKey(img, "new-key");
        verify(org).setImgKey("new-key");
        verify(s3Util).deleteFileByKey("old-key", true);
    }

    @Test
    @DisplayName("modifyOrgInfo - 조직이 없으면 예외")
    void modifyOrgInfo_orgNotFound() {
        Long orgId = 1L;
        MultipartFile img = mock(MultipartFile.class);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> orgAdminService.modifyOrgInfo(orgId, img));
    }

    // ========= regenerateOrgCode =========

    @Test
    @DisplayName("regenerateOrgCode - 승인된 멤버이면 코드 재발급 성공")
    void regenerateOrgCode_success() {
        Long orgId = 1L;
        Long memberId = 2L;

        Organization org = mock(Organization.class);
        Member member = mock(Member.class);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(Optional.of(member));
        when(member.getJoinStatus()).thenReturn(JoinStatusType.APPROVED);

        OrgCodeResponse response = orgAdminService.regenerateOrgCode(memberId, orgId);

        assertNotNull(response);
        assertNotNull(response.getNewCode());
        verify(orgRedisRepository)
                .regenerateCode(eq(orgId), eq(response.getNewCode()));
    }

    @Test
    @DisplayName("regenerateOrgCode - 멤버가 없으면 예외")
    void regenerateOrgCode_memberNotFound() {
        Long orgId = 1L;
        Long memberId = 2L;

        Organization org = mock(Organization.class);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> orgAdminService.regenerateOrgCode(memberId, orgId));
    }

    @Test
    @DisplayName("regenerateOrgCode - joinStatus가 APPROVED가 아니면 예외")
    void regenerateOrgCode_notApproved() {
        Long orgId = 1L;
        Long memberId = 2L;

        Organization org = mock(Organization.class);
        Member member = mock(Member.class);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(Optional.of(member));
        when(member.getJoinStatus()).thenReturn(JoinStatusType.PENDING);

        assertThrows(ApiException.class,
                () -> orgAdminService.regenerateOrgCode(memberId, orgId));

        verify(orgRedisRepository, never()).regenerateCode(anyLong(), anyString());
    }

    // ========= readOrganizationInfo =========

    @Test
    @DisplayName("readOrganizationInfo - 멤버 그룹이 없으면 빈 리스트 반환")
    void readOrganizationInfo_noGroups() {
        Long orgId = 1L;
        Organization org = mock(Organization.class);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(org.getName()).thenReturn("테스트 조직");
        when(org.getImgKey()).thenReturn("org-key");
        when(cdnUrlProvider.generateImgUrl("org-key")).thenReturn("https://cdn/org.png");
        when(memberRepository.countByOrganizationIdAndJoinStatusAndStatus(eq(orgId), any(), any()))
                .thenReturn(5L);
        when(orgRedisRepository.getOrgCodeById(orgId)).thenReturn("ORGCODE");
        when(memberGroupRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());

        ReadAdminOrganizationInfoResponse response = orgAdminService.readOrganizationInfo(orgId);

        assertEquals("테스트 조직", response.getOrgName());
        assertEquals("https://cdn/org.png", response.getImgUrl());
        assertEquals(5L, response.getMemberCnt());
        assertEquals("ORGCODE", response.getOrgCode());
        assertTrue(response.getMemberGroups().isEmpty());
    }

    @Test
    @DisplayName("readOrganizationInfo - 멤버 그룹과 카테고리 묶어서 반환")
    void readOrganizationInfo_withGroupsAndCategories() {
        Long orgId = 1L;
        Organization org = mock(Organization.class);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(org.getName()).thenReturn("테스트 조직");
        when(org.getImgKey()).thenReturn("org-key");
        when(cdnUrlProvider.generateImgUrl("org-key")).thenReturn("https://cdn/org.png");
        when(memberRepository.countByOrganizationIdAndJoinStatusAndStatus(eq(orgId), any(), any()))
                .thenReturn(5L);
        when(orgRedisRepository.getOrgCodeById(orgId)).thenReturn("ORGCODE");

        MemberGroupItem g1 = new MemberGroupItem(1L, "개발팀");
        MemberGroupItem g2 = new MemberGroupItem(2L, "기획팀");
        when(memberGroupRepository.findAllByOrganizationId(orgId))
                .thenReturn(List.of(g1, g2));

        Category c1 = mock(Category.class);
        when(c1.getId()).thenReturn(100L);
        when(c1.getTitle()).thenReturn("백엔드");
        when(c1.getMemberGroupId()).thenReturn(1L);

        Category c2 = mock(Category.class);
        when(c2.getId()).thenReturn(200L);
        when(c2.getTitle()).thenReturn("프론트엔드");
        when(c2.getMemberGroupId()).thenReturn(2L);

        when(categoryRepository.findByMemberGroupIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(c1, c2));

        ReadAdminOrganizationInfoResponse response = orgAdminService.readOrganizationInfo(orgId);

        assertEquals(2, response.getMemberGroups().size());

        ReadAllMemberGroupItem r1 = response.getMemberGroups().get(0);
        assertEquals("개발팀", r1.getName());
        assertEquals(1, r1.getCategories().size());
        ReadAllCategoryItem rc1 = r1.getCategories().get(0);
        assertEquals("백엔드", rc1.getTitle());
    }

    // ========= createMemberGroup / deleteMemberGroup =========

    @Test
    @DisplayName("createMemberGroup - 이름 중복이면 예외")
    void createMemberGroup_duplicateName() {
        Long orgId = 1L;
        String name = "개발팀";

        when(memberGroupRepository.existsByName(name)).thenReturn(true);

        assertThrows(ApiException.class,
                () -> orgAdminService.createMemberGroup(orgId, name));
    }

    @Test
    @DisplayName("createMemberGroup - 정상 생성")
    void createMemberGroup_success() {
        Long orgId = 1L;
        String name = "개발팀";

        Organization org = mock(Organization.class);
        when(memberGroupRepository.existsByName(name)).thenReturn(false);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        boolean result = orgAdminService.createMemberGroup(orgId, name);

        assertTrue(result);
        verify(memberGroupRepository).save(any(MemberGroup.class));
    }

    @Test
    @DisplayName("deleteMemberGroup - 그룹이 없으면 예외")
    void deleteMemberGroup_notFound() {
        Long orgId = 1L;
        Long groupId = 10L;

        when(memberGroupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> orgAdminService.deleteMemberGroup(orgId, groupId));
    }

    @Test
    @DisplayName("deleteMemberGroup - 매핑/공지 매핑 삭제 후 그룹 삭제")
    void deleteMemberGroup_success() {
        Long orgId = 1L;
        Long groupId = 10L;

        MemberGroup group = mock(MemberGroup.class);
        when(memberGroupRepository.findByIdAndOrganizationId(groupId, orgId))
                .thenReturn(Optional.of(group));

        boolean result = orgAdminService.deleteMemberGroup(orgId, groupId);

        assertTrue(result);
        verify(memberGroupMappingRepository).deleteByMemberGroupId(groupId);
        verify(videoMemberGroupMappingRepository).deleteAllByMemberGroupId(groupId);
        verify(noticeMemberGroupMappingRepository).deleteAllByMemberGroupId(groupId);
        verify(memberGroupRepository).delete(group);
    }

    // ========= readAllCategory =========

    @Test
    @DisplayName("readAllCategory - 그룹이 조직에 없으면 예외")
    void readAllCategory_groupNotFound() {
        Long orgId = 1L;
        Long groupId = 10L;

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> orgAdminService.readAllCategory(orgId, groupId));
    }

    @Test
    @DisplayName("readAllCategory - 카테고리 목록 조회 성공")
    void readAllCategory_success() {
        Long orgId = 1L;
        Long groupId = 10L;

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);

        Category c1 = mock(Category.class);
        when(c1.getId()).thenReturn(100L);
        when(c1.getTitle()).thenReturn("백엔드");

        when(categoryRepository.findByMemberGroupId(groupId))
                .thenReturn(List.of(c1));

        List<ReadAllCategoryItem> result = orgAdminService.readAllCategory(orgId, groupId);

        assertEquals(1, result.size());
        assertEquals("백엔드", result.get(0).getTitle());
    }

    // ========= createCategory =========

    @Test
    @DisplayName("createCategory - 그룹이 없으면 예외")
    void createCategory_groupNotFound() {
        Long orgId = 1L;
        Long groupId = 10L;

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> orgAdminService.createCategory(orgId, groupId, "백엔드"));
    }

    @Test
    @DisplayName("createCategory - 중복 카테고리면 예외")
    void createCategory_alreadyExist() {
        Long orgId = 1L;
        Long groupId = 10L;
        String title = "백엔드";

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.existsByMemberGroupIdAndTitle(groupId, title))
                .thenReturn(true);

        assertThrows(ApiException.class,
                () -> orgAdminService.createCategory(orgId, groupId, title));
    }

    @Test
    @DisplayName("createCategory - 정상 생성")
    void createCategory_success() {
        Long orgId = 1L;
        Long groupId = 10L;
        String title = "백엔드";

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.existsByMemberGroupIdAndTitle(groupId, title))
                .thenReturn(false);

        boolean result = orgAdminService.createCategory(orgId, groupId, title);

        assertTrue(result);
        verify(categoryRepository).save(any(Category.class));
    }

    // ========= modifyCategory =========

    @Test
    @DisplayName("modifyCategory - 그룹이 없으면 예외")
    void modifyCategory_groupNotFound() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> orgAdminService.modifyCategory(orgId, groupId, categoryId, "새 제목"));
    }

    @Test
    @DisplayName("modifyCategory - 새로운 제목이 이미 존재하면 예외")
    void modifyCategory_duplicateTitle() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;
        String newTitle = "백엔드";

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.existsByMemberGroupIdAndTitle(groupId, newTitle))
                .thenReturn(true);

        assertThrows(ApiException.class,
                () -> orgAdminService.modifyCategory(orgId, groupId, categoryId, newTitle));
    }

    @Test
    @DisplayName("modifyCategory - 카테고리가 없으면 예외")
    void modifyCategory_categoryNotFound() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;
        String newTitle = "백엔드";

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.existsByMemberGroupIdAndTitle(groupId, newTitle))
                .thenReturn(false);
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> orgAdminService.modifyCategory(orgId, groupId, categoryId, newTitle));
    }

    @Test
    @DisplayName("modifyCategory - 정상 수정")
    void modifyCategory_success() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;
        String newTitle = "백엔드";

        Category category = mock(Category.class);

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.existsByMemberGroupIdAndTitle(groupId, newTitle))
                .thenReturn(false);
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        boolean result = orgAdminService.modifyCategory(orgId, groupId, categoryId, newTitle);

        assertTrue(result);
        verify(category).modifyTitle(newTitle);
    }

    // ========= deleteCategory =========

    @Test
    @DisplayName("deleteCategory - 그룹이 없으면 예외")
    void deleteCategory_groupNotFound() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> orgAdminService.deleteCategory(orgId, groupId, categoryId));
    }

    @Test
    @DisplayName("deleteCategory - 카테고리가 없으면 예외")
    void deleteCategory_categoryNotFound() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(ApiException.class,
                () -> orgAdminService.deleteCategory(orgId, groupId, categoryId));
    }

    @Test
    @DisplayName("deleteCategory - 매핑 삭제 후 카테고리 삭제")
    void deleteCategory_success() {
        Long orgId = 1L;
        Long groupId = 10L;
        Long categoryId = 100L;

        Category category = mock(Category.class);

        when(memberGroupRepository.existsByIdAndOrganizationId(groupId, orgId))
                .thenReturn(true);
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        boolean result = orgAdminService.deleteCategory(orgId, groupId, categoryId);

        assertTrue(result);
        verify(videoCategoryMappingRepository).deleteAllByCategoryId(categoryId);
        verify(categoryRepository).delete(category);
    }
}