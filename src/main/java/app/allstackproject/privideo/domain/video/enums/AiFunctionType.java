package app.allstackproject.privideo.domain.video.enums;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_AI_FUNCTION_TYPE;

import app.allstackproject.privideo.global.exception.ApiException;
import lombok.Getter;

@Getter
public enum AiFunctionType {
    NONE,
    QUIZ,
    FEEDBACK,
    SUMMARY;

    public static AiFunctionType from(String value) {
        try {
            return AiFunctionType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(INVALID_AI_FUNCTION_TYPE);
        }
    }
}
