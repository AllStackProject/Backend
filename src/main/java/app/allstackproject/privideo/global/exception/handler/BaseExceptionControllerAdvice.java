package app.allstackproject.privideo.global.exception.handler;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.*;

import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.response.BaseErrorResponse;
import app.allstackproject.privideo.global.response.status.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import javax.naming.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
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

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseErrorResponse> handleApi(ApiException e, HttpServletRequest req) {
        logWarnOrError(e.getResponseStatus(), e, req);
        return ResponseEntity.status(e.getResponseStatus().getStatus())
                .body(new BaseErrorResponse(e.getResponseStatus(), e.getMessage()));
    }

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

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<BaseErrorResponse> handleAccessDenied(Exception e, HttpServletRequest req) {
        log.warn("[403 Forbidden] {} {} -> {}", req.getMethod(), req.getRequestURI(), e.toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new BaseErrorResponse(FORBIDDEN_NO_PERMISSION));
    }

    @ExceptionHandler({InsufficientAuthenticationException.class, AuthenticationException.class})
    public ResponseEntity<BaseErrorResponse> handleUnauthenticated(Exception e, HttpServletRequest req) {
        log.warn("[401 Unauthorized] {} {} -> {}", req.getMethod(), req.getRequestURI(), e.toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BaseErrorResponse(INVALID_TOKEN));
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseErrorResponse> handleRuntime(RuntimeException e, HttpServletRequest req) {
        log.error("[500 RuntimeException] {} {}", req.getMethod(), req.getRequestURI(), e);
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
