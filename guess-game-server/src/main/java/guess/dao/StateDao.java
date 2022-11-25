package guess.dao;

import guess.domain.GameState;
import guess.domain.StartParameters;
import guess.domain.question.QuestionAnswersSet;
import jakarta.servlet.http.HttpSession;

/**
 * State DAO.
 */
public interface StateDao {
    GameState getGameState(HttpSession httpSession);

    void setGameState(GameState state, HttpSession httpSession);

    StartParameters getStartParameters(HttpSession httpSession);

    void setStartParameters(StartParameters startParameters, HttpSession httpSession);

    void clearStartParameters(HttpSession httpSession);

    QuestionAnswersSet getQuestionAnswersSet(HttpSession httpSession);

    void setQuestionAnswersSet(QuestionAnswersSet questionAnswersSet, HttpSession httpSession);

    void clearQuestionAnswersSet(HttpSession httpSession);
}
