package app.allstackproject.privideo.domain.admin.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideosResponse;
import app.allstackproject.privideo.domain.admin.service.VideoAdminService;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/org/{orgId}")
@PreAuthorize("hasAuthority('org:granted') and hasAuthority('perm:video_manage')")
@Tag(name = "Admin-Video", description = "관리자 영상 관리 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class VideoAdminController {

    private final VideoAdminService videoAdminService;

    @GetMapping("/video")
    @Operation(summary = "조직 내 모든 영상 조회")
    public BaseResponse<ReadAllVideosResponse> readAllVideos(@PathVariable Long orgId) {
        List<ReadAllVideoItem> allVideoItems = videoAdminService.readAllVideos(orgId);
        return new BaseResponse<>(ReadAllVideosResponse.of(allVideoItems));
    }

    @DeleteMapping("/video/{videoId}")
    @Operation(summary = "조직 내 특정 영상 삭제")
    public BaseResponse<SuccessResponse> deleteVideo(
            @PathVariable Long orgId,
            @PathVariable Long videoId) {
        return new BaseResponse<>(SuccessResponse.of(videoAdminService.deleteVideo(orgId, videoId)));
    }
}
