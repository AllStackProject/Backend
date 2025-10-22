package app.allstackproject.privideo.common.exception.handler;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.*;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseErrorResponse;
import app.allstackproject.privideo.common.response.status.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

@Slf4j
@RestControllerAdvice
public class BaseExceptionControllerAdvice {

    // === 1) 우리 커스텀 예외: 예외가 들고 있는 상태를 그대로 사용 ===
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseErrorResponse> handleApi(ApiException e, HttpServletRequest req) {
        logWarnOrError(e.getResponseStatus(), e, req);
        return ResponseEntity.status(e.getResponseStatus().getStatus())
                .body(new BaseErrorResponse(e.getResponseStatus()));
    }

    // === 2) 스프링 표준/검증/파싱 예외 매핑 ===
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                          HttpServletRequest req) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String msgSummary = fieldErrors.stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("요청 본문 검증 실패");

        log.warn("[400 Validation @Valid] {} {} -> {}", req.getMethod(), req.getRequestURI(), msgSummary);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseErrorResponse(BAD_REQUEST));
    }

    @ExceptionHandler({TypeMismatchException.class, ConstraintViolationException.class})
    public ResponseEntity<BaseErrorResponse> handleValidation(Exception e, HttpServletRequest req) {
        log.warn("[400 Validation @Validated] {} {}", req.getMethod(), req.getRequestURI(), e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseErrorResponse(BAD_REQUEST));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class})
    public ResponseEntity<BaseErrorResponse> handleUnreadable(Exception e, HttpServletRequest req) {
        log.warn("[400 PayloadNotReadable] {} {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        String body = getRequestBody(req);
        if (!body.isEmpty()) {
            log.debug("[payload snippet] {}", body);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseErrorResponse(HTTP_MESSAGE_NOT_READABLE));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                    HttpServletRequest req) {
        log.warn("[405 MethodNotAllowed] {} {}", req.getMethod(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new BaseErrorResponse(METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<BaseErrorResponse> handleNotFound(Exception e, HttpServletRequest req) {
        log.warn("[404 NotFound] {} {}", req.getMethod(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseErrorResponse(URL_NOT_FOUND));
    }

    // === 3) 프레임워크 ErrorResponse (스프링 6+)의 상태코드 활용 (fallback 헬퍼) ===
    private ResponseEntity<BaseErrorResponse> respondFromFramework(ErrorResponse er, BaseErrorResponse body) {
        HttpStatusCode code = er.getStatusCode();
        return ResponseEntity.status(code).body(body);
    }

    // === 4) 마지막 방어선 ===
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseErrorResponse> handleRuntime(RuntimeException e, HttpServletRequest req) {
        log.error("[500 RuntimeException] {} {}", req.getMethod(), req.getRequestURI(), e);
        // ErrorResponse 구현체면 그 코드 사용, 아니면 500
        if (e instanceof ErrorResponse er) {
            return respondFromFramework(er, new BaseErrorResponse(SERVER_ERROR));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseErrorResponse(SERVER_ERROR));
    }

    private void logWarnOrError(ResponseStatus status, Exception e, HttpServletRequest req) {
        HttpStatus hs = status.getStatus();
        if (hs.is5xxServerError()) {
            log.error("[{} {}] {} {}", hs.value(), status.getCode(), req.getMethod(), req.getRequestURI(), e);
        } else {
            log.warn("[{} {}] {} {}", hs.value(), status.getCode(), req.getMethod(), req.getRequestURI(), e.toString());
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return "";
        }
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length == 0) {
            return "";
        }
        String body = new String(buf, java.nio.charset.StandardCharsets.UTF_8);
        int LIMIT = 2_000;
        return body.length() > LIMIT ? body.substring(0, LIMIT) + "...(truncated)" : body;
    }
}
