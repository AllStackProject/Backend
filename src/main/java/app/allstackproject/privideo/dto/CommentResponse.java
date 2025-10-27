package app.allstackproject.privideo.dto;

import app.allstackproject.privideo.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private List<CommentItem> comments;

    public static CommentResponse of(List<Comment> comments) {
        List<CommentItem> commentItems = comments.stream()
                .map(comment -> CommentItem.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .createdAt(comment.getCreatedAt())
                        .videoId(comment.getVideo().getId())
                        .videoName(comment.getVideo().getTitle())
                        .videoImg(comment.getVideo().getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .comments(commentItems)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class CommentItem {
        private Long id;
        private String text;
        private LocalDateTime createdAt;
        private Long videoId;
        private String videoName;
        private String videoImg;
    }
}
