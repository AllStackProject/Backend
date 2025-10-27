package app.allstackproject.privideo.service;

import app.allstackproject.privideo.dto.UserQuizResponse;
import app.allstackproject.privideo.entity.MemberQuizResult;
import app.allstackproject.privideo.repository.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyQuizService {

    private final UserQuizRepository userQuizRepository;

    public UserQuizResponse getUserQuizzes(Long memberId) {
        List<MemberQuizResult> quizList = userQuizRepository.findByMemberId(memberId);
        return UserQuizResponse.of(quizList);
    }
}
