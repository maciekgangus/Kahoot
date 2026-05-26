package pl.agh.zti.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Records a Player's answer to a specific Question in a game session.
 * responseTimeMs is used for scoring and anti-cheat validation.
 */
@Entity
@Table(name = "player_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Milliseconds elapsed from question broadcast to answer submission. */
    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(nullable = false)
    private Boolean correct;

    @Column(name = "points_awarded", nullable = false)
    @Builder.Default
    private Integer pointsAwarded = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;
}
