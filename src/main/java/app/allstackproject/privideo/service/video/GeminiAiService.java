package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.dto.video.QuizInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final String PROMPT_TEMPLATE = """
                당신은 동영상 콘텐츠 분석 전문가입니다.
            
                아래는 동영상에서 추출된 STT 텍스트입니다:
            
                [STT 텍스트 시작]
                {sttText}
                [STT 텍스트 끝]
            
                ------------------------------------
                0) 전처리 단계 (영어 용어 복원)
                ------------------------------------
                - STT 텍스트에는 영어 단어/표현이 한글 발음으로 적혀 있거나,
                  영어와 한글이 섞여서 기록되어 있을 수 있습니다.
                  (예: 비즈니스 모델, 마케팅 캠페인, 오브젝트 오리엔티드 프로그래밍 등)
                - 먼저 STT 텍스트를 읽고, 한글 발음 기반 영어 표현을 가능한 한
                  정확한 영어 원어로 복원하여 내부적으로 정제된 텍스트를 만든다고 가정하고 작업합니다.
                  * 고유명사, 전문 용어, 기술 용어, 서비스/제품명 등은 특히 주의해서 복원합니다.
                - 이후 SUMMARY / FEEDBACK / QUIZ 생성은 모두 이 정제된 텍스트를 기준으로 수행합니다.
            
                ------------------------------------
                공통 출력 언어 규칙
                ------------------------------------
                - 결과 문장은 기본적으로 한국어로 작성합니다.
                - 다만 핵심 개념, 고유명사, 전문 용어, 기술 용어 등은
                  가능한 한 영어 원어를 그대로 사용하고,
                  괄호 안에 한글 설명을 함께 표기합니다.
                  예)
                    Domain-Driven Design(도메인 주도 설계)
                    Encapsulation(캡슐화)
                    Product Manager(프로덕트 매니저)
                - QUIZ 모드에서도 문제와 해설은 한국어로 작성하되,
                  위와 같은 방식으로 핵심 용어에 영어 원어 + 한글 설명을 함께 사용합니다.
            
                요청된 기능은 다음 중 하나입니다:
                AI_FUNCTION = {aiFunction}
            
                AI_FUNCTION 값에 따라 아래 규칙을 따르세요.
            
                ------------------------------------
                1) SUMMARY 선택 시
                ------------------------------------
                - 정제된 텍스트를 기준으로 STT 텍스트를 약 500자로 핵심 요약
                - 불필요한 문장 제거
                - 중요한 흐름 및 핵심 개념 중심 서술
                - 자연스러운 한국어 문장으로 구성
                - 두괄식으로 작성
                - 공통 출력 언어 규칙을 따른다.
                - 결과는 단일 문자열(JSON 아님)
            
                ------------------------------------
                2) FEEDBACK 선택 시
                ------------------------------------
                - 학습자가 해당 영상을 잘 시청했다고 가정하고,
                  이해도와 학습 효과를 높이기 위한 피드백을 작성합니다.
                - 잘한 점 + 보완하면 좋은 점 + 더 깊이 이해하면 좋은 포인트를 포함합니다.
                - 분량은 약 500자
                - 공통 출력 언어 규칙을 따른다.
                - 결과는 단일 문자열(JSON 아님)
            
                ------------------------------------
                3) QUIZ 선택 시
                ------------------------------------
                - 정제된 텍스트 내용을 기반으로 OX 퀴즈 3문항을 생성합니다.
                - question은 1~2문장 정도의 문제로 구성합니다.
                - 정답이 명확하게 O 또는 X로 나뉘는 문제만 만듭니다.
                - answer는 불리언(Boolean)으로 표현하며,
                  true = O(맞음), false = X(틀림)의 의미를 갖습니다.
                - 각 문항에는 정답과 간단한 해설(description)을 포함합니다.
                - 공통 출력 언어 규칙을 따른다.
                - answer는 반드시 true 또는 false만 사용합니다. (O, X 금지)
                - 반드시 순수 JSON 배열만 출력합니다.
                - 절대로 마크다운 코드블록을 사용하지 마세요.
                - JSON 앞뒤에 어떠한 텍스트, 설명, 인사말도 포함하지 마세요.
                - 첫 글자는 반드시 [ 이고 마지막 글자는 반드시 ] 이어야 합니다.
            
                ------------------------------------
                ✨ 중요한 규칙
                ------------------------------------
                - 선택된 AI_FUNCTION에 해당하는 결과만 출력할 것
                - 다른 기능 관련 내용은 출력하지 말 것
                - QUIZ 외 기능은 JSON 사용하지 말 것
                - 불필요한 설명, 안내 문구, 인사말 절대 출력 금지
                - 결과값만 출력하기
            
                이제 AI_FUNCTION에 맞는 결과를 출력하세요.
            """;

    public String generateSummary(String sttText) {
        Map<String, Object> params = Map.of(
                "sttText", sttText,
                "aiFunction", "SUMMARY"
        );

        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(params);

        String result = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("SUMMARY 생성 완료: length={}", result.length());
        return result;
    }

    public String generateFeedback(String sttText) {
        Map<String, Object> params = Map.of(
                "sttText", sttText,
                "aiFunction", "FEEDBACK"
        );

        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(params);

        String result = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("FEEDBACK 생성 완료: length={}", result.length());
        return result;
    }

    public List<QuizInfo> generateQuiz(String sttText) {
        Map<String, Object> params = Map.of(
                "sttText", sttText,
                "aiFunction", "QUIZ"
        );

        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(params);

        String jsonResult = chatClient.prompt(prompt)
                .call()
                .content();

        try {
            String cleanJson = jsonResult
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            List<QuizInfo> quizzes = objectMapper.readValue(
                    cleanJson,
                    new TypeReference<List<QuizInfo>>() {
                    }
            );
            log.info("QUIZ 생성 완료: count={}", quizzes.size());
            return quizzes;
        } catch (Exception e) {
            log.error("QUIZ JSON 파싱 실패", e);
            throw new RuntimeException("퀴즈 생성 실패", e);
        }
    }
}