package pl.agh.zti.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.agh.zti.quiz.domain.Player;
import pl.agh.zti.quiz.dto.ws.PlayerScore;
import pl.agh.zti.quiz.dto.ws.RankingEvent;
import pl.agh.zti.quiz.repository.PlayerRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scoring, live ranking, and ranking broadcast.
 * Per component diagram: RankingService layer.
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private final PlayerRepository playerRepo;
    private final SimpMessagingTemplate broker;

    /**
     * Kahoot-style scoring: 1000 × (1 − responseTimeMs / timeLimitMs), minimum 0.
     * Wrong answer always yields 0.
     */
    public int calculateScore(boolean correct, long responseTimeMs, int timeLimitSec) {
        if (!correct) return 0;
        long timeLimitMs = (long) timeLimitSec * 1000;
        if (responseTimeMs >= timeLimitMs) return 0;
        double ratio = (double) responseTimeMs / timeLimitMs;
        return Math.max(0, (int) Math.round(1000 * (1.0 - ratio)));
    }

    /** Returns live ranking for a session, sorted by totalScore descending. */
    @Transactional(readOnly = true)
    public List<PlayerScore> getLiveRanking(UUID sessionId) {
        List<Player> players = playerRepo.findByGameSessionIdOrderByTotalScoreDesc(sessionId);
        AtomicInteger rank = new AtomicInteger(1);
        return players.stream()
                .map(p -> PlayerScore.builder()
                        .playerId(p.getId())
                        .nickname(p.getNickname())
                        .totalScore(p.getTotalScore())
                        .rank(rank.getAndIncrement())
                        .build())
                .toList();
    }

    /**
     * Broadcast RankingEvent to /topic/game.{sessionId}.
     * Per sequence diagram step: RankingService.broadcastRanking().
     */
    public void broadcastRanking(UUID sessionId, int currentQuestion, int totalQuestions, boolean gameFinished) {
        List<PlayerScore> ranking = getLiveRanking(sessionId);
        RankingEvent event = RankingEvent.builder()
                .ranking(ranking)
                .gameFinished(gameFinished)
                .currentQuestion(currentQuestion)
                .totalQuestions(totalQuestions)
                .build();
        broker.convertAndSend("/topic/game." + sessionId, event);
    }
}
