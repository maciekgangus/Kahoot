package pl.agh.zti.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotBlank
    private String content;
    @NotNull
    private Boolean isCorrect;
}
