package app.allstackproject.privideo.dto;

import app.allstackproject.privideo.entity.History;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryResponse {

    private List<VideoItem> videos;

    public static HistoryResponse of(List<History> histories) {
        List<VideoItem> videoItems = histories.stream()
                .map(h -> {
                    double watchRate = h.getWholeTime() == 0 ? 0.0 :
                            (double) h.getWatchedTime() / h.getWholeTime();

                    return VideoItem.builder()
                            .id(h.getVideo().getId())
                            .name(h.getVideo().getTitle())
                            .img(h.getVideo().getThumbnailUrl())
                            .watchRate(watchRate)
                            .recentWatch(h.getLastModifiedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return HistoryResponse.builder()
                .videos(videoItems)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoItem {
        private Long id;
        private String name;
        private String img;
        private double watchRate;
        private LocalDateTime recentWatch;
    }
}
