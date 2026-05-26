package pl.agh.zti.quiz.dto.ws;

import lombok.Data;

import java.util.UUID;

/** Sent by host to /app/game.nextQuestion */
@Data
public class NextQuestionRequest {
    private UUID sessionId;
    private Long hostId;   // host identity verification
}
