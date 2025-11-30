package app.allstackproject.privideo.domain.organization.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.BOOTSTRAP_AUTH_KEY;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_ORG_CREATE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_ORG_JOIN;
import static app.allstackproject.privideo.global.security.JwtAuthFilter.ACCESS_TOKEN_HEADER;
import static app.allstackproject.privideo.global.security.JwtAuthFilter.TOKEN_PREFIX;
import static app.allstackproject.privideo.global.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.domain.organization.dto.request.CreateOrgRequest;
import app.allstackproject.privideo.domain.organization.dto.request.JoinOrgRequest;
import app.allstackproject.privideo.domain.organization.dto.response.CreatOrgResult;
import app.allstackproject.privideo.domain.organization.dto.response.CreateOrgResponse;
import app.allstackproject.privideo.domain.organization.dto.response.ReadOrgDto;
import app.allstackproject.privideo.domain.organization.dto.response.ReadOrgsResponse;
import app.allstackproject.privideo.domain.organization.dto.response.SelectOrgResponse;
import app.allstackproject.privideo.domain.organization.dto.response.SelectOrgResult;
import app.allstackproject.privideo.domain.organization.service.OrganizationService;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.shared.enums.AuthPrincipal;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orgs")
@PreAuthorize("hasAuthority('bootstrap:granted')")
@Tag(name = "Organization", description = "조직 관련 API")
@SecurityRequirement(name = BOOTSTRAP_AUTH_KEY)
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping(value = "")
    @Operation(summary = "조직 생성")
    public BaseResponse<CreateOrgResponse> createOrg(
            @AuthenticationPrincipal AuthPrincipal me,
            @Valid @ModelAttribute CreateOrgRequest createOrgRequest,
            BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_CREATE, getErrorMessage(bindingResult));
        }

        CreatOrgResult result = organizationService.createOrg(me.userId(), createOrgRequest);
        response.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + result.getToken());
        return new BaseResponse<>(CreateOrgResponse.of(result.getId()));
    }

    @GetMapping("/availability")
    @Operation(summary = "조직명 중복 조회", description = "새로 생성할 조직에 대해 중복 조직명이 존재하는지 조회합니다.")
    public BaseResponse<SuccessResponse> validateOrgName(
            @AuthenticationPrincipal AuthPrincipal me,
            @RequestParam("name") String orgName) {
        return new BaseResponse<>(SuccessResponse.of(organizationService.validateOrgName(me.userId(), orgName)));
    }

    @GetMapping("")
    @Operation(summary = "조직 목록 조회", description = "가입 요청을 보낸 조직과 가입이 완료된 조직을 모두 조회합니다.")
    public BaseResponse<ReadOrgsResponse> readOrgs(@AuthenticationPrincipal AuthPrincipal me) {
        List<ReadOrgDto> readOrgResult = organizationService.readOrgs(me.userId());
        return new BaseResponse<>(ReadOrgsResponse.of(readOrgResult));
    }

    @PostMapping("/join")
    @Operation(summary = "조직 가입 요청")
    public BaseResponse<SuccessResponse> joinOrg(
            @AuthenticationPrincipal AuthPrincipal me,
            @Valid @RequestBody JoinOrgRequest joinOrgRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_JOIN, getErrorMessage(bindingResult));
        }

        return new BaseResponse<>(SuccessResponse.of(
                organizationService.joinOrg(me.userId(), joinOrgRequest.getCode(), joinOrgRequest.getNickname())));
    }

    @GetMapping("/availability/nickname")
    @Operation(summary = "조직 닉네임 중복 조회", description = "조직 내에서 사용할 닉네임에 대해 중복 닉네임이 존재하는지 조회합니다.")
    public BaseResponse<SuccessResponse> validateOrgNickname(
            @AuthenticationPrincipal AuthPrincipal me,
            @RequestParam String nickname,
            @RequestParam String code) {
        return new BaseResponse<>(
                SuccessResponse.of(organizationService.validateOrgNickname(me.userId(), nickname, code)));
    }

    @PatchMapping("/{orgId}")
    @Operation(summary = "조직 선택", description = "org token을 발행합니다.")
    public BaseResponse<SelectOrgResponse> selectOrg(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            HttpServletResponse response) {
        SelectOrgResult selectOrgResult = organizationService.selectOrg(me.userId(), orgId);
        response.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + selectOrgResult.getToken());
        return new BaseResponse<>(SelectOrgResponse.of(selectOrgResult.getNickname()));
    }

    @PutMapping("/{orgId}")
    @Operation(summary = "조직 탈퇴")
    public BaseResponse<SuccessResponse> exitOrg(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId) {
        return new BaseResponse<>(SuccessResponse.of(organizationService.exitOrg(me.userId(), orgId)));
    }
}
