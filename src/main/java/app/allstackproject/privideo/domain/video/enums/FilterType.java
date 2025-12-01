package app.allstackproject.privideo.domain.video.enums;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_HOME_FILTER;

import app.allstackproject.privideo.global.exception.ApiException;
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
