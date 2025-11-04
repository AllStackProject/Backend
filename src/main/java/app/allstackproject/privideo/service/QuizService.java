package app.allstackproject.privideo.service;

import app.allstackproject.privideo.dto.QuizResponse;
import app.allstackproject.privideo.dto.quiz.MemberQuizDto;
import app.allstackproject.privideo.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizResponse getUserQuizzes(Long memberId, Long orgId) {
        List<MemberQuizDto> memberQuizDtos = quizRepository.findByMemberIdAndOrganizationId(memberId, orgId);
        return QuizResponse.of(memberQuizDtos);
    }
}
