package app.allstackproject.privideo.domain.quiz.repository;

import app.allstackproject.privideo.domain.quiz.entity.Quiz;
import app.allstackproject.privideo.domain.quiz.repository.custom.QuizRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryCustom {
    void deleteAllByVideoId(Long videoId);
}