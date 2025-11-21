package app.allstackproject.privideo.service.video;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CLOUD_FRONT_SIGN_FAIL;
import static java.nio.charset.StandardCharsets.UTF_8;

import app.allstackproject.privideo.common.exception.ApiException;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CloudFrontCookieService {

    @Value("${cloud.aws.cloudfront.domain}")
    private String CDN_BASE_URL;

    @Value("${cloud.aws.cloudfront.key-pair-id}")
    private String KEY_PAIR_ID;

    @Value("${cloud.aws.cloudfront.private-key-path}")
    private String PRIVATE_KEY_PATH;

    @Value("${cloud.aws.cloudfront.cookie-ttl-seconds}")
    private long COOKIE_TTL_SECONDS;

    public void addSignedCookies(HttpServletResponse response, String videoHlsPrefix) {
        String resource = CDN_BASE_URL + "/" + videoHlsPrefix + "*";

        String policyJson = """
                {
                  "Statement": [{
                    "Resource": "%s",
                    "Condition": {
                      "DateLessThan": {"AWS:EpochTime": %d}
                    }
                  }]
                }
                """.formatted(resource, COOKIE_TTL_SECONDS);

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
            throw new ApiException(CLOUD_FRONT_SIGN_FAIL);
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String pem = Files.readString(Path.of(PRIVATE_KEY_PATH));
        String clean = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private String urlSafeBase64(byte[] input) {
        String b64 = Base64.getEncoder().encodeToString(input);
        return b64.replace('+', '-')
                .replace('=', '_')
                .replace('/', '~');
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(false)
                .secure(false)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
