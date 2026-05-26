package pl.agh.zti.quiz.dto.ws;

import lombok.Data;

import java.util.UUID;

/** Sent by player to /app/game.answer */
@Data
public class SubmitAnswerRequest {
    private UUID sessionId;
    private UUID playerId;
    private Long questionId;
    private Long answerId;
    private long responseTimeMs;
}
