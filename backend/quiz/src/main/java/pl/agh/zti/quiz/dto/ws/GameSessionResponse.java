package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;
import pl.agh.zti.quiz.domain.GameSession;
import pl.agh.zti.quiz.domain.GameState;

import java.util.UUID;

@Data
@Builder
public class GameSessionResponse {
    private UUID sessionId;
    private String lobbyCode;
    private GameState state;
    private Long quizId;
    private String quizTitle;

    public static GameSessionResponse from(GameSession s) {
        return GameSessionResponse.builder()
                .sessionId(s.getId())
                .lobbyCode(s.getLobbyCode())
                .state(s.getState())
                .quizId(s.getQuiz().getId())
                .quizTitle(s.getQuiz().getTitle())
                .build();
    }
}
