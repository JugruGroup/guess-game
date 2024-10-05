package guess.dao;

import guess.domain.answer.AnswerSet;
import guess.util.HttpSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

@DisplayName("AnswerDaoImpl class tests")
class AnswerDaoImplTest {
    private static final AnswerDao answerDao = new AnswerDaoImpl();

    @Test
    void getAnswerSets() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            answerDao.getAnswerSets(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.getAnswerSets(httpSession), Mockito.times(1));
        }
    }

    @Test
    void clearAnswerSets() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();

            answerDao.clearAnswerSets(httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.clearAnswerSets(httpSession), Mockito.times(1));
        }
    }

    @Test
    void addAnswerSet() {
        try (MockedStatic<HttpSessionUtils> mockedStatic = Mockito.mockStatic(HttpSessionUtils.class)) {
            HttpSession httpSession = new MockHttpSession();
            AnswerSet answerSet = new AnswerSet(
                    List.of(0L),
                    List.of(1L, 0L),
                    false);

            answerDao.addAnswerSet(answerSet, httpSession);

            mockedStatic.verify(() -> HttpSessionUtils.addAnswerSet(answerSet, httpSession), Mockito.times(1));
        }
    }
}
