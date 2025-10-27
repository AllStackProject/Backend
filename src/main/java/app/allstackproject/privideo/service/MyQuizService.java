package app.allstackproject.privideo.service;

import app.allstackproject.privideo.dto.QuizResponse;
import app.allstackproject.privideo.entity.MemberQuizResult;
import app.allstackproject.privideo.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyQuizService {

    private final QuizRepository quizRepository;

    public QuizResponse getUserQuizzes(Long memberId, Long orgId) {
        List<MemberQuizResult> quizList = quizRepository.findByMemberIdAndOrganizationId(memberId, orgId);
        return QuizResponse.of(quizList);
    }
}
