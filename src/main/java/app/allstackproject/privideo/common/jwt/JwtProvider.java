package app.allstackproject.privideo.common.jwt;

import static app.allstackproject.privideo.common.enumStatus.TokenType.BOOTSTRAP;
import static app.allstackproject.privideo.common.enumStatus.TokenType.ORG;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.EXPIRED_TOKEN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_TOKEN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.UNSUPPORTED_TOKEN_TYPE;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
        return sign(Map.of(
                "tokenType", BOOTSTRAP.name(),
                "userId", userId
        ), expTime);
    }

    public String createOrgToken(OrgTokenDto dto) {
        return sign(Map.of(
                "tokenType", ORG.name(),
                "userId", dto.getUserId(),
                "memberId", dto.getMemberId(),
                "orgId", dto.getOrgId(),
                "orgJoinStatus", dto.getOrgJoinStatus(),
                "orgIsAdmin", dto.getOrgIsAdmin(),
                "orgPermission", dto.getOrgPermission()
        ), expTime);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new ApiException(EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new ApiException(UNSUPPORTED_TOKEN_TYPE);
        } catch (IllegalArgumentException | JwtException e) {
            throw new ApiException(INVALID_TOKEN);
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
}
