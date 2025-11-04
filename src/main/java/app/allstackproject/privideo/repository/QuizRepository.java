package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.Quiz;
import app.allstackproject.privideo.repository.quiz.QuizRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryCustom {
}