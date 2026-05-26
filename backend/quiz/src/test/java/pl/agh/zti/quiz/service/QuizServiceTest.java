package pl.agh.zti.quiz.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.agh.zti.quiz.domain.*;
import pl.agh.zti.quiz.dto.*;
import pl.agh.zti.quiz.repository.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock HostRepository hostRepo;
    @Mock QuizRepository quizRepo;
    @Mock QuestionRepository questionRepo;
    @Mock AnswerRepository answerRepo;

    @InjectMocks QuizService service;

    private Host host;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        host = Host.builder().id(1L).username("alice").passwordHash("x").build();
        quiz = Quiz.builder().id(10L).title("Test Quiz").defaultTimeLimitSec(30).host(host).build();
    }

    @Test
    void createQuiz_savesAndReturnsResponse() {
        QuizRequest req = new QuizRequest();
        req.setTitle("Test Quiz");
        req.setDefaultTimeLimitSec(20);

        when(hostRepo.findById(1L)).thenReturn(Optional.of(host));
        when(quizRepo.save(any())).thenReturn(quiz);

        QuizResponse res = service.createQuiz(1L, req);

        assertThat(res.getTitle()).isEqualTo("Test Quiz");
        assertThat(res.getHostId()).isEqualTo(1L);
        verify(quizRepo).save(any(Quiz.class));
    }

    @Test
    void createQuiz_throwsWhenHostNotFound() {
        when(hostRepo.findById(99L)).thenReturn(Optional.empty());

        QuizRequest req = new QuizRequest();
        req.setTitle("X");

        assertThatThrownBy(() -> service.createQuiz(99L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Host not found");
    }

    @Test
    void addQuestion_assignsNextOrderIndex() {
        QuestionRequest req = new QuestionRequest();
        req.setContent("What?");

        Question saved = Question.builder().id(1L).content("What?").orderIndex(2).quiz(quiz).build();

        when(quizRepo.findById(10L)).thenReturn(Optional.of(quiz));
        when(questionRepo.countByQuizId(10L)).thenReturn(2);
        when(questionRepo.save(any())).thenReturn(saved);

        QuestionResponse res = service.addQuestion(10L, req);

        assertThat(res.getOrderIndex()).isEqualTo(2);
        verify(questionRepo).save(argThat(q -> q.getOrderIndex() == 2));
    }

    @Test
    void addAnswer_linksToQuestion() {
        Question q = Question.builder().id(5L).content("?").orderIndex(0).quiz(quiz).build();
        AnswerRequest req = new AnswerRequest();
        req.setContent("Paris");
        req.setIsCorrect(true);

        Answer saved = Answer.builder().id(1L).content("Paris").isCorrect(true).question(q).build();

        when(questionRepo.findById(5L)).thenReturn(Optional.of(q));
        when(answerRepo.save(any())).thenReturn(saved);

        AnswerResponse res = service.addAnswer(5L, req);

        assertThat(res.getContent()).isEqualTo("Paris");
        assertThat(res.getIsCorrect()).isTrue();
    }

    @Test
    void getQuizzesByHost_returnsList() {
        when(quizRepo.findByHostId(1L)).thenReturn(List.of(quiz));

        List<QuizResponse> list = service.getQuizzesByHost(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTitle()).isEqualTo("Test Quiz");
    }

    @Test
    void deleteQuiz_callsRepository() {
        service.deleteQuiz(10L);
        verify(quizRepo).deleteById(10L);
    }

    @Test
    void registerHost_throwsOnDuplicateUsername() {
        when(hostRepo.existsByUsername("alice")).thenReturn(true);

        HostRequest req = new HostRequest();
        req.setUsername("alice");
        req.setPassword("pass");

        assertThatThrownBy(() -> service.registerHost(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }
}
