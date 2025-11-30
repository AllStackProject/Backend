package app.allstackproject.privideo.domain.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrgRequest {
    @NotBlank(message = "조직 이름은 1글자 이상이어야 합니다.")
    private String name;

    @Schema(description = "조직 이미지", type = "string", format = "binary")
    @NotNull(message = "조직 이미지는 필수입니다.")
    private MultipartFile img;

    @NotNull
    private String desc;

    @NotNull
    private String nickname;
}
