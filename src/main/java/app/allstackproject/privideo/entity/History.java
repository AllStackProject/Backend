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
    private Long actualWatchSec;

    @NotNull
    private Long recentPositionSec;

    @NotNull
    private LocalDateTime startedAt;

    @NotNull
    private Long watchedSegCnt;

    private boolean hadEnd;

    private boolean isComplete;

    @NotNull
    private LocalDateTime completedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private History(Member member, Video video, Long actualWatchSec, Long recentPositionSec, LocalDateTime startedAt,
                    Long watchedSegCnt, boolean hadEnd, boolean isComplete, LocalDateTime completedAt) {
        this.member = member;
        this.video = video;
        this.actualWatchSec = actualWatchSec;
        this.recentPositionSec = recentPositionSec;
        this.startedAt = startedAt;
        this.watchedSegCnt = watchedSegCnt;
        this.hadEnd = hadEnd;
        this.isComplete = isComplete;
        this.completedAt = completedAt;
    }

    public static History create(Member member, Video video, Long actualWatchSec, Long recentPositionSec,
                                 LocalDateTime startedAt, Long watchedSegCnt, boolean hadEnd, boolean isComplete,
                                 LocalDateTime completedAt) {
        return History.builder()
                .member(member)
                .video(video)
                .actualWatchSec(actualWatchSec)
                .recentPositionSec(recentPositionSec)
                .startedAt(startedAt)
                .watchedSegCnt(watchedSegCnt)
                .hadEnd(hadEnd)
                .isComplete(isComplete)
                .completedAt(completedAt)
                .build();
    }
}
