package pl.agh.zti.quiz.dto;

import lombok.Builder;
import lombok.Data;
import pl.agh.zti.quiz.domain.Host;

@Data
@Builder
public class HostResponse {
    private Long id;
    private String username;

    public static HostResponse from(Host h) {
        return HostResponse.builder().id(h.getId()).username(h.getUsername()).build();
    }
}
