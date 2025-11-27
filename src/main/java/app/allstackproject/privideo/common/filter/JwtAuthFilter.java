package app.allstackproject.privideo.common.filter;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.FORBIDDEN_ORG_MISMATCH;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_TOKEN;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.enumStatus.PermissionType;
import app.allstackproject.privideo.common.enumStatus.TokenType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final OrgRedisRepository orgRedisRepository;

    public final static String ACCESS_TOKEN_HEADER = "Authorization";
    public final static String TOKEN_PREFIX = "Bearer ";
    public static final String SECURITY_EXCEPTION_KEY = "SECURITY_RESPONSE_STATUS";

    private static final Set<String> EXCLUDED_ROOTS = Set.of(
            "user", "orgs",
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

        log.warn("이거왜이래 [JwtAuthFilter] method={} uri={} token={}",
                req.getMethod(), req.getRequestURI(), token);

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

            String uri = req.getRequestURI();
            String firstPath = firstSegment(req);
            Authentication auth;

            switch (tokenType) {
                case BOOTSTRAP -> {
                    // BOOTSTRAP 토큰은 /orgs 경로에만 허용
//                    if (!"orgs".equals(firstPath)) {
                    if (!EXCLUDED_ROOTS.contains(firstPath)) {
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
                    String pathOrgId = extractOrgIdFromPath(uri, firstPath);

                    Long userId = getLongFlexible(c, "userId");
                    Long memberId = getLongFlexible(c, "memberId");
                    Long orgId = getLongFlexible(c, "orgId");

                    String orgJoinStatus = getStringFlexible(c, "orgJoinStatus");
                    boolean orgIsAdmin = getBooleanFlexible(c, "orgIsAdmin");

                    Long perm = getLongFlexible(c, "orgPermission");

                    if (pathOrgId != null && !Objects.equals(pathOrgId, String.valueOf(orgId))) {
                        throw new ApiException(FORBIDDEN_ORG_MISMATCH);
                    }

                    Long redisPermission = null;
                    try {
                        redisPermission = orgRedisRepository.getMemberPermission(orgId, memberId);
                        if (redisPermission != null && !redisPermission.equals(perm)) {
                            log.info("권한 변경 감지 - memberId: {}, 기존: {}, 최신: {}", memberId, perm, redisPermission);
                            String newToken = jwtProvider.createOrgToken(OrgTokenDto.builder()
                                    .userId(userId)
                                    .memberId(memberId)
                                    .orgId(orgId)
                                    .orgJoinStatus(orgJoinStatus)
                                    .orgIsAdmin(orgIsAdmin)
                                    .orgPermission(redisPermission)
                                    .build());
                            res.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + newToken);
                        }
                    } catch (Exception e) {
                        log.warn("Redis 조회 실패", e);
                    }

                    long finalPerm = (redisPermission != null) ? redisPermission : perm;

                    List<GrantedAuthority> auths = new ArrayList<>(List.of(
                            new SimpleGrantedAuthority("org:granted"),
                            new SimpleGrantedAuthority("org:" + orgJoinStatus)
                    ));

                    if (orgIsAdmin) {
                        auths.add(new SimpleGrantedAuthority("org:admin"));
                    }
                    if (PermissionType.has(finalPerm, PermissionType.VIDEO_MANAGE)) {
                        auths.add(new SimpleGrantedAuthority("perm:video_manage"));
                    }
                    if (PermissionType.has(finalPerm, PermissionType.STATS_REPORT)) {
                        auths.add(new SimpleGrantedAuthority("perm:stats_report"));
                    }
                    if (PermissionType.has(finalPerm, PermissionType.NOTICE)) {
                        auths.add(new SimpleGrantedAuthority("perm:notice"));
                    }
                    if (PermissionType.has(finalPerm, PermissionType.ORG_SETTING)) {
                        auths.add(new SimpleGrantedAuthority("perm:org_setting"));
                    }

                    var principal = new AuthPrincipal(userId, memberId, orgId, orgIsAdmin, finalPerm, TokenType.ORG);
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

    /**
     * ORG 토큰용 orgId 추출
     * - /{orgId}/... 형식: 첫 번째 세그먼트가 숫자면 반환
     * - /admin/{orgId}/... 형식: 두 번째 세그먼트가 숫자면 반환
     */
    private String extractOrgIdFromPath(String uri, String firstPath) {
        if (!StringUtils.hasText(uri) || "/".equals(uri)) {
            return null;
        }

        if (firstPath != null && (EXCLUDED_ROOTS.contains(firstPath) || "orgs".equals(firstPath))) {
            return null;
        }

        String[] parts = uri.split("/");
        List<String> segments = new ArrayList<>();

        for (String raw : parts) {
            if (StringUtils.hasText(raw)) {
                segments.add(cleanSegment(raw));
            }
        }

        if (segments.isEmpty()) {
            return null;
        }

        if (isAllDigits(segments.get(0))) {
            return segments.get(0);
        }

        if ("admin".equals(segments.get(0)) && segments.size() > 1 && isAllDigits(segments.get(1))) {
            return segments.get(1);
        }

        return null;
    }

    private static Long getLongFlexible(Claims c, String key) {
        Object v = c.get(key);
        if (v == null) {
            throw new ApiException(INVALID_TOKEN);
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            return Long.parseLong(s);
        }
        throw new ApiException(INVALID_TOKEN);
    }

    private static String getStringFlexible(Claims c, String key) {
        Object v = c.get(key);
        if (v == null) {
            throw new ApiException(INVALID_TOKEN);
        }
        return String.valueOf(v);
    }

    private static boolean getBooleanFlexible(Claims c, String key) {
        Object v = c.get(key);
        if (v == null) {
            throw new ApiException(INVALID_TOKEN);
        }
        if (v instanceof Boolean b) {
            return b;
        }
        if (v instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        if (v instanceof Number n) {
            return n.intValue() != 0;
        }
        throw new ApiException(INVALID_TOKEN);
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
