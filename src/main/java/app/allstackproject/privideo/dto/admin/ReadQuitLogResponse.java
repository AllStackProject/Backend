package app.allstackproject.privideo.dto.admin;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadQuitLogResponse {
    private List<QuitLogItem> highQuitRateLogs;

    private List<QuitLogItem> lowQuitRateLogs;

    private ReadQuitLogResponse(List<QuitLogItem> highQuitRateLogs, List<QuitLogItem> lowQuitRateLogs) {
        this.highQuitRateLogs = highQuitRateLogs;
        this.lowQuitRateLogs = lowQuitRateLogs;
    }

    public static ReadQuitLogResponse of(List<QuitLogItem> logs) {
        if (logs == null || logs.isEmpty()) {
            return new ReadQuitLogResponse(List.of(), List.of());
        }

        List<QuitLogItem> distinct = logs.stream()
                .collect(Collectors.toMap(
                        item -> item.getTitle() + "|" + item.getCreatedAt().toString(),
                        item -> item,
                        (a, b) -> a
                ))
                .values()
                .stream()
                .toList();

        List<QuitLogItem> sorted = distinct.stream()
                .sorted(Comparator.comparing(QuitLogItem::getQuitRate))
                .toList();

        List<QuitLogItem> lowQuitRateLogs = sorted.stream()
                .limit(3)
                .toList();

        List<QuitLogItem> highQuitRateLogs = sorted.stream()
                .skip(Math.max(0, sorted.size() - 3))
                .toList();

        return new ReadQuitLogResponse(highQuitRateLogs, lowQuitRateLogs);
    }

}
