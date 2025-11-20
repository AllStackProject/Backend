package app.allstackproject.privideo.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CdnUrlProvider {
    @Value("${cdn.base-url}")
    private String baseUrl;

    public String generateFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        return baseUrl + "/" + fileKey;
    }
}
