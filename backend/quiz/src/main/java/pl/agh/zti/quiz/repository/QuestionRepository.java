package pl.agh.zti.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agh.zti.quiz.domain.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);
    int countByQuizId(Long quizId);
}
