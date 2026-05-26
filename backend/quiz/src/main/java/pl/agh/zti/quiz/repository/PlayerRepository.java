package pl.agh.zti.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agh.zti.quiz.domain.Player;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    List<Player> findByGameSessionIdOrderByTotalScoreDesc(UUID gameSessionId);
}
