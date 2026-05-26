package pl.agh.zti.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank
    private String content;
    @Min(5) @Max(120)
    private Integer timeLimitSec;
}
