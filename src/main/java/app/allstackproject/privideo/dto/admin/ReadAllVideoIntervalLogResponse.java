package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllVideoIntervalLogResponse {
    private List<ReadAllVideoIntervalLogItem> allVideoInterval;

    private ReadAllVideoIntervalLogResponse(List<ReadAllVideoIntervalLogItem> allVideoInterval) {
        this.allVideoInterval = allVideoInterval;
    }

    public static ReadAllVideoIntervalLogResponse of(List<ReadAllVideoIntervalLogItem> allVideoInterval) {
        return new ReadAllVideoIntervalLogResponse(allVideoInterval);
    }
}
