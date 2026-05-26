package pl.agh.zti.quiz.domain;

/**
 * Game session state machine.
 * Transitions: CREATED → LOBBY → QUESTION_ACTIVE → EVALUATING → RANKING_DISPLAY → (loop) → FINISHED
 */
public enum GameState {
    CREATED,
    LOBBY,
    QUESTION_ACTIVE,
    EVALUATING,
    RANKING_DISPLAY,
    FINISHED
}
