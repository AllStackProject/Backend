package app.allstackproject.privideo.domain.video.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ModifyVideoRequest {
    @NotNull
    private String description;

    @NotNull
    private Boolean isComment;

    @NotNull
    private LocalDate expiredAt;

    @NotNull
    private List<Long> memberGroups;

    @NotNull
    private List<Long> categories;
}
