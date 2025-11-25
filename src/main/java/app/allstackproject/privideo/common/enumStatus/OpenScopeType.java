package app.allstackproject.privideo.common.enumStatus;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_OPEN_SCOPE_TYPE;

import app.allstackproject.privideo.common.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum OpenScopeType {
    PUBLIC,
    GROUP,
    PRIVATE;

    @JsonCreator
    public static OpenScopeType from(String value) {
        try {
            return OpenScopeType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new ApiException(INVALID_OPEN_SCOPE_TYPE);
        }
    }
}
