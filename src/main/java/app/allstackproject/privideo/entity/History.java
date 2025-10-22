package app.allstackproject.privideo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    private int watchedTime;

    private int wholeTime;

    private boolean isEnd;

    @Builder(access = AccessLevel.PRIVATE)
    private History(Member member, Video video, int watchedTime, int wholeTime, boolean isEnd) {
        this.member = member;
        this.video = video;
        this.watchedTime = watchedTime;
        this.wholeTime = wholeTime;
        this.isEnd = isEnd;
    }

    public static History create(Member member, Video video, int watchedTime, int wholeTime, boolean isEnd) {
        return History.builder()
                .member(member)
                .video(video)
                .watchedTime(watchedTime)
                .wholeTime(wholeTime)
                .isEnd(isEnd)
                .build();
    }
}
