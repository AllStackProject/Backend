package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.common.enumStatus.AiFunctionType;
import app.allstackproject.privideo.common.util.S3Util;
import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.entity.Quiz;
import app.allstackproject.privideo.entity.Video;
import app.allstackproject.privideo.repository.quiz.QuizRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
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
    private final GeminiAiService geminiService;
    private final VideoRepository videoRepository;
    private final QuizRepository quizRepository;
    private final S3Util s3Util;

    @Value("${cloud.aws.s3.buckets.original}")
    private String originalBucket;

    @Async
    public void processAiFunction(Long videoId, String videoKey, AiFunctionType aiFunction) {
        File tempAudioFile = null;
        try {
            // 1단계: S3에서 원본 파일 다운로드
            log.info("S3에서 원본 파일 다운로드 시작: videoKey={}", videoKey);
            tempAudioFile = s3Util.downloadToTempFile(originalBucket, videoKey);
            log.info("S3 다운로드 완료: videoKey={}, size={}MB", videoKey, tempAudioFile.length() / 1024 / 1024);

            // 2단계: STT 요청
            log.info("STT 시작: videoId={}", videoId);
            String transcribeId = sttService.requestTranscription(tempAudioFile);

            // 3단계: STT 결과 조회 (폴링)
            String sttText = sttService.getTranscriptionResult(transcribeId);
            log.info("STT 완료: videoId={}, textLength={}", videoId, sttText.length());

            // 4단계: Gemini AI 처리
            processWithGemini(videoId, sttText, aiFunction);

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

    /**
     * Gemini로 AI 결과 생성
     */
    private void processWithGemini(Long videoId, String sttText, AiFunctionType aiFunction) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        switch (aiFunction) {
            case SUMMARY -> {
                String summary = geminiService.generateSummary(sttText);
                video.setAiSummary(summary);
                log.info("Summary 저장 완료: videoId={}", videoId);
            }
            case FEEDBACK -> {
                String feedback = geminiService.generateFeedback(sttText);
                video.setAiFeedback(feedback);
                log.info("Feedback 저장 완료: videoId={}", videoId);
            }
            case QUIZ -> {
                List<QuizInfo> quizItems = geminiService.generateQuiz(sttText);
                saveQuizzesToDatabase(video, quizItems);
                log.info("Quiz 저장 완료: videoId={}, count={}", videoId, quizItems.size());
            }
        }
    }

    /**
     * 퀴즈를 DB에 저장
     */
    private void saveQuizzesToDatabase(Video video, List<QuizInfo> quizItems) {
        List<Quiz> quizzes = quizItems.stream()
                .map(q -> Quiz.create(
                        video,
                        q.getQuestion(),
                        q.getAnswer(),
                        q.getDescription()
                ))
                .collect(Collectors.toList());

        quizRepository.saveAll(quizzes);
    }

}
