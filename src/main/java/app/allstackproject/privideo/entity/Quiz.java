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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @NotBlank
    @Column(length = 600)
    private String question;

    private boolean answer;

    @NotBlank
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private Quiz(Video video, String question, boolean answer, String description) {
        this.video = video;
        this.question = question;
        this.answer = answer;
        this.description = description;
    }

    public static Quiz create(Video video, String question, boolean answer, String description) {
        return Quiz.builder()
                .video(video)
                .question(question)
                .answer(answer)
                .description(description)
                .build();
    }
}
