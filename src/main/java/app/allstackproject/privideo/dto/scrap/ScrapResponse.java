package app.allstackproject.privideo.dto.scrap;

import app.allstackproject.privideo.dto.history.HistoryItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScrapResponse {

    private List<HistoryItem> allScrap;

    public static ScrapResponse of(List<HistoryItem> historyItems) {
        return new ScrapResponse(historyItems);
    }
}

