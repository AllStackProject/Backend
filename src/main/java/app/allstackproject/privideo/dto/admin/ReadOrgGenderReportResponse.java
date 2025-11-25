package app.allstackproject.privideo.dto.admin;

import static app.allstackproject.privideo.common.enumStatus.GenderType.FEMALE;
import static app.allstackproject.privideo.common.enumStatus.GenderType.MALE;

import app.allstackproject.privideo.common.enumStatus.GenderType;
import java.util.Map;
import lombok.Getter;

@Getter
public class ReadOrgGenderReportResponse {
    private Long male;

    private Long female;

    private ReadOrgGenderReportResponse(Long male, Long female) {
        this.male = male;
        this.female = female;
    }

    public static ReadOrgGenderReportResponse of(Map<GenderType, Long> genderMap) {
        Long maleNum = genderMap.getOrDefault(MALE, 0L);
        Long femaleNum = genderMap.getOrDefault(FEMALE, 0L);

        return new ReadOrgGenderReportResponse(maleNum, femaleNum);
    }
}
