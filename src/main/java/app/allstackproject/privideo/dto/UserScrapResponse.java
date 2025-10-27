package app.allstackproject.privideo.dto;

import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.entity.Scrap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScrapResponse {

    private List<ScrapVideo> allScrap;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScrapVideo {
        private Long id;
        private String name;
        private String img;
        private double watchRate;
        private LocalDateTime recentWatch;

        public static ScrapVideo of(Scrap scrap, History history) {
            double watchRate = history.getWholeTime() == 0 ? 0.0 :
                    (double) history.getWatchedTime() / history.getWholeTime();

            return ScrapVideo.builder()
                    .id(scrap.getVideo().getId())
                    .name(scrap.getVideo().getTitle())
                    .img(scrap.getVideo().getThumbnailUrl())
                    .watchRate(watchRate)
                    .recentWatch(history.getLastModifiedAt())
                    .build();
        }

        public static List<ScrapVideo> of(List<Scrap> scraps, Map<Long, History> historyMapByVideoId) {
            return scraps.stream()
                    .map(scrap -> of(scrap, historyMapByVideoId.get(scrap.getVideo().getId())))
                    .collect(Collectors.toList());
        }
    }

    public static UserScrapResponse of(List<Scrap> scraps, Map<Long, History> historyMapByVideoId) {
        List<ScrapVideo> allScrap = ScrapVideo.of(scraps, historyMapByVideoId);
        return UserScrapResponse.builder()
                .allScrap(allScrap)
                .build();
    }
}

