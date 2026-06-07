package pl.agh.zti.quiz.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.agh.zti.quiz.dto.HostRequest;
import pl.agh.zti.quiz.dto.HostResponse;
import pl.agh.zti.quiz.service.QuizService;

@RestController
@RequestMapping("/api/hosts")
@RequiredArgsConstructor
public class HostController {

    private final QuizService quizService;

    /** Register a new host. Returns hostId used for subsequent quiz management. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public HostResponse register(@Valid @RequestBody HostRequest req) {
        return quizService.registerHost(req);
    }

    @PostMapping("/login")
    public HostResponse login(@Valid @RequestBody HostRequest req) {
        return quizService.loginHost(req);
    }

    @GetMapping("/{id}")
    public HostResponse get(@PathVariable Long id) {
        return quizService.getHost(id);
    }
}
