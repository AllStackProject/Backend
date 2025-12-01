package app.allstackproject.privideo.domain.admin.dto;

import static app.allstackproject.privideo.domain.user.dto.enums.GenderType.FEMALE;
import static app.allstackproject.privideo.domain.user.dto.enums.GenderType.MALE;

import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
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
