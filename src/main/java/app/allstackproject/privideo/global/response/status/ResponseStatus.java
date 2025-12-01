package app.allstackproject.privideo.global.response.status;

import org.springframework.http.HttpStatus;

public interface ResponseStatus {

    int getCode();

    HttpStatus getStatus();

    String getMessage();

}
