package app.allstackproject.privideo.domain.history.entity;

import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.shared.entity.BaseEntity;
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
@Table(name = "History")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class History extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @NotNull
    private Long watchRate;

    @NotNull
    private Long recentPositionSec;

    @NotNull
    private LocalDateTime startedAt;

    private boolean hadEnd;

    private boolean isComplete;

    private LocalDateTime completedAt;

    private LocalDateTime lastWatchedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private History(Member member, Video video, Long watchRate, Long recentPositionSec, LocalDateTime startedAt,
                    boolean hadEnd, boolean isComplete, LocalDateTime completedAt, LocalDateTime lastWatchedAt) {
        this.member = member;
        this.video = video;
        this.watchRate = watchRate;
        this.recentPositionSec = recentPositionSec;
        this.startedAt = startedAt;
        this.hadEnd = hadEnd;
        this.isComplete = isComplete;
        this.completedAt = completedAt;
        this.lastWatchedAt = lastWatchedAt;
    }

    public static History create(Member member, Video video) {
        return History.builder()
                .member(member)
                .video(video)
                .watchRate(0L)
                .recentPositionSec(0L)
                .startedAt(LocalDateTime.now())
                .hadEnd(false)
                .isComplete(false)
                .lastWatchedAt(LocalDateTime.now())
                .build();
    }

    public void update(Long watchRate, Long recentPositionSec, boolean hadEnd) {
        this.watchRate = Math.max(this.watchRate, watchRate);
        this.recentPositionSec = recentPositionSec;
        this.hadEnd = hadEnd;

        if (watchRate >= 90 && hadEnd) {
            this.isComplete = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    public void updateLastWatchedAt() {
        this.lastWatchedAt = LocalDateTime.now();
    }
}
