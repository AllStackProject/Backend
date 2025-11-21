package app.allstackproject.privideo.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CdnUrlProvider {
    @Value("${cdn.base-url}")
    private String baseUrl;

    public String generateImgUrl(String imgKey) {
        if (imgKey == null || imgKey.isBlank()) {
            return null;
        }
        return baseUrl + "/" + imgKey;
    }
}
