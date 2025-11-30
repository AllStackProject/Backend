package app.allstackproject.privideo.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryResponse {

    private List<VideoHistory> videos;

    public static HistoryResponse of(List<VideoHistory> histories) {
        return HistoryResponse.builder()
                .videos(histories)
                .build();
    }
}
