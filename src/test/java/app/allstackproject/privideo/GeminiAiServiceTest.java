package app.allstackproject.privideo;

import static org.assertj.core.api.Assertions.assertThat;

import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.service.video.GeminiAiService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class GeminiAiServiceTest {

    @Autowired
    private GeminiAiService geminiAiService;

    @Test
    void testSummary() throws Exception {
        Path path = Path.of("src/test/resources/summarySample.txt");
        String sttText = Files.readString(path, StandardCharsets.UTF_8);

        String summary = geminiAiService.generateSummary(sttText);

        log.info("===== SUMMARY =====\n{}", summary);
        assertThat(summary).isNotBlank();
    }

    @Test
    void testFeedback() throws Exception {
        Path path = Path.of("src/test/resources/feedbackSample.txt");
        String sttText = Files.readString(path, StandardCharsets.UTF_8);

        String feedback = geminiAiService.generateFeedback(sttText);

        log.info("===== FEEDBACK =====\n{}", feedback);
        assertThat(feedback).isNotBlank();
    }

    @Test
    void testQuiz() throws Exception {
        Path path = Path.of("src/test/resources/quizSample.txt");
        String sttText = Files.readString(path, StandardCharsets.UTF_8);

        List<QuizInfo> quizzes = geminiAiService.generateQuiz(sttText);

        log.info("===== QUIZ =====");
        quizzes.forEach(q -> log.info("Q: {}\nA: {}\nD: {}\n",
                q.getQuestion(), q.getAnswer(), q.getDescription()));

        assertThat(quizzes).hasSize(3);
    }
}