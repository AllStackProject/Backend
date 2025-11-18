package app.allstackproject.privideo.common.enumStatus;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_HOME_FILTER;

import app.allstackproject.privideo.common.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum FilterType {
    RECOMMEND("recommend"),
    RECENT("recent"),
    POPULAR("popular");

    private final String value;

    FilterType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static FilterType from(String value) {
        for (FilterType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new ApiException(INVALID_HOME_FILTER);
    }
}
