package pl.agh.zti.quiz.dto.ws;

import lombok.Data;

/** Sent by player to /app/lobby.join */
@Data
public class LobbyJoinRequest {
    private String lobbyCode;
    private String nickname;
}
