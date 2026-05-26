package pl.agh.zti.quiz.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: verifies JPA entity persistence on the compose Postgres.
 * Requires: docker compose up -d db
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DomainPersistenceTest {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      () -> "jdbc:postgresql://localhost:5432/quiz");
        r.add("spring.datasource.username", () -> "quiz");
        r.add("spring.datasource.password", () -> "quiz");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired TestEntityManager em;

    @Test
    void savesAndLoadsFullGameSessionGraph() {
        Host host = em.persist(Host.builder().username("h1").passwordHash("x").build());

        Quiz quiz = em.persist(Quiz.builder()
                .title("Sample Quiz").description("Test").defaultTimeLimitSec(20).host(host).build());

        Question q = em.persist(Question.builder()
                .content("What is 2+2?").orderIndex(0).timeLimitSec(15).quiz(quiz).build());

        Answer a1 = em.persist(Answer.builder().content("3").isCorrect(false).question(q).build());
        Answer a2 = em.persist(Answer.builder().content("4").isCorrect(true).question(q).build());

        GameSession session = em.persist(GameSession.builder()
                .lobbyCode("123456").state(GameState.LOBBY)
                .startedAt(LocalDateTime.now()).quiz(quiz).build());

        Player player = em.persist(Player.builder()
                .nickname("Alice").totalScore(0).gameSession(session).build());

        PlayerAnswer pa = em.persist(PlayerAnswer.builder()
                .responseTimeMs(3500L).correct(true).pointsAwarded(875)
                .player(player).question(q).answer(a2).build());

        em.flush();
        em.clear();

        GameSession loaded = em.find(GameSession.class, session.getId());
        assertThat(loaded.getLobbyCode()).isEqualTo("123456");
        assertThat(loaded.getState()).isEqualTo(GameState.LOBBY);

        Player lp = em.find(Player.class, player.getId());
        assertThat(lp.getNickname()).isEqualTo("Alice");

        PlayerAnswer lpa = em.find(PlayerAnswer.class, pa.getId());
        assertThat(lpa.getPointsAwarded()).isEqualTo(875);
        assertThat(lpa.getCorrect()).isTrue();
    }

    @Test
    void questionResolvesTimeLimitFromQuiz() {
        Host host = em.persist(Host.builder().username("h2").passwordHash("x").build());
        Quiz quiz = em.persist(Quiz.builder().title("Q").defaultTimeLimitSec(30).host(host).build());
        Question q = em.persist(Question.builder().content("?").orderIndex(0).quiz(quiz).build());
        em.flush();
        em.clear();

        Question loaded = em.find(Question.class, q.getId());
        assertThat(loaded.getTimeLimitSec()).isNull();
        // resolvedTimeLimitSec() needs quiz — re-load with quiz
        loaded.setQuiz(em.find(Quiz.class, quiz.getId()));
        assertThat(loaded.resolvedTimeLimitSec()).isEqualTo(30);
    }
}
