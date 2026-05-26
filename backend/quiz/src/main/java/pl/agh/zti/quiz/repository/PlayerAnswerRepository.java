package pl.agh.zti.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agh.zti.quiz.domain.PlayerAnswer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long> {
    List<PlayerAnswer> findByPlayerGameSessionId(UUID gameSessionId);
    Optional<PlayerAnswer> findByPlayerIdAndQuestionId(UUID playerId, Long questionId);
}
