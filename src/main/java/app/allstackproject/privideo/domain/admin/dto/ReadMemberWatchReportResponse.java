package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadMemberWatchReportResponse {
    private Long totalWatchedVideoCnt;

    private List<String> mostWatchedCategories;

    private List<MonthlyWatchItem> monthlyWatchedCnts;

    private ReadMemberWatchReportResponse(Long totalWatchedVideoCnt, List<String> mostWatchedCategories,
                                          List<MonthlyWatchItem> monthlyWatchedCnts) {
        this.totalWatchedVideoCnt = totalWatchedVideoCnt;
        this.mostWatchedCategories = mostWatchedCategories;
        this.monthlyWatchedCnts = monthlyWatchedCnts;
    }

    public static ReadMemberWatchReportResponse of(MemberWatchReport memberWatchReport) {
        return new ReadMemberWatchReportResponse(memberWatchReport.getTotalWatchedVideoCnt(),
                memberWatchReport.getMostWatchedCategories(), memberWatchReport.getMonthlyWatchedCnts());
    }
}
