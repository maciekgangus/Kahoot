package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;
import pl.agh.zti.quiz.domain.GameSession;
import pl.agh.zti.quiz.domain.GameState;
import pl.agh.zti.quiz.domain.Player;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GameSessionResponse {
    private UUID sessionId;
    private String lobbyCode;
    private GameState state;
    private Long quizId;
    private String quizTitle;
    private List<String> participants;

    public static GameSessionResponse from(GameSession s) {
        List<String> nicks = s.getParticipants().stream()
                .map(Player::getNickname).toList();
        return GameSessionResponse.builder()
                .sessionId(s.getId())
                .lobbyCode(s.getLobbyCode())
                .state(s.getState())
                .quizId(s.getQuiz().getId())
                .quizTitle(s.getQuiz().getTitle())
                .participants(nicks)
                .build();
    }
}
