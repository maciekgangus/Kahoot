package pl.agh.zti.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A single question within a Quiz. Has 2–6 Answers (per class diagram).
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /** Per-question time limit; falls back to Quiz.defaultTimeLimitSec if null. */
    @Column(name = "time_limit_sec")
    private Integer timeLimitSec;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /** 2–6 answers per question (class diagram multiplicity). */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

    /** Resolved time limit: own value or quiz default, minimum 5 seconds. */
    @Transient
    public int resolvedTimeLimitSec() {
        if (timeLimitSec != null && timeLimitSec > 0) return timeLimitSec;
        if (quiz != null && quiz.getDefaultTimeLimitSec() != null) return quiz.getDefaultTimeLimitSec();
        return 30;
    }
}
