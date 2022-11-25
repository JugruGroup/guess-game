package guess.dao;

import guess.domain.Language;
import guess.util.HttpSessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Repository;

/**
 * Locale DAO implementation.
 */
@Repository
public class LocaleDaoImpl implements LocaleDao {
    @Override
    public Language getLanguage(HttpSession httpSession) {
        return HttpSessionUtils.getLanguage(httpSession);
    }

    @Override
    public void setLanguage(Language language, HttpSession httpSession) {
        HttpSessionUtils.setLanguage(language, httpSession);
    }
}
