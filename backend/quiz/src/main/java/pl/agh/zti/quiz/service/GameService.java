package pl.agh.zti.quiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.agh.zti.quiz.domain.*;
import pl.agh.zti.quiz.dto.ws.*;
import pl.agh.zti.quiz.repository.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Core game lifecycle service.
 * Manages: session creation, lobby, state transitions, question broadcast, answer submission.
 * Per sequence diagram: GameService.nextQuestion(), GameService.submitAnswer().
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameSessionRepository sessionRepo;
    private final PlayerRepository playerRepo;
    private final PlayerAnswerRepository playerAnswerRepo;
    private final QuizService quizService;
    private final RankingService rankingService;
    private final SimpMessagingTemplate broker;
    private final TaskScheduler taskScheduler;

    /** Tracks active timeout futures per session — allows cancellation if all players answer early. */
    private final Map<UUID, ScheduledFuture<?>> timeoutFutures = new HashMap<>();

    // ---- REST endpoints support ----

    /** Create a new game session for a quiz. Returns session info for the host. */
    public GameSessionResponse createSession(Long quizId, Long hostId) {
        Quiz quiz = quizService.findQuiz(quizId);
        // Verify host owns the quiz
        if (!quiz.getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Host does not own this quiz");
        }
        GameSession session = GameSession.builder()
                .lobbyCode(generateLobbyCode())
                .state(GameState.LOBBY)
                .quiz(quiz)
                .build();
        session = sessionRepo.save(session);
        log.info("Created session {} with code {}", session.getId(), session.getLobbyCode());
        return GameSessionResponse.from(session);
    }

    @Transactional(readOnly = true)
    public GameSessionResponse getSession(UUID sessionId) {
        return GameSessionResponse.from(findSession(sessionId));
    }

    @Transactional(readOnly = true)
    public GameSessionResponse getSessionByCode(String lobbyCode) {
        GameSession session = sessionRepo.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new IllegalArgumentException("Session not found for code: " + lobbyCode));
        return GameSessionResponse.from(session);
    }

    // ---- WebSocket handlers ----

    /**
     * Player joins lobby. Broadcast to /topic/lobby.{sessionId}.
     * Per communication diagram: /app/lobby.join → Player dołącza do lobby.
     */
    public Player joinLobby(String lobbyCode, String nickname) {
        GameSession session = sessionRepo.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid lobby code: " + lobbyCode));

        if (session.getState() != GameState.LOBBY) {
            throw new IllegalStateException("Session is not in LOBBY state: " + session.getState());
        }

        Player player = Player.builder()
                .nickname(nickname)
                .totalScore(0)
                .gameSession(session)
                .build();
        player = playerRepo.save(player);
        session.getParticipants().add(player);

        List<String> nicknames = session.getParticipants().stream()
                .map(Player::getNickname).toList();

        LobbyEvent event = LobbyEvent.builder()
                .type("PLAYER_JOINED")
                .playerId(player.getId())
                .nickname(nickname)
                .participants(nicknames)
                .sessionId(session.getId())
                .lobbyCode(lobbyCode)
                .build();
        broker.convertAndSend("/topic/lobby." + session.getId(), event);
        log.info("Player {} joined session {}", nickname, session.getId());
        return player;
    }

    /**
     * Host starts the game: LOBBY → QUESTION_ACTIVE, broadcasts first question.
     */
    public void startGame(UUID sessionId, Long hostId) {
        GameSession session = findSession(sessionId);
        verifyHost(session, hostId);

        if (session.getState() != GameState.LOBBY) {
            throw new IllegalStateException("Can only start from LOBBY state");
        }
        session.setState(GameState.QUESTION_ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        session.setCurrentQuestionIndex(0);
        sessionRepo.save(session);

        broadcastLobbyStarted(session);
        broadcastCurrentQuestion(session);
    }

    /**
     * Host advances to next question (or finishes game).
     * Per sequence diagram: Host → SEND /app/game.nextQuestion → GameService.nextQuestion().
     */
    public void nextQuestion(UUID sessionId, Long hostId) {
        GameSession session = findSession(sessionId);
        verifyHost(session, hostId);

        cancelTimeout(sessionId);

        List<Question> questions = session.getQuiz().getQuestions();
        int nextIndex = session.getCurrentQuestionIndex() + 1;

        if (nextIndex >= questions.size()) {
            finishGame(session);
        } else {
            session.setCurrentQuestionIndex(nextIndex);
            session.setState(GameState.QUESTION_ACTIVE);
            sessionRepo.save(session);
            broadcastCurrentQuestion(session);
        }
    }

    /**
     * Player submits an answer.
     * Per sequence diagram: Gracz → SEND /app/game.answer → GameService.submitAnswer().
     * AuditAspect intercepts this method call.
     */
    public void submitAnswer(UUID sessionId, UUID playerId, Long questionId, Long answerId, long responseTimeMs) {
        GameSession session = findSession(sessionId);

        if (session.getState() != GameState.QUESTION_ACTIVE) {
            log.warn("Answer submitted outside QUESTION_ACTIVE state by player {}", playerId);
            return;
        }

        // Prevent duplicate answers for the same question
        if (playerAnswerRepo.findByPlayerIdAndQuestionId(playerId, questionId).isPresent()) {
            log.warn("Duplicate answer from player {} for question {}", playerId, questionId);
            return;
        }

        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        Question question = quizService.findQuestion(questionId);
        Answer answer = question.getAnswers().stream()
                .filter(a -> a.getId().equals(answerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));

        boolean correct = answer.getIsCorrect();
        int timeLimitSec = question.resolvedTimeLimitSec();
        int points = rankingService.calculateScore(correct, responseTimeMs, timeLimitSec);

        PlayerAnswer pa = PlayerAnswer.builder()
                .responseTimeMs(responseTimeMs)
                .correct(correct)
                .pointsAwarded(points)
                .player(player)
                .question(question)
                .answer(answer)
                .build();
        playerAnswerRepo.save(pa);

        player.setTotalScore(player.getTotalScore() + points);
        playerRepo.save(player);

        log.debug("Player {} answered question {}: correct={}, points={}", playerId, questionId, correct, points);

        // Check if all players answered → auto-advance to ranking
        long totalPlayers = session.getParticipants().size();
        long answeredCount = playerAnswerRepo.findByPlayerGameSessionId(sessionId).stream()
                .filter(a -> a.getQuestion().getId().equals(questionId))
                .count();

        if (answeredCount >= totalPlayers) {
            cancelTimeout(sessionId);
            evaluateRound(session);
        }
    }

    // ---- Internal helpers ----

    private void evaluateRound(GameSession session) {
        session.setState(GameState.EVALUATING);
        sessionRepo.save(session);

        List<Question> questions = session.getQuiz().getQuestions();
        boolean isLast = session.getCurrentQuestionIndex() >= questions.size() - 1;

        session.setState(GameState.RANKING_DISPLAY);
        sessionRepo.save(session);

        rankingService.broadcastRanking(
                session.getId(),
                session.getCurrentQuestionIndex() + 1,
                questions.size(),
                isLast
        );
    }

    private void broadcastCurrentQuestion(GameSession session) {
        List<Question> questions = session.getQuiz().getQuestions();
        int idx = session.getCurrentQuestionIndex();
        Question q = questions.get(idx);

        List<AnswerOption> options = q.getAnswers().stream()
                .map(a -> AnswerOption.builder().id(a.getId()).content(a.getContent()).build())
                .toList();

        QuestionEvent event = QuestionEvent.builder()
                .questionId(q.getId())
                .content(q.getContent())
                .answers(options)
                .timeLimitSec(q.resolvedTimeLimitSec())
                .questionNumber(idx + 1)
                .totalQuestions(questions.size())
                .serverTimestamp(System.currentTimeMillis())
                .build();

        broker.convertAndSend("/topic/game." + session.getId(), event);
        log.info("Broadcast question {}/{} to session {}", idx + 1, questions.size(), session.getId());

        // Schedule server-side timeout
        scheduleTimeout(session.getId(), q.resolvedTimeLimitSec());
    }

    private void broadcastLobbyStarted(GameSession session) {
        List<String> nicknames = session.getParticipants().stream()
                .map(Player::getNickname).toList();
        LobbyEvent event = LobbyEvent.builder()
                .type("GAME_STARTED")
                .participants(nicknames)
                .sessionId(session.getId())
                .build();
        broker.convertAndSend("/topic/lobby." + session.getId(), event);
    }

    private void finishGame(GameSession session) {
        session.setState(GameState.FINISHED);
        sessionRepo.save(session);
        log.info("Game session {} finished", session.getId());

        List<Question> questions = session.getQuiz().getQuestions();
        rankingService.broadcastRanking(session.getId(), questions.size(), questions.size(), true);
    }

    private void scheduleTimeout(UUID sessionId, int timeLimitSec) {
        cancelTimeout(sessionId);
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> handleTimeout(sessionId),
                Instant.now().plusSeconds(timeLimitSec)
        );
        timeoutFutures.put(sessionId, future);
    }

    private void handleTimeout(UUID sessionId) {
        log.info("Question timeout for session {}", sessionId);
        sessionRepo.findById(sessionId).ifPresent(session -> {
            if (session.getState() == GameState.QUESTION_ACTIVE) {
                evaluateRound(session);
            }
        });
        timeoutFutures.remove(sessionId);
    }

    private void cancelTimeout(UUID sessionId) {
        ScheduledFuture<?> future = timeoutFutures.remove(sessionId);
        if (future != null) future.cancel(false);
    }

    private void verifyHost(GameSession session, Long hostId) {
        if (!session.getQuiz().getHost().getId().equals(hostId)) {
            throw new IllegalArgumentException("Not the host of this session");
        }
    }

    private GameSession findSession(UUID id) {
        return sessionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
    }

    private String generateLobbyCode() {
        Random rng = new Random();
        String code;
        do {
            code = String.format("%06d", rng.nextInt(1_000_000));
        } while (sessionRepo.findByLobbyCode(code).isPresent());
        return code;
    }
}
