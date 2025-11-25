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
        Long maleNum = genderMap.get(MALE) == null ? 0 : genderMap.get(MALE);
        Long femaleNum = genderMap.get(FEMALE) == null ? 0 : genderMap.get(FEMALE);

        return new ReadOrgGenderReportResponse(maleNum, femaleNum);
    }
}
