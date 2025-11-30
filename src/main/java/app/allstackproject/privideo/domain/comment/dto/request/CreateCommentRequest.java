package app.allstackproject.privideo.domain.comment.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateCommentRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String text;

    @Nullable
    private Long parentCommentId;
}
