package pl.agh.zti.quiz.web.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.zti.quiz.dto.ws.GameSessionResponse;
import pl.agh.zti.quiz.service.GameService;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class GameSessionController {

    private final GameService gameService;

    /** Host creates a game session for a given quiz. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameSessionResponse create(@RequestParam Long quizId, @RequestParam Long hostId) {
        return gameService.createSession(quizId, hostId);
    }

    @GetMapping("/{id}")
    public GameSessionResponse get(@PathVariable UUID id) {
        return gameService.getSession(id);
    }

    /** Host starts the game (moves from LOBBY to QUESTION_ACTIVE). */
    @PostMapping("/{id}/start")
    public GameSessionResponse start(@PathVariable UUID id, @RequestParam Long hostId) {
        gameService.startGame(id, hostId);
        return gameService.getSession(id);
    }

    /** Player looks up session by lobby code (needed for WS subscription). */
    @GetMapping("/by-code")
    public GameSessionResponse getByCode(@RequestParam String code) {
        return gameService.getSessionByCode(code);
    }

    /**
     * Returns the currently active question, or 204 No Content if no question is active.
     * PlayerGame calls this on mount to recover a question broadcast before it subscribed.
     */
    @GetMapping("/{id}/current-question")
    public ResponseEntity<?> currentQuestion(@PathVariable UUID id) {
        return gameService.getCurrentQuestion(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
