package app.allstackproject.privideo.common.response;

import app.allstackproject.privideo.common.response.status.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonPropertyOrder({"code", "status", "message"})
public class BaseErrorResponse implements ResponseStatus {

    private final int code;

    @JsonIgnore
    private final HttpStatus httpStatus;

    private final String message;

    public BaseErrorResponse(ResponseStatus responseStatus) {
        this.code = responseStatus.getCode();
        this.httpStatus = responseStatus.getStatus();
        this.message = responseStatus.getMessage();
    }

    public BaseErrorResponse(ResponseStatus responseStatus, String detailMessage) {
        this.code = responseStatus.getCode();
        this.httpStatus = responseStatus.getStatus();
        this.message =
                (detailMessage != null && !detailMessage.isBlank()) ? detailMessage : responseStatus.getMessage();
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
