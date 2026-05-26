package pl.agh.zti.quiz.web.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import pl.agh.zti.quiz.domain.Player;
import pl.agh.zti.quiz.dto.ws.LobbyJoinRequest;
import pl.agh.zti.quiz.service.GameService;

/**
 * STOMP handler for lobby events.
 * Per communication diagram: /app/lobby.join → /topic/lobby.{id}
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LobbyController {

    private final GameService gameService;

    /**
     * Player joins a lobby by lobby code.
     * Destination: /app/lobby.join
     * Broadcasts LobbyEvent to /topic/lobby.{sessionId}
     */
    @MessageMapping("/lobby.join")
    public void joinLobby(@Payload LobbyJoinRequest req, SimpMessageHeaderAccessor headers) {
        log.info("Join request: code={} nickname={}", req.getLobbyCode(), req.getNickname());
        Player player = gameService.joinLobby(req.getLobbyCode(), req.getNickname());
        // Store playerId in WebSocket session for convenience
        if (headers.getSessionAttributes() != null) {
            headers.getSessionAttributes().put("playerId", player.getId());
            headers.getSessionAttributes().put("sessionId", player.getGameSession().getId());
        }
    }
}
