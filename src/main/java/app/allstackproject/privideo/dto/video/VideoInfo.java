package app.allstackproject.privideo.dto.video;

import app.allstackproject.privideo.entity.Video;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoInfo {
    private final Long id;

    private final String title;

    private final String url;

    private final Long watchCnt;

    private final Long wholeTime;

    @Builder(access = AccessLevel.PRIVATE)
    private VideoInfo(Long id, String title, String url, Long watchCnt, Long wholeTime) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.watchCnt = watchCnt;
        this.wholeTime = wholeTime;
    }

    public static VideoInfo from(Video video) {
        return VideoInfo.builder()
                .id(video.getId())
                .title(video.getTitle())
                .url(video.getUrl())
                .watchCnt(video.getWatchCnt())
                .wholeTime(video.getWholeTime())
                .build();
    }
}
