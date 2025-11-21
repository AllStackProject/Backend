package app.allstackproject.privideo.dto.video;

import app.allstackproject.privideo.entity.Video;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoInfo {
    private final Long id;

    private final String title;

    private final String description;

    private final String hlsPrefix;

    private final Long watchCnt;

    private final Long wholeTime;

    private final LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private VideoInfo(Long id, String title, String description, String hlsPrefix, Long watchCnt, Long wholeTime,
                      LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.hlsPrefix = hlsPrefix;
        this.watchCnt = watchCnt;
        this.wholeTime = wholeTime;
        this.createdAt = createdAt;
    }

    public static VideoInfo from(Video video) {
        return VideoInfo.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .hlsPrefix(video.getHlsPrefix())
                .watchCnt(video.getWatchCnt())
                .wholeTime(video.getWholeTime())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
