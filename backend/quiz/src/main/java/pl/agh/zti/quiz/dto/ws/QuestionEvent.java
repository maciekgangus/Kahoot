package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Broadcast to /topic/game.{sessionId} when a new question starts.
 * Per sequence diagram: server → all participants.
 */
@Data
@Builder
public class QuestionEvent {
    private Long questionId;
    private String content;
    private List<AnswerOption> answers;   // NO isCorrect flag
    private int timeLimitSec;
    private int questionNumber;           // 1-based
    private int totalQuestions;
    private long serverTimestamp;         // epoch ms — for client-side timer sync
}
