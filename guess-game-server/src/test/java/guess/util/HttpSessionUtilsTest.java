package guess.util;

import guess.domain.GameState;
import guess.domain.GuessMode;
import guess.domain.Quadruple;
import guess.domain.StartParameters;
import guess.domain.answer.AnswerSet;
import guess.domain.answer.SpeakerAnswer;
import guess.domain.question.QuestionAnswers;
import guess.domain.question.QuestionAnswersSet;
import guess.domain.question.SpeakerQuestion;
import guess.domain.source.LocaleItem;
import guess.domain.source.Speaker;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("HttpSessionUtils class tests")
class HttpSessionUtilsTest {
    private static final GameState state0 = GameState.GUESS_NAME_BY_PHOTO_STATE;
    private static final GameState state1 = GameState.RESULT_STATE;

    private static final StartParameters startParameters0 = new StartParameters(
            List.of(0L),
            List.of(0L, 1L),
            GuessMode.GUESS_NAME_BY_PHOTO_MODE,
            42);
    private static final StartParameters startParameters1 = new StartParameters(
            List.of(1L),
            List.of(2L, 3L),
            GuessMode.GUESS_NAME_BY_PHOTO_MODE,
            43);

    private static QuestionAnswersSet questionAnswersSet0;
    private static QuestionAnswersSet questionAnswersSet1;

    private static AnswerSet answerSet0;
    private static AnswerSet answerSet1;

    private static List<AnswerSet> answerSets0;
    private static List<AnswerSet> answerSets1;

    @BeforeAll
    static void init() {
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

        questionAnswersSet0 = new QuestionAnswersSet(
                List.of(new LocaleItem("en", "Name0")),
                "logoFileName0",
                List.of(new QuestionAnswers(
                        new SpeakerQuestion(speaker0),
                        List.of(speakerAnswer0),
                        new Quadruple<>(speakerAnswer0, speakerAnswer1, speakerAnswer2, speakerAnswer3)
                ))
        );

        questionAnswersSet1 = new QuestionAnswersSet(
                List.of(new LocaleItem("en", "Name1")),
                "logoFileName1",
                List.of(new QuestionAnswers(
                        new SpeakerQuestion(speaker1),
                        List.of(speakerAnswer1),
                        new Quadruple<>(speakerAnswer0, speakerAnswer1, speakerAnswer2, speakerAnswer3)
                ))
        );

        answerSet0 = new AnswerSet(
                List.of(0L),
                List.of(1L, 0L),
                false);
        answerSet1 = new AnswerSet(
                List.of(0L, 1L),
                List.of(1L, 0L),
                true);

        answerSets0 = List.of(answerSet0, answerSet1);
        answerSets1 = List.of(answerSet1);
    }

    @Test
    void getGameState() {
        HttpSession httpSession = new MockHttpSession();

        assertEquals(GameState.START_STATE, HttpSessionUtils.getGameState(httpSession));

        httpSession.setAttribute(HttpSessionUtils.STATE_ATTRIBUTE_NAME, state0);
        assertEquals(state0, HttpSessionUtils.getGameState(httpSession));

        httpSession.setAttribute(HttpSessionUtils.STATE_ATTRIBUTE_NAME, state1);
        assertEquals(state1, HttpSessionUtils.getGameState(httpSession));
    }

    @Test
    void setGameState() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.STATE_ATTRIBUTE_NAME));

        HttpSessionUtils.setGameState(state0, httpSession);
        assertEquals(state0, httpSession.getAttribute(HttpSessionUtils.STATE_ATTRIBUTE_NAME));

        HttpSessionUtils.setGameState(state1, httpSession);
        assertEquals(state1, httpSession.getAttribute(HttpSessionUtils.STATE_ATTRIBUTE_NAME));
    }

    @Test
    void getStartParameters() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(HttpSessionUtils.getStartParameters(httpSession));

        httpSession.setAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME, startParameters0);
        assertEquals(startParameters0, HttpSessionUtils.getStartParameters(httpSession));

        httpSession.setAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME, startParameters1);
        assertEquals(startParameters1, HttpSessionUtils.getStartParameters(httpSession));
    }

    @Test
    void setStartParameters() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));

        HttpSessionUtils.setStartParameters(startParameters0, httpSession);
        assertEquals(startParameters0, httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));

        HttpSessionUtils.setStartParameters(startParameters1, httpSession);
        assertEquals(startParameters1, httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));
    }

    @Test
    void clearStartParameters() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));

        HttpSessionUtils.clearStartParameters(httpSession);
        assertNull(httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));

        httpSession.setAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME, startParameters0);
        assertEquals(startParameters0, httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));

        HttpSessionUtils.clearStartParameters(httpSession);
        assertNull(httpSession.getAttribute(HttpSessionUtils.START_PARAMETERS_ATTRIBUTE_NAME));
    }

    @Test
    void getQuestionAnswersSet() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(HttpSessionUtils.getQuestionAnswersSet(httpSession));

        httpSession.setAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME, questionAnswersSet0);
        assertEquals(questionAnswersSet0, HttpSessionUtils.getQuestionAnswersSet(httpSession));

        httpSession.setAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME, questionAnswersSet1);
        assertEquals(questionAnswersSet1, HttpSessionUtils.getQuestionAnswersSet(httpSession));
    }

    @Test
    void clearQuestionAnswersSet() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));

        HttpSessionUtils.clearQuestionAnswersSet(httpSession);
        assertNull(httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));

        httpSession.setAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME, questionAnswersSet0);
        assertEquals(questionAnswersSet0, httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));

        HttpSessionUtils.clearQuestionAnswersSet(httpSession);
        assertNull(httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));
    }

    @Test
    void setQuestionAnswersSet() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));

        HttpSessionUtils.setQuestionAnswersSet(questionAnswersSet0, httpSession);
        assertEquals(questionAnswersSet0, httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));

        HttpSessionUtils.setQuestionAnswersSet(questionAnswersSet1, httpSession);
        assertEquals(questionAnswersSet1, httpSession.getAttribute(HttpSessionUtils.QUESTION_ANSWERS_SET_ATTRIBUTE_NAME));
    }

    @Test
    void getAnswerSets() {
        HttpSession httpSession = new MockHttpSession();

        assertEquals(Collections.emptyList(), HttpSessionUtils.getAnswerSets(httpSession));

        httpSession.setAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME, answerSets0);
        assertEquals(answerSets0, HttpSessionUtils.getAnswerSets(httpSession));

        httpSession.setAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME, answerSets1);
        assertEquals(answerSets1, HttpSessionUtils.getAnswerSets(httpSession));
    }

    @Test
    void clearAnswerSets() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));

        HttpSessionUtils.clearAnswerSets(httpSession);
        assertNull(httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));

        httpSession.setAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME, answerSets0);
        assertEquals(answerSets0, httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));

        HttpSessionUtils.clearAnswerSets(httpSession);
        assertNull(httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));
    }

    @Test
    void addAnswerSet() {
        HttpSession httpSession = new MockHttpSession();

        assertNull(httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));

        HttpSessionUtils.addAnswerSet(answerSet0, httpSession);
        assertEquals(List.of(answerSet0), httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));

        HttpSessionUtils.addAnswerSet(answerSet1, httpSession);
        assertEquals(List.of(answerSet0, answerSet1), httpSession.getAttribute(HttpSessionUtils.ANSWER_SETS_ATTRIBUTE_NAME));
    }
}
