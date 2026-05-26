package pl.agh.zti.quiz.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.agh.zti.quiz.dto.*;
import pl.agh.zti.quiz.service.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse create(@RequestParam Long hostId, @Valid @RequestBody QuizRequest req) {
        return quizService.createQuiz(hostId, req);
    }

    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable Long id) {
        return quizService.getQuiz(id);
    }

    @GetMapping
    public List<QuizResponse> listByHost(@RequestParam Long hostId) {
        return quizService.getQuizzesByHost(hostId);
    }

    @PutMapping("/{id}")
    public QuizResponse update(@PathVariable Long id, @Valid @RequestBody QuizRequest req) {
        return quizService.updateQuiz(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quizService.deleteQuiz(id);
    }

    // ---- Questions ----

    @PostMapping("/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse addQuestion(@PathVariable Long quizId, @Valid @RequestBody QuestionRequest req) {
        return quizService.addQuestion(quizId, req);
    }

    @PutMapping("/{quizId}/questions/{questionId}")
    public QuestionResponse updateQuestion(@PathVariable Long quizId,
                                           @PathVariable Long questionId,
                                           @Valid @RequestBody QuestionRequest req) {
        return quizService.updateQuestion(questionId, req);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Long quizId, @PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
    }

    // ---- Answers ----

    @PostMapping("/{quizId}/questions/{questionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerResponse addAnswer(@PathVariable Long quizId,
                                    @PathVariable Long questionId,
                                    @Valid @RequestBody AnswerRequest req) {
        return quizService.addAnswer(questionId, req);
    }

    @PutMapping("/{quizId}/questions/{questionId}/answers/{answerId}")
    public AnswerResponse updateAnswer(@PathVariable Long quizId,
                                       @PathVariable Long questionId,
                                       @PathVariable Long answerId,
                                       @Valid @RequestBody AnswerRequest req) {
        return quizService.updateAnswer(answerId, req);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}/answers/{answerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswer(@PathVariable Long quizId,
                             @PathVariable Long questionId,
                             @PathVariable Long answerId) {
        quizService.deleteAnswer(answerId);
    }
}
