package app.allstackproject.privideo.common.jwt;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.EXPIRED_TOKEN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_TOKEN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.UNSUPPORTED_TOKEN_TYPE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.WRONG_SIGNATURE_JWT;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class JwtProvider {
    private final Key key;
    private final long expTime;

    public JwtProvider(
            @Value("${secret.jwt.secret-key}") final String secretKey,
            @Value("${secret.jwt.expired-in}") final long expTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expTime = expTime;
    }

    public String createBootstrapToken(Long userId) {
        return sign(Map.of("userId", userId), expTime);
    }

    public String createOrgToken(OrgTokenDto orgTokenDto) {
        return sign(Map.of(
                "userId", orgTokenDto.getUserId(),
                "memberId", orgTokenDto.getMemberId(),
                "orgId", orgTokenDto.getOrgId(),
                "orgRole", orgTokenDto.getOrgRole(),
                "orgPermission", orgTokenDto.getOrgPermission()), expTime
        );
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public Long getMemberId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    public boolean isValidToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token);
            return claims.getBody().getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            throw new ApiException(EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new ApiException(UNSUPPORTED_TOKEN_TYPE);
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            throw new ApiException(INVALID_TOKEN);
        } catch (SignatureException e) {
            throw new ApiException(WRONG_SIGNATURE_JWT);
        } catch (JwtException e) {
            log.error("[JwtTokenProvider.validateAccessToken]", e);
            throw e;
        }
    }

    private String sign(Map<String, Object> claims, long expireTime) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expireTime);

        return Jwts.builder()
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
