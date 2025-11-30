package app.allstackproject.privideo.domain.admin.dto;

import app.allstackproject.privideo.domain.member.enums.AgeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgeCountDto {
    private AgeType age;

    private Long count;

    public AgeCountDto(Integer age, Long count) {
        this.age = AgeType.from(age);
        this.count = count;
    }
}
