package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PlayerScore {
    private UUID playerId;
    private String nickname;
    private int totalScore;
    private int rank;
}
