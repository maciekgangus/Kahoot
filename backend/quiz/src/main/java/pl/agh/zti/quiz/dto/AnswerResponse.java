package pl.agh.zti.quiz.dto;

import lombok.Builder;
import lombok.Data;
import pl.agh.zti.quiz.domain.Answer;

@Data
@Builder
public class AnswerResponse {
    private Long id;
    private String content;
    private Boolean isCorrect;

    public static AnswerResponse from(Answer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .content(a.getContent())
                .isCorrect(a.getIsCorrect())
                .build();
    }
}
