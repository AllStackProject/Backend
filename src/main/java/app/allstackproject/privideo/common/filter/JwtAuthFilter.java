package app.allstackproject.privideo.common.filter;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.FORBIDDEN_ORG_MISMATCH;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_TOKEN;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.enumStatus.PermissionType;
import app.allstackproject.privideo.common.enumStatus.TokenType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public final static String ACCESS_TOKEN_HEADER = "Authorization";
    public final static String TOKEN_PREFIX = "Bearer ";
    public static final String SECURITY_EXCEPTION_KEY = "SECURITY_RESPONSE_STATUS";

    private static final Set<String> EXCLUDED_ROOTS = Set.of(
            "user", "org",
            "error", "favicon.ico",
            "public", "assets", "static"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader(ACCESS_TOKEN_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }
        String token = header.substring(TOKEN_PREFIX.length());

        try {
            jwtProvider.validate(token);
            Claims c = jwtProvider.getClaims(token);

            String tokenTypeStr = c.get("tokenType", String.class);
            if (!StringUtils.hasText(tokenTypeStr)) {
                throw new ApiException(INVALID_TOKEN);
            }

            TokenType tokenType;
            try {
                tokenType = TokenType.valueOf(tokenTypeStr);
            } catch (IllegalArgumentException ex) {
                throw new ApiException(INVALID_TOKEN, ex.getMessage());
            }

            String pathOrgId = extractOrgIdFromPath(req);
            Authentication auth;

            switch (tokenType) {
                case BOOTSTRAP -> {
                    String firstPath = firstSegment(req);
                    if (pathOrgId != null && !EXCLUDED_ROOTS.contains(firstPath)) {
                        throw new ApiException(FORBIDDEN_ORG_MISMATCH);
                    }

                    Long userId = c.get("userId", Number.class) != null
                            ? c.get("userId", Number.class).longValue()
                            : null;
                    if (userId == null) {
                        throw new ApiException(INVALID_TOKEN);
                    }

                    List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("bootstrap:granted"));
                    var principal = new AuthPrincipal(userId, null, null, null, null, TokenType.BOOTSTRAP);
                    auth = new UsernamePasswordAuthenticationToken(principal, null, auths);
                }

                case ORG -> {
                    Long userId = getLong(c, "userId");
                    Long memberId = getLong(c, "memberId");
                    Long orgId = getLong(c, "orgId");
                    String orgJoinStatus = getString(c, "orgJoinStatus");
                    String orgIsAdmin = getString(c, "orgIsAdmin");
                    Number n = c.get("orgPermission", Number.class);
                    Long perm = n != null ? n.longValue() : 0L;

                    if (pathOrgId != null && !Objects.equals(pathOrgId, String.valueOf(orgId))) {
                        throw new ApiException(FORBIDDEN_ORG_MISMATCH);
                    }

                    // TODO: Redis 최신 권한 검증

                    List<GrantedAuthority> auths = new ArrayList<>(List.of(new SimpleGrantedAuthority("org:granted"),
                            new SimpleGrantedAuthority("org:" + orgJoinStatus)));

                    if (orgIsAdmin.equals("true")) {
                        auths.add(new SimpleGrantedAuthority("org:admin"));
                    }

                    if (PermissionType.has(perm, PermissionType.VIDEO_QUIZ_MANAGE)) {
                        auths.add(new SimpleGrantedAuthority("perm:video_quiz_manage"));
                    }
                    if (PermissionType.has(perm, PermissionType.STATS_REPORT)) {
                        auths.add(new SimpleGrantedAuthority("perm:stats_report"));
                    }
                    if (PermissionType.has(perm, PermissionType.NOTICE)) {
                        auths.add(new SimpleGrantedAuthority("perm:notice"));
                    }
                    if (PermissionType.has(perm, PermissionType.ORG_SETTING)) {
                        auths.add(new SimpleGrantedAuthority("perm:org_setting"));
                    }

                    var principal = new AuthPrincipal(userId, memberId, orgId, Boolean.getBoolean(orgIsAdmin), perm,
                            TokenType.ORG);
                    auth = new UsernamePasswordAuthenticationToken(principal, null, auths);
                }

                default -> throw new ApiException(INVALID_TOKEN);
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (ApiException e) {
            throw toSecurityException(req, e);
        } catch (Exception e) {
            throw toSecurityException(req, new ApiException(INVALID_TOKEN, e.getMessage()));
        }

        chain.doFilter(req, res);
    }

    private static String firstSegment(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri == null || uri.isEmpty() || "/".equals(uri)) {
            return null;
        }
        String[] raw = uri.split("/");
        for (String s : raw) {
            if (s == null || s.isEmpty()) {
                continue;
            }
            int semi = s.indexOf(';');
            return semi >= 0 ? s.substring(0, semi) : s;
        }
        return null;
    }

    private String extractOrgIdFromPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri == null || uri.isEmpty() || "/".equals(uri)) {
            return null;
        }

        String[] raw = uri.split("/");
        String first = null;
        for (String s : raw) {
            if (s == null || s.isEmpty()) {
                continue;
            }
            first = cleanSegment(s);
            break;
        }
        if (first == null) {
            return null;
        }

        if (EXCLUDED_ROOTS.contains(first)) {
            return null;
        }

        return isAllDigits(first) ? first : null;
    }

    private static Long getLong(Claims c, String key) {
        Number n = c.get(key, Number.class);
        if (n == null) {
            throw new ApiException(INVALID_TOKEN);
        }
        return n.longValue();
    }

    private static Integer getInt(Claims c, String key) {
        Number n = c.get(key, Number.class);
        if (n == null) {
            throw new ApiException(INVALID_TOKEN);
        }
        return n.intValue();
    }

    private static String getString(Claims c, String key) {
        String s = c.get(key, String.class);
        if (s == null) {
            throw new ApiException(INVALID_TOKEN);
        }
        return s;
    }

    private RuntimeException toSecurityException(HttpServletRequest req, ApiException e) {
        SecurityContextHolder.clearContext();
        req.setAttribute(SECURITY_EXCEPTION_KEY, e.getResponseStatus());
        return switch (e.getResponseStatus().getStatus()) {
            case FORBIDDEN -> new AccessDeniedException(e.getMessage(), e);
            case UNAUTHORIZED -> new InsufficientAuthenticationException(e.getMessage(), e);
            default -> new InsufficientAuthenticationException(e.getMessage(), e);
        };
    }

    private static String cleanSegment(String s) {
        int semi = s.indexOf(';');
        return semi >= 0 ? s.substring(0, semi) : s;
    }

    private static boolean isAllDigits(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
