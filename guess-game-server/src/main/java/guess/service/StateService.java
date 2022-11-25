package guess.service;

import guess.domain.GameState;
import guess.domain.StartParameters;
import guess.domain.question.QuestionAnswersSet;
import jakarta.servlet.http.HttpSession;

/**
 * State service.
 */
public interface StateService {
    void setStartParameters(StartParameters startParameters, HttpSession httpSession);

    void deleteStartParameters(HttpSession httpSession);

    GameState getState(HttpSession httpSession);

    void setState(GameState state, HttpSession httpSession);

    QuestionAnswersSet getQuestionAnswersSet(HttpSession httpSession);
}
