package app.allstackproject.privideo.dto.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private MultipartFile thumbnailImg;

    @NotNull
    private Long wholeTime;

    @NotNull
    private Boolean isComment;

    @NotBlank
    private String aiFunction;

    private LocalDate expiredAt;
}
