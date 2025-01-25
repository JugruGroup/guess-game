package guess.service;

import guess.dao.SpeakerDao;
import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.util.LocalizationUtils;
import guess.util.SearchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Speaker service implementation.
 */
@Service
public class SpeakerServiceImpl implements SpeakerService {
    private final SpeakerDao speakerDao;

    @Autowired
    public SpeakerServiceImpl(SpeakerDao speakerDao) {
        this.speakerDao = speakerDao;
    }

    @Override
    public Speaker getSpeakerById(long id) {
        return speakerDao.getSpeakerById(id);
    }

    @Override
    public List<Speaker> getSpeakerByIds(List<Long> ids) {
        return speakerDao.getSpeakerByIds(ids);
    }

    @Override
    public List<Speaker> getSpeakersByFirstLetter(String firstLetter, Language language) {
        return speakerDao.getSpeakers().stream()
                .filter(s -> {
                    var name = LocalizationUtils.getString(LocalizationUtils.getSpeakerNamesWithLastNameFirst(s), language);
                    String nameFirstLetter;

                    if (name != null) {
                        String trimmedName = name.trim();

                        nameFirstLetter = !trimmedName.isEmpty() ? trimmedName.substring(0, 1) : null;
                    } else {
                        nameFirstLetter = null;
                    }

                    return firstLetter.equalsIgnoreCase(nameFirstLetter);
                })
                .toList();
    }

    @Override
    public List<Speaker> getSpeakersByFirstLetters(String firstLetters, Language language) {
        String lowerCaseFirstLetters = (firstLetters != null) ? firstLetters.toLowerCase() : "";

        return speakerDao.getSpeakers().stream()
                .filter(s -> LocalizationUtils.getString(LocalizationUtils.getSpeakerNamesWithLastNameFirst(s), language)
                        .toLowerCase().indexOf(lowerCaseFirstLetters) == 0)
                .toList();
    }

    @Override
    public List<Speaker> getSpeakers(String name, String company, Speaker.SpeakerSocials speakerSocials,
                                     String description, boolean isJavaChampion, boolean isMvp) {
        String twitter = speakerSocials.getTwitter();
        String gitHub = speakerSocials.getGitHub();
        String habr = speakerSocials.getHabr();
        String trimmedLowerCasedName = SearchUtils.trimAndLowerCase(name);
        String trimmedLowerCasedCompany = SearchUtils.trimAndLowerCase(company);
        String trimmedLowerCasedTwitter = SearchUtils.trimAndLowerCase(twitter);
        String trimmedLowerCasedGitHub = SearchUtils.trimAndLowerCase(gitHub);
        String trimmedLowerCasedHabr = SearchUtils.trimAndLowerCase(habr);
        String trimmedLowerCasedDescription = SearchUtils.trimAndLowerCase(description);
        boolean isNameSet = SearchUtils.isStringSet(trimmedLowerCasedName);
        boolean isCompanySet = SearchUtils.isStringSet(trimmedLowerCasedCompany);
        boolean isTwitterSet = SearchUtils.isStringSet(trimmedLowerCasedTwitter);
        boolean isGitHubSet = SearchUtils.isStringSet(trimmedLowerCasedGitHub);
        boolean isHabrSet = SearchUtils.isStringSet(trimmedLowerCasedHabr);
        boolean isDescriptionSet = SearchUtils.isStringSet(trimmedLowerCasedDescription);

        if (!isNameSet && !isCompanySet && !isTwitterSet && !isGitHubSet && !isHabrSet && !isDescriptionSet && !isJavaChampion && !isMvp) {
            return Collections.emptyList();
        } else {
            return speakerDao.getSpeakers().stream()
                    .filter(s -> (isValidByName(s, isNameSet, trimmedLowerCasedName) &&
                            (!isCompanySet || isSpeakerCompanyFound(s, trimmedLowerCasedCompany)) &&
                            isValidByField(s, isTwitterSet, trimmedLowerCasedTwitter, Speaker::getTwitter) &&
                            isValidByField(s, isGitHubSet, trimmedLowerCasedGitHub, Speaker::getGitHub) &&
                            isValidByField(s, isHabrSet, trimmedLowerCasedHabr, Speaker::getHabr) &&
                            (!isDescriptionSet || SearchUtils.isSubstringFound(trimmedLowerCasedDescription, s.getBio())) &&
                            (!isJavaChampion || s.isJavaChampion()) &&
                            (!isMvp || s.isAnyMvp())))
                    .toList();
        }
    }

    @Override
    public List<Speaker> getSpeakersByCompanyId(long companyId) {
        return speakerDao.getSpeakers().stream()
                .filter(s -> s.getCompanyIds().contains(companyId))
                .toList();
    }

    static boolean isValidByName(Speaker s, boolean isNameSet, String trimmedLowerCasedName) {
        return (!isNameSet || SearchUtils.isSubstringFound(trimmedLowerCasedName, s.getName()) ||
                SearchUtils.isSubstringFound(trimmedLowerCasedName, LocalizationUtils.getSpeakerNamesWithLastNameFirst(s)));
    }

    static boolean isValidByField(Speaker s, boolean isStringSet, String trimmedLowerCasedString, Function<Speaker, String> fieldFunction) {
        return (!isStringSet || SearchUtils.isSubstringFound(trimmedLowerCasedString, fieldFunction.apply(s)));
    }

    static boolean isSpeakerCompanyFound(Speaker speaker, String trimmedLowerCasedCompany) {
        return speaker.getCompanies().stream()
                .anyMatch(c -> SearchUtils.isSubstringFound(trimmedLowerCasedCompany, c.getName()));
    }
}
