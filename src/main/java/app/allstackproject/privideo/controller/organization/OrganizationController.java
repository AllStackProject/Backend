package app.allstackproject.privideo.controller.organization;

import static app.allstackproject.privideo.common.config.SwaggerConfig.BOOTSTRAP_AUTH_KEY;
import static app.allstackproject.privideo.common.filter.JwtAuthFilter.ACCESS_TOKEN_HEADER;
import static app.allstackproject.privideo.common.filter.JwtAuthFilter.TOKEN_PREFIX;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CREATE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_JOIN;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.organization.CreateOrgRequest;
import app.allstackproject.privideo.dto.organization.CreateOrgResponse;
import app.allstackproject.privideo.dto.organization.CreateOrgResult;
import app.allstackproject.privideo.dto.organization.JoinOrgRequest;
import app.allstackproject.privideo.dto.organization.ReadOrgDto;
import app.allstackproject.privideo.dto.organization.ReadOrgsResponse;
import app.allstackproject.privideo.service.organization.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orgs")
@PreAuthorize("hasAuthority('bootstrap:granted')")
@Tag(name = "Organization", description = "조직 관련 API")
@SecurityRequirement(name = BOOTSTRAP_AUTH_KEY)
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("")
    @Operation(summary = "조직 생성")
    public BaseResponse<CreateOrgResponse> createOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                     @Valid @RequestBody CreateOrgRequest createOrgRequest,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_CREATE, getErrorMessage(bindingResult));
        }

        CreateOrgResult createOrgResult = organizationService.createOrg(userId, createOrgRequest);
        return new BaseResponse<>(CreateOrgResponse.of(createOrgResult));
    }

    @GetMapping("")
    @Operation(summary = "전체 조직 조회", description = "가입 요청을 보낸 조직과 가입이 완료된 조직을 모두 조회합니다.")
    public BaseResponse<ReadOrgsResponse> readOrgs(@AuthenticationPrincipal(expression = "userId") Long userId) {
        List<ReadOrgDto> result = organizationService.readOrgs(userId);
        return new BaseResponse<>(ReadOrgsResponse.of(result));
    }

    @PostMapping("/{orgId}/join")
    @Operation(summary = "조직 가입 요청")
    public BaseResponse<SuccessResponse> joinOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                 @Valid @RequestBody JoinOrgRequest joinOrgRequest,
                                                 @PathVariable("orgId") Long orgId,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_JOIN, getErrorMessage(bindingResult));
        }

        boolean isSuccess = organizationService.joinOrg(userId, orgId, joinOrgRequest.getCode());
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

    @PatchMapping("/{orgId}")
    @Operation(summary = "조직 선택", description = "org token을 발행합니다.")
    public BaseResponse<SuccessResponse> selectOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                   @PathVariable("orgId") Long orgId, HttpServletResponse response) {
        String orgToken = organizationService.selectOrg(userId, orgId);
        if (orgToken == null || orgToken.isBlank()) {
            return new BaseResponse<>(SuccessResponse.of(false));
        }

        response.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + orgToken);
        return new BaseResponse<>(SuccessResponse.of(true));
    }

    @PutMapping("/{orgId}")
    @Operation(summary = "조직 탈퇴")
    public BaseResponse<SuccessResponse> exitOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                 @PathVariable("orgId") Long orgId) {
        boolean isSuccess = organizationService.exitOrg(userId, orgId);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }
}
