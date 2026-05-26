package pl.agh.zti.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agh.zti.quiz.domain.GameSession;

import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
    Optional<GameSession> findByLobbyCode(String lobbyCode);
}
