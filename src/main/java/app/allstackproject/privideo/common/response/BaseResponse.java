package app.allstackproject.privideo.common.response;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.SUCCESS;

import app.allstackproject.privideo.common.response.status.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseResponse<T> implements ResponseStatus {

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    @JsonInclude(Include.NON_NULL)
    private final T result;

    public BaseResponse(T result) {
        this.code = SUCCESS.getCode();
        this.httpStatus = SUCCESS.getStatus();
        this.message = SUCCESS.getMessage();
        this.result = result;
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
