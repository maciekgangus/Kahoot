package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/** Broadcast to /topic/lobby.{sessionId} when player joins or leaves */
@Data
@Builder
public class LobbyEvent {
    private String type;          // "PLAYER_JOINED" | "GAME_STARTED"
    private UUID playerId;
    private String nickname;
    private List<String> participants;
    private UUID sessionId;
    private String lobbyCode;
}
