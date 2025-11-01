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
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long wholeTime;

    private boolean isComment;

    private boolean isQuiz;

    @NotNull
    private LocalDate expiredAt;

    @NotNull
    private Long watchCnt;

    @NotNull
    private Long quitCnt;

    @Builder(access = AccessLevel.PRIVATE)
    private Video(Organization organization, Member creator, String title, String thumbnailUrl, Long wholeTime,
                  boolean isComment, boolean isQuiz, LocalDate expiredAt, Long watchCnt, Long quitCnt) {
        this.organization = organization;
        this.creator = creator;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.wholeTime = wholeTime;
        this.isComment = isComment;
        this.isQuiz = isQuiz;
        this.expiredAt = expiredAt;
        this.watchCnt = watchCnt;
        this.quitCnt = quitCnt;
    }

    public static Video create(Organization organization, Member creator, String title, String thumbnailUrl,
                               Long wholeTime, boolean isComment, boolean isQuiz, LocalDate expiredAt, Long watchCnt,
                               Long quitCnt) {
        if (expiredAt == null) {
            expiredAt = LocalDate.now().plusYears(100);
        }
        if (watchCnt == null) {
            watchCnt = 0L;
        }
        if (quitCnt == null) {
            quitCnt = 0L;
        }

        return Video.builder()
                .organization(organization)
                .creator(creator)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .wholeTime(wholeTime)
                .isComment(isComment)
                .isQuiz(isQuiz)
                .expiredAt(expiredAt)
                .watchCnt(watchCnt)
                .quitCnt(quitCnt)
                .build();
    }
}
