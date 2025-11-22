package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CLOUD_FRONT_SIGN_FAIL;
import static java.nio.charset.StandardCharsets.UTF_8;

import app.allstackproject.privideo.common.exception.ApiException;
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

    @Value("${cloud.aws.cloudfront.domain}")
    private String CDN_BASE_URL;

    @Value("${cloud.aws.cloudfront.key-pair-id}")
    private String KEY_PAIR_ID;

    @Value("${cloud.aws.cloudfront.private-key-base64}")
    private String PRIVATE_KEY_BASE64;

    @Value("${cloud.aws.cloudfront.cookie-ttl-seconds}")
    private long COOKIE_TTL_SECONDS;

    private PrivateKey cachedPrivateKey;

    public void addSignedCookies(HttpServletResponse response, String videoHlsPrefix) {
        long expirationTime = Instant.now().getEpochSecond() + COOKIE_TTL_SECONDS;

        String domain = CDN_BASE_URL
                .replace("https://", "")
                .replace("http://", "");

        String resource = String.format("https://%s/%s/*", domain, videoHlsPrefix);

        String policyJson = String.format(
                "{\"Statement\":[{\"Resource\":\"%s\",\"Condition\":{\"DateLessThan\":{\"AWS:EpochTime\":%d}}}]}",
                resource, expirationTime
        );

        String encodedPolicy = urlSafeBase64(policyJson.getBytes(UTF_8));
        String signature = signPolicy(policyJson);

        addCookie(response, "CloudFront-Policy", encodedPolicy, domain);
        addCookie(response, "CloudFront-Signature", signature, domain);
        addCookie(response, "CloudFront-Key-Pair-Id", KEY_PAIR_ID, domain);

        log.info("CloudFront signed cookies added for resource: {}", resource);
        log.info("Cookie domain: {}", domain);
        log.info("Expiration: {} ({})", expirationTime, Instant.ofEpochSecond(expirationTime));
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

    private void addCookie(HttpServletResponse response, String name, String value, String domain) {
        String cookieHeader = String.format(
                "%s=%s; Path=/; Domain=%s; Max-Age=%d; SameSite=None; Secure",
                name,
                value,
                domain,
                COOKIE_TTL_SECONDS
        );

        response.addHeader("Set-Cookie", cookieHeader);

        log.debug("Set-Cookie: {} = {} (domain: {})",
                name,
                value.substring(0, Math.min(30, value.length())) + "...",
                domain);
    }
}