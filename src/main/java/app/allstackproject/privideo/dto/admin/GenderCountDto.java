package app.allstackproject.privideo.dto.admin;

import app.allstackproject.privideo.common.enumStatus.GenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenderCountDto {
    private final GenderType gender;
    private final Long count;
}
