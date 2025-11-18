package app.allstackproject.privideo.common.exception;

import app.allstackproject.privideo.common.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ResponseStatus responseStatus;

    public ApiException(ResponseStatus responseStatus) {
        super(responseStatus.getMessage());
        this.responseStatus = responseStatus;
    }

    public ApiException(ResponseStatus exceptionStatus, String message) {
        super(message);
        this.responseStatus = exceptionStatus;
    }
}
