package app.allstackproject.privideo.domain.video.service;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.CLOUD_FRONT_SIGN_FAIL;
import static java.nio.charset.StandardCharsets.UTF_8;

import app.allstackproject.privideo.global.exception.ApiException;
import jakarta.servlet.http.HttpServletResponse;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CloudFrontCookieService {

    @Value("${cloud.aws.cloudfront.distribution-domain}")
    private String DISTRIBUTION_DOMAIN;

    @Value("${cloud.aws.cloudfront.key-pair-id}")
    private String KEY_PAIR_ID;

    @Value("${cloud.aws.cloudfront.private-key-base64}")
    private String PRIVATE_KEY_BASE64;

    @Value("${cloud.aws.cloudfront.cookie-ttl-seconds}")
    private long COOKIE_TTL_SECONDS;

    @Value("${cloud.aws.cloudfront.cookie-domain}")
    private String COOKIE_DOMAIN;

    @Value("${cloud.aws.cloudfront.fallback-enabled:true}")
    private boolean fallbackEnabled;

    private PrivateKey cachedPrivateKey;

    /**
     * CloudFront 서명된 쿠키 추가 (실패 시 fallback 처리)
     *
     * @return true if CloudFront cookies added, false if fallback to S3
     */
    public boolean addSignedCookies(HttpServletResponse response, String videoHlsPrefix) {
        if (!fallbackEnabled) {
            addSignedCookiesInternal(response, videoHlsPrefix);
            return true;
        }

        try {
            addSignedCookiesInternal(response, videoHlsPrefix);
            log.info("CloudFront signed cookies added successfully for: {}", videoHlsPrefix);
            return true;
        } catch (Exception e) {
            log.error("CloudFront signing failed, falling back to S3 direct access: {}", videoHlsPrefix, e);
            return false;
        }
    }

    private void addSignedCookiesInternal(HttpServletResponse response, String videoHlsPrefix) {
        long expirationTime = Instant.now().getEpochSecond() + COOKIE_TTL_SECONDS;

        String resource = String.format(
                "https://%s/%s*", DISTRIBUTION_DOMAIN, videoHlsPrefix
        );

        String policyJson = String.format(
                "{\"Statement\":[{\"Resource\":\"%s\",\"Condition\":{\"DateLessThan\":{\"AWS:EpochTime\":%d}}}]}",
                resource, expirationTime
        );

        String encodedPolicy = urlSafeBase64(policyJson.getBytes(UTF_8));
        String signature = signPolicy(policyJson);

        addCookie(response, "CloudFront-Policy", encodedPolicy);
        addCookie(response, "CloudFront-Signature", signature);
        addCookie(response, "CloudFront-Key-Pair-Id", KEY_PAIR_ID);
    }

    private String signPolicy(String policyJson) {
        try {
            PrivateKey privateKey = loadPrivateKey();
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(privateKey);
            signer.update(policyJson.getBytes(UTF_8));
            byte[] sig = signer.sign();
            return urlSafeBase64(sig);
        } catch (Exception e) {
            log.error("Failed to sign CloudFront policy", e);
            throw new ApiException(CLOUD_FRONT_SIGN_FAIL);
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }

        log.info("Loading CloudFront private key from Base64");

        byte[] decoded = Base64.getDecoder().decode(PRIVATE_KEY_BASE64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        cachedPrivateKey = kf.generatePrivate(keySpec);

        log.info("Private key loaded and cached");
        return cachedPrivateKey;
    }

    private String urlSafeBase64(byte[] input) {
        String b64 = Base64.getEncoder().encodeToString(input);
        return b64.replace('+', '-')
                .replace('=', '_')
                .replace('/', '~');
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        String cookieHeader = String.format(
                "%s=%s; Path=/; Domain=%s; Max-Age=%d; SameSite=None; Secure",
                name,
                value,
                COOKIE_DOMAIN,
                COOKIE_TTL_SECONDS
        );

        response.addHeader("Set-Cookie", cookieHeader);

        log.debug("Set-Cookie: {} = {} (domain: {})",
                name,
                value.substring(0, Math.min(30, value.length())) + "...",
                COOKIE_DOMAIN);
    }
}