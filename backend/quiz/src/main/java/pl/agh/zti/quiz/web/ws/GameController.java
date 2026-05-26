package pl.agh.zti.quiz.web.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import pl.agh.zti.quiz.dto.ws.NextQuestionRequest;
import pl.agh.zti.quiz.dto.ws.SubmitAnswerRequest;
import pl.agh.zti.quiz.service.GameService;

/**
 * STOMP message handlers for in-game events.
 * Per communication diagram:
 *   /app/game.answer        — player submits answer
 *   /app/game.nextQuestion  — host advances to next question
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;

    /**
     * Player submits answer.
     * Destination: /app/game.answer
     */
    @MessageMapping("/game.answer")
    public void submitAnswer(@Payload SubmitAnswerRequest req) {
        log.debug("Answer received: player={} question={} answer={}", req.getPlayerId(), req.getQuestionId(), req.getAnswerId());
        gameService.submitAnswer(
                req.getSessionId(),
                req.getPlayerId(),
                req.getQuestionId(),
                req.getAnswerId(),
                req.getResponseTimeMs()
        );
    }

    /**
     * Host advances to next question.
     * Destination: /app/game.nextQuestion
     */
    @MessageMapping("/game.nextQuestion")
    public void nextQuestion(@Payload NextQuestionRequest req) {
        log.debug("Next question requested by host {} for session {}", req.getHostId(), req.getSessionId());
        gameService.nextQuestion(req.getSessionId(), req.getHostId());
    }
}
