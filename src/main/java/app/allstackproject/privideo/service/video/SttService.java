package app.allstackproject.privideo.service.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttService {
    private final WebClient webClient;

    @Value("${rtzr.api.client-id}")
    private String clientId;

    @Value("${rtzr.api.client-secret}")
    private String clientSecret;

    @Value("${rtzr.api.base-url}")
    private String baseUrl;

    private String cachedAccessToken;
    private Instant tokenExpireAt;

    /**
     * 1단계: 파일 전사 요청 POST /v1/transcribe
     */
    public String requestTranscription(File audioFile) {
        String token = getAccessToken();
        log.info("전사 요청 시 사용되는 토큰 : " + token);

        // Whisper 모델 설정: 다중 언어 처리 (한국어, 영어, 일본어)
        TranscribeConfig config = new TranscribeConfig(
                "whisper",
                "multi",
                new String[]{"ko", "en", "ja"}
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(audioFile));
        builder.part("config", config);

        TranscribeResponse response = webClient.post()
                .uri(baseUrl + "/v1/transcribe")
                .header(HttpHeaders.AUTHORIZATION, "bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(TranscribeResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("STT 요청 실패: 응답 없음");
        }

        log.info("STT 요청 완료: transcribeId={}", response.id());
        return response.id();
    }

    /**
     * 2단계: 전사 결과 조회 (폴링) GET /v1/transcribe/{TRANSCRIBE_ID}
     */
    public String getTranscriptionResult(String transcribeId) {
        String token = getAccessToken();
        log.info("전사 결과 조회 시 사용되는 토큰 : " + token);

        int maxRetries = 60; // 최대 5분 대기 (5초 * 60)
        int retryCount = 0;

        while (retryCount < maxRetries) {
            TranscribeResultResponse result = webClient.get()
                    .uri(baseUrl + "/v1/transcribe/" + transcribeId)
                    .header(HttpHeaders.AUTHORIZATION, "bearer " + token)
                    .retrieve()
                    .bodyToMono(TranscribeResultResponse.class)
                    .block();

            if (result == null) {
                throw new RuntimeException("STT 결과 조회 실패");
            }

            String resultStatus = result.status();
            log.info("STT 상태 확인: status={}", resultStatus);

            if ("completed".equals(resultStatus)) {
                return extractTextFromResult(result);
            } else if ("failed".equals(resultStatus)) {
                throw new RuntimeException("STT 전사 실패");
            }

            // 5초 대기 후 재시도
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("STT 폴링 중단됨", e);
            }

            retryCount++;
        }

        throw new RuntimeException("STT 전사 타임아웃");
    }

    /**
     * 액세스 토큰 발급 또는 캐시에서 가져오기
     */
    private String getAccessToken() {
        if (cachedAccessToken == null
                || tokenExpireAt == null
                || Instant.now().plusSeconds(600).isAfter(tokenExpireAt)) {

            log.info("새 액세스 토큰 발급 시작");
            authenticateAndCache();
        }

        return cachedAccessToken;
    }

    /**
     * POST /v1/authenticate - 토큰 발급 및 캐싱
     */
    private void authenticateAndCache() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        AuthResponse response = webClient.post()
                .uri(baseUrl + "/v1/authenticate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("인증 실패: 응답 없음");
        }

        cachedAccessToken = response.accessToken();
        tokenExpireAt = Instant.ofEpochSecond(response.expireAt());

        log.info("액세스 토큰 발급 완료: 만료시간={}", tokenExpireAt);
    }

    private String extractTextFromResult(TranscribeResultResponse result) {
        if (result.results() == null || result.results().utterances() == null) {
            throw new RuntimeException("STT 결과가 비어있음");
        }

        StringBuilder fullText = new StringBuilder();
        result.results().utterances().forEach(utterance -> {
            fullText.append(utterance.msg()).append(" ");
        });
        return fullText.toString().trim();
    }

    record AuthResponse(
            @JsonProperty("access_token")
            String accessToken,

            @JsonProperty("expire_at")
            long expireAt
    ) {
    }

    record TranscribeConfig(String model_name, String language, String[] language_candidates) {
    }

    record TranscribeResponse(String id, String status) {
    }

    record TranscribeResultResponse(
            String id,
            String status,
            TranscribeResults results
    ) {
    }

    record TranscribeResults(List<Utterance> utterances) {
    }

    record Utterance(String msg, int startAt, int duration) {
    }
}