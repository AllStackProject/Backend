package app.allstackproject.privideo.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @NotBlank
    @Column(length = 600)
    private String text;

    private boolean isChild;

    @Nullable
    private Long parentCommentId;

    @Builder(access = AccessLevel.PRIVATE)
    private Comment(Video video, Member member, String text, boolean isChild, @Nullable Long parentCommentId) {
        this.video = video;
        this.member = member;
        this.text = text;
        this.isChild = isChild;
        this.parentCommentId = parentCommentId;
    }

    public static Comment create(Video video, Member member, String text, boolean isChild,
                                 @Nullable Long parentCommentId) {
        return Comment.builder()
                .video(video)
                .member(member)
                .text(text)
                .isChild(isChild)
                .parentCommentId(parentCommentId)
                .build();
    }
}
