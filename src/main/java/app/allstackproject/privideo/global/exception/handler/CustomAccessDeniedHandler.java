package app.allstackproject.privideo.global.exception.handler;

import static app.allstackproject.privideo.global.security.JwtAuthFilter.SECURITY_EXCEPTION_KEY;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.FORBIDDEN_NO_PERMISSION;

import app.allstackproject.privideo.global.response.BaseErrorResponse;
import app.allstackproject.privideo.global.response.status.ResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) {
        try {
            Object attr = req.getAttribute(SECURITY_EXCEPTION_KEY);
            ResponseStatus rs = (attr instanceof ResponseStatus) ? (ResponseStatus) attr : FORBIDDEN_NO_PERMISSION;

            BaseErrorResponse body = new BaseErrorResponse(rs, ex.getMessage());

            res.setStatus(rs.getStatus().value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(res.getWriter(), body);
        } catch (Exception ignore) {
        }
    }
}
