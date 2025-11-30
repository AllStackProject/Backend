package app.allstackproject.privideo.domain.member.enums;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_AGE_TYPE;

import app.allstackproject.privideo.global.exception.ApiException;
import lombok.Getter;

@Getter
public enum AgeType {
    TEN(10),
    TWENTY(20),
    THIRTY(30),
    FORTY(40),
    FIFTY(50),
    SIXTY(60);

    private final int value;

    AgeType(int value) {
        this.value = value;
    }

    public static AgeType from(int value) {
        for (AgeType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new ApiException(INVALID_AGE_TYPE);
    }
}
