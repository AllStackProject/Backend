package app.allstackproject.privideo.common.response.status;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BaseExceptionResponseStatus implements ResponseStatus {

    /**
     * 1000: 요청 성공 (OK)
     */
    SUCCESS(1000, HttpStatus.OK, "요청에 성공하였습니다."),

    /**
     * 2000: Request 오류 (BAD_REQUEST)
     */
    BAD_REQUEST(2000, HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    URL_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "유효하지 않은 URL 입니다."),
    METHOD_NOT_ALLOWED(2002, HttpStatus.METHOD_NOT_ALLOWED, "해당 URL에서는 지원하지 않는 HTTP Method 입니다."),
    HTTP_MESSAGE_NOT_READABLE(2003, HttpStatus.BAD_REQUEST, "request body 양식에 문제가 있습니다"),

    /**
     * 3000: Server 오류 (INTERNAL_SERVER_ERROR)
     */
    SERVER_ERROR(3000, HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생하였습니다."),
    DB_TEMPORARY_UNAVAILABLE(3001, HttpStatus.SERVICE_UNAVAILABLE, "일시적으로 데이터를 처리할 수 없습니다."),
    DB_CONSTRAINT_VIOLATE(3004, HttpStatus.BAD_REQUEST, "DB 무결성에 적합하지 않습니다."),

    /**
     * 4000: Authentication 오류
     */
    UNSUPPORTED_TOKEN_TYPE(4001, HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다."),
    INVALID_TOKEN(4002, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(4003, HttpStatus.UNAUTHORIZED, "만료된 token 입니다."),
    FORBIDDEN_ORG_MISMATCH(4004, HttpStatus.FORBIDDEN, "요청된 조직과 토큰의 조직이 다릅니다."),
    FORBIDDEN_NO_PERMISSION(4005, HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다."),

    /**
     * 5000: User 오류
     */
    INVALID_USER_SIGNUP(5001, HttpStatus.BAD_REQUEST, "회원가입 요청에서 유효하지 않은 값이 존재합니다."),
    DUPLICATE_EMAIL(5002, HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다."),
    INVALID_ORG_CODE(5003, HttpStatus.BAD_REQUEST, "유효하지 않은 조직 코드입니다."),
    INVALID_USER_LOGIN(5004, HttpStatus.BAD_REQUEST, "로그인 요청에서 유효하지 않은 값이 존재합니다"),
    USER_NOT_FOUND(5005, HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_PASSWORD(5006, HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다.");

    /**
     * 6000: Comment 오류
     */
    //COMMENT_NOT_FOUND(6001, HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

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
