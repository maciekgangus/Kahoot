package pl.agh.zti.quiz.dto;

import lombok.Builder;
import lombok.Data;
import pl.agh.zti.quiz.domain.Quiz;

import java.util.List;

@Data
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Integer defaultTimeLimitSec;
    private Long hostId;
    private List<QuestionResponse> questions;

    public static QuizResponse from(Quiz q) {
        return QuizResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .description(q.getDescription())
                .defaultTimeLimitSec(q.getDefaultTimeLimitSec())
                .hostId(q.getHost().getId())
                .questions(q.getQuestions().stream().map(QuestionResponse::from).toList())
                .build();
    }
}
