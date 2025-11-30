package app.allstackproject.privideo.domain.admin.dto;

import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenderCountDto {
    private final GenderType gender;
    private final Long count;
}
