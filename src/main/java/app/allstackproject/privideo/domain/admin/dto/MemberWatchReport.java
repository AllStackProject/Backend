package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberWatchReport {
    private Long totalWatchedVideoCnt;

    private List<String> mostWatchedCategories;

    private List<MonthlyWatchItem> monthlyWatchedCnts;
}
