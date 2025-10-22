package app.allstackproject.privideo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Member_Quiz_Result")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberQuizResult extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    private boolean isCorrect;

    @NotNull
    private LocalDateTime submittedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberQuizResult(Quiz quiz, Member member, Video video, boolean isCorrect, LocalDateTime submittedAt) {
        this.quiz = quiz;
        this.member = member;
        this.video = video;
        this.isCorrect = isCorrect;
        this.submittedAt = submittedAt;
    }

    public static MemberQuizResult create(Quiz quiz, Member member, Video video, boolean isCorrect,
                                          LocalDateTime submittedAt) {
        return MemberQuizResult.builder()
                .quiz(quiz)
                .member(member)
                .video(video)
                .isCorrect(isCorrect)
                .submittedAt(submittedAt)
                .build();
    }
}
