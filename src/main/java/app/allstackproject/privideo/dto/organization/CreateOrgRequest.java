package app.allstackproject.privideo.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrgRequest {
    @NotBlank(message = "조직 이름은 1글자 이상이어야 합니다.")
    private String name;

    @NotNull(message = "조직 이미지는 필수입니다.")
    private MultipartFile img;

    @NotNull
    private String desc;
}
