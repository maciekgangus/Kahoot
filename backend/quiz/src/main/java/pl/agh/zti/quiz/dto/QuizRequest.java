package pl.agh.zti.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizRequest {
    @NotBlank
    private String title;
    private String description;
    @Min(5) @Max(120)
    private Integer defaultTimeLimitSec;
}
