package pl.agh.zti.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.agh.zti.quiz.domain.Player;
import pl.agh.zti.quiz.dto.ws.PlayerScore;
import pl.agh.zti.quiz.repository.PlayerRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock PlayerRepository playerRepo;
    @Mock SimpMessagingTemplate broker;
    @InjectMocks RankingService service;

    @Test
    void calculateScore_correctFastAnswer_nearMax() {
        int score = service.calculateScore(true, 500, 20);
        assertThat(score).isGreaterThan(900).isLessThanOrEqualTo(1000);
    }

    @Test
    void calculateScore_correctSlowAnswer_lowerScore() {
        int fast = service.calculateScore(true, 1000, 20);
        int slow = service.calculateScore(true, 15000, 20);
        assertThat(fast).isGreaterThan(slow);
    }

    @Test
    void calculateScore_wrongAnswer_zero() {
        assertThat(service.calculateScore(false, 500, 20)).isEqualTo(0);
    }

    @Test
    void calculateScore_answerAfterLimit_zero() {
        assertThat(service.calculateScore(true, 21000, 20)).isEqualTo(0);
    }

    @Test
    void calculateScore_answerAtExactLimit_zero() {
        assertThat(service.calculateScore(true, 20000, 20)).isEqualTo(0);
    }

    @Test
    void getLiveRanking_sortsByScoreDesc() {
        UUID sid = UUID.randomUUID();
        Player p1 = Player.builder().id(UUID.randomUUID()).nickname("Alice").totalScore(800).build();
        Player p2 = Player.builder().id(UUID.randomUUID()).nickname("Bob").totalScore(1200).build();
        Player p3 = Player.builder().id(UUID.randomUUID()).nickname("Carol").totalScore(500).build();

        when(playerRepo.findByGameSessionIdOrderByTotalScoreDesc(sid))
                .thenReturn(List.of(p2, p1, p3));

        List<PlayerScore> ranking = service.getLiveRanking(sid);

        assertThat(ranking).hasSize(3);
        assertThat(ranking.get(0).getNickname()).isEqualTo("Bob");
        assertThat(ranking.get(0).getRank()).isEqualTo(1);
        assertThat(ranking.get(1).getNickname()).isEqualTo("Alice");
        assertThat(ranking.get(1).getRank()).isEqualTo(2);
    }

    @Test
    void broadcastRanking_sendsToCorrectTopic() {
        UUID sid = UUID.randomUUID();
        when(playerRepo.findByGameSessionIdOrderByTotalScoreDesc(sid)).thenReturn(List.of());

        service.broadcastRanking(sid, 1, 5, false);

        verify(broker).convertAndSend(eq("/topic/game." + sid), any(Object.class));
    }
}
