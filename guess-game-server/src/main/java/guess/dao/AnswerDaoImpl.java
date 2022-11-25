package guess.dao;

import guess.domain.answer.AnswerSet;
import guess.util.HttpSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Answer DAO implementation.
 */
@Repository
public class AnswerDaoImpl implements AnswerDao {
    @Override
    public List<AnswerSet> getAnswerSets(HttpSession httpSession) {
        return HttpSessionUtils.getAnswerSets(httpSession);
    }

    @Override
    public void clearAnswerSets(HttpSession httpSession) {
        HttpSessionUtils.clearAnswerSets(httpSession);
    }

    @Override
    public void addAnswerSet(AnswerSet answerSet, HttpSession httpSession) {
        HttpSessionUtils.addAnswerSet(answerSet, httpSession);
    }
}
