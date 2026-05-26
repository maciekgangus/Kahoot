package pl.agh.zti.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HostRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Size(min = 4)
    private String password;
}
