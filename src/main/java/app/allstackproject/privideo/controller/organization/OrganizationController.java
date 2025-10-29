package app.allstackproject.privideo.controller.organization;

import static app.allstackproject.privideo.common.filter.JwtAuthFilter.ACCESS_TOKEN_HEADER;
import static app.allstackproject.privideo.common.filter.JwtAuthFilter.TOKEN_PREFIX;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CREATE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_JOIN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_SELECT;
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
import app.allstackproject.privideo.dto.organization.SelectOrgRequest;
import app.allstackproject.privideo.service.organization.OrganizationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orgs")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @PostMapping("")
    public BaseResponse<CreateOrgResponse> createOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                     @Valid @RequestBody CreateOrgRequest createOrgRequest,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_CREATE, getErrorMessage(bindingResult));
        }

        CreateOrgResult createOrgResult = organizationService.createOrg(userId, createOrgRequest);
        return new BaseResponse<>(CreateOrgResponse.of(createOrgResult));
    }

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @GetMapping("")
    public BaseResponse<ReadOrgsResponse> readOrgs(@AuthenticationPrincipal(expression = "userId") Long userId) {
        List<ReadOrgDto> result = organizationService.readOrgs(userId);
        return new BaseResponse<>(ReadOrgsResponse.of(result));
    }

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @PostMapping("/join")
    public BaseResponse<SuccessResponse> joinOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                 @Valid @RequestBody JoinOrgRequest joinOrgRequest,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_JOIN, getErrorMessage(bindingResult));
        }

        boolean isSuccess = organizationService.joinOrg(userId, joinOrgRequest.getName(), joinOrgRequest.getCode());
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @PatchMapping("")
    public BaseResponse<SuccessResponse> selectOrg(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                   @Valid @RequestBody SelectOrgRequest selectOrgRequest,
                                                   BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_ORG_SELECT, getErrorMessage(bindingResult));
        }

        String orgToken = organizationService.selectOrg(userId, selectOrgRequest.getId());
        if (orgToken == null || orgToken.isBlank()) {
            return new BaseResponse<>(SuccessResponse.of(false));
        }

        response.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + orgToken);
        return new BaseResponse<>(SuccessResponse.of(true));
    }
}
