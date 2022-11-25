package guess.dao;

import guess.domain.Language;
import jakarta.servlet.http.HttpSession;

/**
 * Locale DAO.
 */
public interface LocaleDao {
    Language getLanguage(HttpSession httpSession);

    void setLanguage(Language language, HttpSession httpSession);
}
