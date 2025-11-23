package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.common.enumStatus.AiFunctionType;
import app.allstackproject.privideo.common.util.S3Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiFunctionService {

    private final SttService sttService;
    private final S3Util s3Util;

    @Value("${cloud.aws.s3.buckets.output}")
    private String outputBucket;

    @Async
    public void processAiFunction(Long videoId, String hlsPrefix, AiFunctionType aiFunction) {
        File tempAudioFile = null;
        try {
            // 1단계: S3에서 음성 파일 다운로드
            log.info("S3에서 음성 파일 다운로드 시작: videoId={}, audioKey={}", videoId, hlsPrefix + "/voice.mp3");
            tempAudioFile = s3Util.downloadToTempFile(outputBucket, hlsPrefix + "/voice.mp3");
            log.info("S3 다운로드 완료: videoId={}, size={}MB", videoId, tempAudioFile.length() / 1024 / 1024);

            // 2단계: STT 요청
            log.info("STT 시작: videoId={}", videoId);
            String transcribeId = sttService.requestTranscription(tempAudioFile);

            // 3단계: STT 결과 조회 (폴링)
            String sttText = sttService.getTranscriptionResult(transcribeId);
            log.info("STT 완료: videoId={}, textLength={}", videoId, sttText.length());
            log.info("STT 완료: result={}", sttText);

            // TODO: Gemini AI 처리

            log.info("AI 기능 처리 완료: videoId={}, function={}", videoId, aiFunction);

        } catch (Exception e) {
            log.error("AI 기능 처리 실패: videoId={}", videoId, e);
        } finally {
            if (tempAudioFile != null && tempAudioFile.exists()) {
                try {
                    Files.delete(tempAudioFile.toPath());
                    log.info("임시 파일 삭제: {}", tempAudioFile.getName());
                } catch (IOException e) {
                    log.warn("임시 파일 삭제 실패: {}", tempAudioFile.getName(), e);
                }
            }
        }
    }

}
