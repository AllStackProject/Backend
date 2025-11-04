package app.allstackproject.privideo.entity;

import static app.allstackproject.privideo.service.video.LogService.SEGMENT_SECONDS;
import static java.lang.Math.ceil;

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

    private int watchedSegCnt;

    private boolean hadEnd;

    private boolean isComplete;

    private LocalDateTime completedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private History(Member member, Video video, Long watchRate, Long recentPositionSec, LocalDateTime startedAt,
                    int watchedSegCnt, boolean hadEnd, boolean isComplete, LocalDateTime completedAt) {
        this.member = member;
        this.video = video;
        this.watchRate = watchRate;
        this.recentPositionSec = recentPositionSec;
        this.startedAt = startedAt;
        this.watchedSegCnt = watchedSegCnt;
        this.hadEnd = hadEnd;
        this.isComplete = isComplete;
        this.completedAt = completedAt;
    }

    public static History create(Member member, Video video) {
        return History.builder()
                .member(member)
                .video(video)
                .watchRate(0L)
                .recentPositionSec(0L)
                .startedAt(LocalDateTime.now())
                .watchedSegCnt(0)
                .hadEnd(false)
                .isComplete(false)
                .build();
    }

    public void update(Long watchRate, Long recentPositionSec, int watchedSegCnt, boolean hadEnd) {
        this.watchRate = watchRate;
        this.recentPositionSec = recentPositionSec;
        this.watchedSegCnt = watchedSegCnt;
        this.hadEnd = hadEnd;

        if (watchedSegCnt > 0.9 * ceil((double) video.getWholeTime() / SEGMENT_SECONDS) && hadEnd) {
            this.isComplete = true;
            this.completedAt = LocalDateTime.now();
        }
    }
}
