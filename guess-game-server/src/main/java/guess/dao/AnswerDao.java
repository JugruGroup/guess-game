package guess.dao;

import guess.domain.answer.AnswerSet;
import jakarta.servlet.http.HttpSession;

import java.util.List;

/**
 * Answer DAO.
 */
public interface AnswerDao {
    List<AnswerSet> getAnswerSets(HttpSession httpSession);

    void clearAnswerSets(HttpSession httpSession);

    void addAnswerSet(AnswerSet answerSet, HttpSession httpSession);
}
