package pl.agh.zti.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.agh.zti.quiz.domain.Answer;
import pl.agh.zti.quiz.domain.Host;
import pl.agh.zti.quiz.domain.Question;
import pl.agh.zti.quiz.domain.Quiz;
import pl.agh.zti.quiz.dto.*;
import pl.agh.zti.quiz.repository.*;

import java.util.List;

/**
 * CRUD operations for quizzes, questions, and answers.
 * Referenced in component diagram: QuizService layer.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class QuizService {

    private final HostRepository hostRepo;
    private final QuizRepository quizRepo;
    private final QuestionRepository questionRepo;
    private final AnswerRepository answerRepo;

    // ---- Host ----

    public HostResponse registerHost(HostRequest req) {
        if (hostRepo.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + req.getUsername());
        }
        Host host = Host.builder()
                .username(req.getUsername())
                .passwordHash(req.getPassword()) // plain for now — no auth required per spec
                .build();
        return HostResponse.from(hostRepo.save(host));
    }

    @Transactional(readOnly = true)
    public HostResponse getHost(Long hostId) {
        return HostResponse.from(findHost(hostId));
    }

    // ---- Quiz ----

    public QuizResponse createQuiz(Long hostId, QuizRequest req) {
        Host host = findHost(hostId);
        Quiz quiz = Quiz.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .defaultTimeLimitSec(req.getDefaultTimeLimitSec() != null ? req.getDefaultTimeLimitSec() : 30)
                .host(host)
                .build();
        return QuizResponse.from(quizRepo.save(quiz));
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId) {
        return QuizResponse.from(findQuiz(quizId));
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByHost(Long hostId) {
        return quizRepo.findByHostId(hostId).stream().map(QuizResponse::from).toList();
    }

    public QuizResponse updateQuiz(Long quizId, QuizRequest req) {
        Quiz quiz = findQuiz(quizId);
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        if (req.getDefaultTimeLimitSec() != null) quiz.setDefaultTimeLimitSec(req.getDefaultTimeLimitSec());
        return QuizResponse.from(quizRepo.save(quiz));
    }

    public void deleteQuiz(Long quizId) {
        quizRepo.deleteById(quizId);
    }

    // ---- Question ----

    public QuestionResponse addQuestion(Long quizId, QuestionRequest req) {
        Quiz quiz = findQuiz(quizId);
        int nextIndex = questionRepo.countByQuizId(quizId);
        Question q = Question.builder()
                .content(req.getContent())
                .orderIndex(nextIndex)
                .timeLimitSec(req.getTimeLimitSec())
                .quiz(quiz)
                .build();
        return QuestionResponse.from(questionRepo.save(q));
    }

    public QuestionResponse updateQuestion(Long questionId, QuestionRequest req) {
        Question q = findQuestion(questionId);
        q.setContent(req.getContent());
        if (req.getTimeLimitSec() != null) q.setTimeLimitSec(req.getTimeLimitSec());
        return QuestionResponse.from(questionRepo.save(q));
    }

    public void deleteQuestion(Long questionId) {
        questionRepo.deleteById(questionId);
    }

    // ---- Answer ----

    public AnswerResponse addAnswer(Long questionId, AnswerRequest req) {
        Question q = findQuestion(questionId);
        Answer a = Answer.builder()
                .content(req.getContent())
                .isCorrect(req.getIsCorrect())
                .question(q)
                .build();
        return AnswerResponse.from(answerRepo.save(a));
    }

    public AnswerResponse updateAnswer(Long answerId, AnswerRequest req) {
        Answer a = findAnswer(answerId);
        a.setContent(req.getContent());
        a.setIsCorrect(req.getIsCorrect());
        return AnswerResponse.from(answerRepo.save(a));
    }

    public void deleteAnswer(Long answerId) {
        answerRepo.deleteById(answerId);
    }

    // ---- Helpers ----

    public Host findHost(Long id) {
        return hostRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Host not found: " + id));
    }

    public Quiz findQuiz(Long id) {
        return quizRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));
    }

    public Question findQuestion(Long id) {
        return questionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    private Answer findAnswer(Long id) {
        return answerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + id));
    }
}
