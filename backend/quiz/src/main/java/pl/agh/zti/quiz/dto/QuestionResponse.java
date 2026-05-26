package pl.agh.zti.quiz.dto;

import lombok.Builder;
import lombok.Data;
import pl.agh.zti.quiz.domain.Question;

import java.util.List;

@Data
@Builder
public class QuestionResponse {
    private Long id;
    private String content;
    private Integer orderIndex;
    private Integer timeLimitSec;
    private List<AnswerResponse> answers;

    public static QuestionResponse from(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .orderIndex(q.getOrderIndex())
                .timeLimitSec(q.getTimeLimitSec())
                .answers(q.getAnswers().stream().map(AnswerResponse::from).toList())
                .build();
    }
}
