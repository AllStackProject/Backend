package app.allstackproject.privideo.common.response;

import app.allstackproject.privideo.common.response.status.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseErrorResponse implements ResponseStatus {

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    public BaseErrorResponse(ResponseStatus responseStatus) {
        this.code = responseStatus.getCode();
        this.httpStatus = responseStatus.getStatus();
        this.message = responseStatus.getMessage();
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
