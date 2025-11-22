package app.allstackproject.privideo.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CdnUrlProvider {
    @Value("${cloud.aws.cloudfront.domain}")
    private String CDN_BASE_URL;

    public String generateImgUrl(String imgKey) {
        if (imgKey == null || imgKey.isBlank()) {
            return null;
        }
        return CDN_BASE_URL + "/" + imgKey;
    }
}
