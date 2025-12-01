package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadVideoRankResponse {
    private List<VideoRankItem> allVideoRank;

    private ReadVideoRankResponse(List<VideoRankItem> allVideoRank) {
        this.allVideoRank = allVideoRank;
    }

    public static ReadVideoRankResponse of(List<VideoRankItem> allVideoRank) {
        return new ReadVideoRankResponse(allVideoRank);
    }
}
