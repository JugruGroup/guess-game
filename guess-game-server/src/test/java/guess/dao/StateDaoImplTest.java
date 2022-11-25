package guess.dao;

import guess.domain.GameState;
import guess.domain.GuessMode;
import guess.domain.Quadruple;
import guess.domain.StartParameters;
import guess.domain.answer.SpeakerAnswer;
import guess.domain.question.QuestionAnswers;
import guess.domain.question.QuestionAnswersSet;
import guess.domain.question.SpeakerQuestion;
import guess.domain.source.LocaleItem;
import guess.domain.source.Speaker;
import guess.util.HttpSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

@DisplayName("StateDaoImpl class tests")
class StateDaoImplTest {
    private final static StateDao stateDao = new StateDaoImpl();

    @Test
    void getGameState() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            stateDao.getGameState(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.getGameState(httpSession), Mockito.times(1));
        }
    }

    @Test
    void setGameState() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            GameState state = GameState.GUESS_NAME_BY_PHOTO_STATE;
            HttpSession httpSession = new MockHttpSession();

            stateDao.setGameState(state, httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.setGameState(state, httpSession), Mockito.times(1));
        }
    }

    @Test
    void getStartParameters() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            stateDao.getStartParameters(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.getStartParameters(httpSession), Mockito.times(1));
        }
    }

    @Test
    void setStartParameters() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            StartParameters startParameters = new StartParameters(
                    List.of(0L),
                    List.of(0L, 1L),
                    GuessMode.GUESS_NAME_BY_PHOTO_MODE,
                    42);
            HttpSession httpSession = new MockHttpSession();

            stateDao.setStartParameters(startParameters, httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.setStartParameters(startParameters, httpSession), Mockito.times(1));
        }
    }

    @Test
    void clearStartParameters() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            stateDao.clearStartParameters(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.clearStartParameters(httpSession), Mockito.times(1));
        }
    }

    @Test
    void getQuestionAnswersSet() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            stateDao.getQuestionAnswersSet(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.getQuestionAnswersSet(httpSession), Mockito.times(1));
        }
    }

    @Test
    void setQuestionAnswersSet() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            Speaker speaker0 = new Speaker();
            speaker0.setId(0);

            Speaker speaker1 = new Speaker();
            speaker0.setId(1);

            Speaker speaker2 = new Speaker();
            speaker0.setId(2);

            Speaker speaker3 = new Speaker();
            speaker0.setId(3);

            SpeakerAnswer speakerAnswer0 = new SpeakerAnswer(speaker0);
            SpeakerAnswer speakerAnswer1 = new SpeakerAnswer(speaker1);
            SpeakerAnswer speakerAnswer2 = new SpeakerAnswer(speaker2);
            SpeakerAnswer speakerAnswer3 = new SpeakerAnswer(speaker3);

            QuestionAnswersSet questionAnswersSet = new QuestionAnswersSet(
                    List.of(new LocaleItem("en", "Name0")),
                    "logoFileName0",
                    List.of(new QuestionAnswers(
                            new SpeakerQuestion(speaker0),
                            List.of(speakerAnswer0),
                            new Quadruple<>(speakerAnswer0, speakerAnswer1, speakerAnswer2, speakerAnswer3)
                    ))
            );
            HttpSession httpSession = new MockHttpSession();

            stateDao.setQuestionAnswersSet(questionAnswersSet, httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.setQuestionAnswersSet(questionAnswersSet, httpSession), Mockito.times(1));
        }
    }

    @Test
    void clearQuestionAnswersSet() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            stateDao.clearQuestionAnswersSet(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.clearQuestionAnswersSet(httpSession), Mockito.times(1));
        }
    }
}
