package app.allstackproject.privideo.service;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_SOLVED_QUIZ;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_QUIZ_REQUEST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.QUIZ_NOT_FOUND;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.QuizResponse;
import app.allstackproject.privideo.dto.quiz.MemberQuizDto;
import app.allstackproject.privideo.dto.quiz.SolveResultDto;
import app.allstackproject.privideo.entity.MemberQuizResult;
import app.allstackproject.privideo.repository.QuizRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.quiz.MemberQuizResultRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;
    private final QuizRepository quizRepository;
    private final MemberQuizResultRepository memberQuizResultRepository;

    private final static int QUIZ_CNT = 3;

    @Transactional(readOnly = true)
    public QuizResponse getUserQuizzes(Long memberId, Long orgId) {
        List<MemberQuizDto> memberQuizDtos = quizRepository.findByMemberIdAndOrganizationId(memberId, orgId);
        return QuizResponse.of(memberQuizDtos);
    }

    public boolean createQuizResult(Long memberId, Long orgId, Long videoId, SolveResultDto[] solveResults) {
        if (!videoRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId)) {
            throw new ApiException(INVALID_QUIZ_REQUEST);
        }

        int result = 0;
        for (SolveResultDto solveResult : solveResults) {
            Long quizId = solveResult.getQuizId();
            if (!quizRepository.existsById(quizId)) {
                throw new ApiException(QUIZ_NOT_FOUND);
            }

            if (memberQuizResultRepository.existsByQuizId(quizId)) {
                throw new ApiException(ALREADY_SOLVED_QUIZ);
            }

            MemberQuizResult memberQuizResult = MemberQuizResult.create(
                    quizRepository.getReferenceById(quizId),
                    memberRepository.getReferenceById(memberId),
                    videoRepository.getReferenceById(videoId),
                    solveResult.getMemberAnswer(), LocalDateTime.now());

            if (memberQuizResultRepository.save(memberQuizResult) != null) {
                result++;
            }
        }

        return result == QUIZ_CNT;
    }
}


