package pl.agh.zti.quiz.dto.ws;

import lombok.Builder;
import lombok.Data;

/** Answer option sent to players — never exposes isCorrect */
@Data
@Builder
public class AnswerOption {
    private Long id;
    private String content;
}
