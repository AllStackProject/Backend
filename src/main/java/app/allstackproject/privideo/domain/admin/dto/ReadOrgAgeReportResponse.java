package app.allstackproject.privideo.domain.admin.dto;

import app.allstackproject.privideo.domain.member.enums.AgeType;
import java.util.Map;
import lombok.Getter;

@Getter
public class ReadOrgAgeReportResponse {
    private Long ten;

    private Long twenty;

    private Long thirty;

    private Long forty;

    private Long fifty;

    private Long sixty;

    private ReadOrgAgeReportResponse(Long ten, Long twenty, Long thirty, Long forty, Long fifty, Long sixty) {
        this.ten = ten;
        this.twenty = twenty;
        this.thirty = thirty;
        this.forty = forty;
        this.fifty = fifty;
        this.sixty = sixty;
    }

    public static ReadOrgAgeReportResponse of(Map<AgeType, Long> ageMap) {
        Long tenNum = ageMap.getOrDefault(AgeType.TEN, 0L);
        Long twentyNum = ageMap.getOrDefault(AgeType.TWENTY, 0L);
        Long thirtyNum = ageMap.getOrDefault(AgeType.THIRTY, 0L);
        Long fortyNum = ageMap.getOrDefault(AgeType.FORTY, 0L);
        Long fiftyNum = ageMap.getOrDefault(AgeType.FIFTY, 0L);
        Long sixtyNum = ageMap.getOrDefault(AgeType.SIXTY, 0L);

        return new ReadOrgAgeReportResponse(
                tenNum,
                twentyNum,
                thirtyNum,
                fortyNum,
                fiftyNum,
                sixtyNum
        );
    }
}
