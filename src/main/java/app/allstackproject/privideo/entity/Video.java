package app.allstackproject.privideo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Video")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id")
    private Member creator;

    @NotBlank
    @Column(length = 600)
    private String title;

    @NotBlank
    private String thumbnailUrl;

    private boolean isComment;

    private boolean isQuiz;

    @NotBlank
    private LocalDate expireTime;

    @Builder(access = AccessLevel.PRIVATE)
    private Video(Organization organization, Member creator, String title, String thumbnailUrl, boolean isComment,
                  boolean isQuiz, LocalDate expireTime) {
        this.organization = organization;
        this.creator = creator;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.isComment = isComment;
        this.isQuiz = isQuiz;
        this.expireTime = expireTime;
    }

    public static Video create(Organization organization, Member creator, String title, String thumbnailUrl,
                               boolean isComment, boolean isQuiz, LocalDate expireTime) {
        return Video.builder()
                .organization(organization)
                .creator(creator)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .isComment(isComment)
                .isQuiz(isQuiz)
                .expireTime(expireTime)
                .build();
    }
}
