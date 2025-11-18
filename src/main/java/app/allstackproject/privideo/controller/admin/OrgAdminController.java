package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.admin.CreateCategoryRequest;
import app.allstackproject.privideo.dto.admin.CreateMemberGroupRequest;
import app.allstackproject.privideo.dto.admin.ModifyCategoryRequest;
import app.allstackproject.privideo.dto.admin.ModifyOrgInfoRequest;
import app.allstackproject.privideo.dto.admin.ReadAllCategoryResponse;
import app.allstackproject.privideo.dto.admin.ReadOrganizationInfoResponse;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.service.admin.OrgAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/org/{orgId}")
@PreAuthorize("hasAuthority('org:granted') and hasAuthority('perm:org_setting')")
@Tag(name = "Admin-Org", description = "관리자 조직 설정 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class OrgAdminController {

    private final OrgAdminService orgAdminService;

    @GetMapping("/orgs/info")
    @Operation(summary = "조직 정보 조회")
    public BaseResponse<ReadOrganizationInfoResponse> readOrganizationInfo(@PathVariable("orgId") Long orgId) {
        return new BaseResponse<>(orgAdminService.readOrganizationInfo(orgId));
    }

    @PatchMapping("/orgs/info")
    @Operation(summary = "조직 이미지 수정")
    public BaseResponse<SuccessResponse> modifyOrgInfo(@PathVariable("orgId") Long orgId,
                                                       @Valid @ModelAttribute ModifyOrgInfoRequest modifyOrgInfoRequest) {
        // TODO: S3에 이미지 업로드
        String imgUrl = "";
        return new BaseResponse<>(
                SuccessResponse.of(orgAdminService.modifyOrgInfo(orgId, imgUrl)));
    }

    @PatchMapping("/orgs/code")
    @Operation(summary = "조직 코드 재발급", description = "조직 코드를 새로 발급합니다.")
    public BaseResponse<OrgCodeResponse> regenerateOrgToken(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId) {
        OrgCodeResponse newOrgCode = orgAdminService.regenerateOrgCode(memberId, orgId);
        return new BaseResponse<>(newOrgCode);
    }

    @PostMapping("/group")
    @Operation(summary = "멤버 그룹 추가")
    public BaseResponse<SuccessResponse> createMemberGroup(@PathVariable("orgId") Long orgId,
                                                           @Valid @RequestBody CreateMemberGroupRequest createMemberGroupRequest) {
        return new BaseResponse<>(SuccessResponse.of(
                orgAdminService.createMemberGroup(orgId, createMemberGroupRequest.getName())));
    }

    @DeleteMapping("/group/{groupId}")
    @Operation(summary = "멤버 그룹 삭제")
    public BaseResponse<SuccessResponse> deleteMemberGroup(@PathVariable("orgId") Long orgId,
                                                           @PathVariable("groupId") Long groupId) {
        return new BaseResponse<>(SuccessResponse.of(orgAdminService.deleteMemberGroup(orgId, groupId)));
    }

    @GetMapping("/group/{groupId}/category")
    @Operation(summary = "카테고리 전체 조회")
    public BaseResponse<ReadAllCategoryResponse> readAllCategory(@PathVariable("orgId") Long orgId,
                                                                 @PathVariable("groupId") Long groupId) {
        return new BaseResponse<>(ReadAllCategoryResponse.of(orgAdminService.readAllCategory(orgId, groupId)));
    }

    @PostMapping("/group/{groupId}/category")
    @Operation(summary = "카테고리 추가")
    public BaseResponse<SuccessResponse> createCategory(@PathVariable("orgId") Long orgId,
                                                        @PathVariable("groupId") Long groupId,
                                                        @Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        return new BaseResponse<>(SuccessResponse.of(
                orgAdminService.createCategory(orgId, groupId, createCategoryRequest.getTitle())));
    }

    @PutMapping("/group/{groupId}/category/{categoryId}")
    @Operation(summary = "카테고리 수정")
    public BaseResponse<SuccessResponse> modifyCategory(@PathVariable("orgId") Long orgId,
                                                        @PathVariable("groupId") Long groupId,
                                                        @PathVariable("categoryId") Long categoryId,
                                                        @Valid @RequestBody ModifyCategoryRequest modifyCategoryRequest) {
        return new BaseResponse<>(SuccessResponse.of(
                orgAdminService.modifyCategory(orgId, groupId, categoryId, modifyCategoryRequest.getTitle())));
    }

    @DeleteMapping("/group/{groupId}/category/{categoryId}")
    @Operation(summary = "카테고리 삭제")
    public BaseResponse<SuccessResponse> deleteCategory(@PathVariable("orgId") Long orgId,
                                                        @PathVariable("groupId") Long groupId,
                                                        @PathVariable("categoryId") Long categoryId) {
        return new BaseResponse<>(SuccessResponse.of(orgAdminService.deleteCategory(orgId, groupId, categoryId)));
    }
}
