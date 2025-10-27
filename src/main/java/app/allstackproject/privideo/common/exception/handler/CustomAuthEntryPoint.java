package app.allstackproject.privideo.common.exception.handler;

import static app.allstackproject.privideo.common.filter.JwtAuthFilter.SECURITY_EXCEPTION_KEY;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_TOKEN;

import app.allstackproject.privideo.common.response.BaseErrorResponse;
import app.allstackproject.privideo.common.response.status.ResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) {
        try {
            Object attr = req.getAttribute(SECURITY_EXCEPTION_KEY);
            ResponseStatus rs = (attr instanceof ResponseStatus) ? (ResponseStatus) attr : INVALID_TOKEN;
            
            BaseErrorResponse body = new BaseErrorResponse(rs, ex.getMessage());

            res.setStatus(rs.getStatus().value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");

            objectMapper.writeValue(res.getWriter(), body);
        } catch (Exception ignore) {
        }
    }
}
