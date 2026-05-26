package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Broadcast to /topic/game.{sessionId} after each round evaluation.
 * Per sequence diagram: RankingService → broadcastRanking().
 */
@Data
@Builder
public class RankingEvent {
    private List<PlayerScore> ranking;
    private boolean gameFinished;
    private int currentQuestion;
    private int totalQuestions;
}
