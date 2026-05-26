package pl.agh.zti.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A live game session created by a Host. Tracks state transitions and active participants.
 */
@Entity
@Table(name = "game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lobby_code", nullable = false, unique = true, length = 6)
    private String lobbyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameState state = GameState.CREATED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Player> participants = new ArrayList<>();

    /**
     * Index of the currently active question within quiz.getQuestions().
     * Not in the class diagram but required for game flow. Stored in DB.
     */
    @Column(name = "current_question_index", nullable = false)
    @Builder.Default
    private Integer currentQuestionIndex = 0;
}
