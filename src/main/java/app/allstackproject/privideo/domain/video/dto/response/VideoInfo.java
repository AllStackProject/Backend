package app.allstackproject.privideo.domain.video.dto.response;

import app.allstackproject.privideo.domain.video.entity.Video;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoInfo {
    private final Long id;

    private final String title;

    private final String description;

    @JsonIgnore
    private final String hlsPrefix;

    private final Long watchCnt;

    private final Long wholeTime;

    private final Long recentPositionSec;

    private final LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private VideoInfo(Long id, String title, String description, String hlsPrefix, Long watchCnt, Long wholeTime,
                      Long recentPositionSec, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.hlsPrefix = hlsPrefix;
        this.watchCnt = watchCnt;
        this.wholeTime = wholeTime;
        this.recentPositionSec = recentPositionSec;
        this.createdAt = createdAt;
    }

    public static VideoInfo from(Video video, Long recentPositionSec) {
        return VideoInfo.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .hlsPrefix(video.getHlsPrefix())
                .watchCnt(video.getWatchCnt())
                .wholeTime(video.getWholeTime())
                .recentPositionSec(recentPositionSec)
                .createdAt(video.getCreatedAt())
                .build();
    }
}
