package guess.util.load;

import guess.domain.Conference;
import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.source.extract.ExtractPair;
import guess.domain.source.extract.ExtractSet;
import guess.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * CMS data loader.
 */
public abstract class CmsDataLoader {
    private static final Logger log = LoggerFactory.getLogger(CmsDataLoader.class);

    static final long JUG_RU_GROUP_ORGANIZER_ID = 0L;

    /**
     * Gets tags by conference code prefix.
     *
     * @param conferenceCodePrefix conference code prefix
     * @return tags
     */
    abstract Map<String, List<String>> getTags(String conferenceCodePrefix) throws IOException, NoSuchFieldException;

    /**
     * Gets event types.
     *
     * @return event types
     */
    abstract List<EventType> getEventTypes() throws IOException, NoSuchFieldException;

    /**
     * Gets event.
     *
     * @param conference     conference
     * @param startDate      start date
     * @param conferenceCode conference code
     * @param eventTemplate  event template
     * @return event
     */
    abstract Event getEvent(Conference conference, LocalDate startDate, String conferenceCode, Event eventTemplate) throws IOException, NoSuchFieldException;

    /**
     * Gets talks
     *
     * @param conference      conference
     * @param startDate       start date
     * @param conferenceCode  conference code
     * @param ignoreDemoStage ignore demo stage talks
     * @return talks
     */
    abstract List<Talk> getTalks(Conference conference, LocalDate startDate, String conferenceCode, boolean ignoreDemoStage) throws IOException, NoSuchFieldException;

    /**
     * Gets template of image parameters.
     *
     * @return template of image parameters
     */
    abstract String getImageParametersTemplate();

    static RestTemplate createRestTemplate() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new FormHttpMessageConverter());

        return new RestTemplate(converters);
    }

    /**
     * Extracts string, i.e. trims not null string.
     *
     * @param value                      source value
     * @param removeDuplicateWhiteSpaces {@code true} if need to remove duplicate white spaces, {@code false} otherwise
     * @return extracted string
     */
    static String extractString(String value, boolean removeDuplicateWhiteSpaces) {
        if (value != null) {
            String trimmedValue = value.trim();

            if (removeDuplicateWhiteSpaces) {
                return trimmedValue.replaceAll("\\s+", " ");
            } else {
                return trimmedValue;
            }
        } else {
            return null;
        }
    }

    /**
     * Extracts string, i.e. trims not null string.
     *
     * @param value source value
     * @return extracted string
     */
    static String extractString(String value) {
        return extractString(value, false);
    }

    /**
     * Extracts local items.
     *
     * @param enText                     English text
     * @param ruText                     Russian text
     * @param checkEnTextExistence       {@code true} if need to check English text existence, {@code false} otherwise
     * @param removeDuplicateWhiteSpaces {@code true} if need to remove duplicate white spaces, {@code false} otherwise
     * @return local items
     */
    static List<LocaleItem> extractLocaleItems(String enText, String ruText, boolean checkEnTextExistence, boolean removeDuplicateWhiteSpaces) {
        enText = extractString(enText, removeDuplicateWhiteSpaces);
        ruText = extractString(ruText, removeDuplicateWhiteSpaces);

        if (checkEnTextExistence &&
                ((enText == null) || enText.isEmpty()) &&
                ((ruText != null) && !ruText.isEmpty())) {
            log.warn("Invalid arguments: enText is empty, ruText is not empty ('{}')", ruText);
        }

        if (Objects.equals(enText, ruText)) {
            ruText = null;
        }

        List<LocaleItem> localeItems = new ArrayList<>();

        if ((enText != null) && !enText.isEmpty()) {
            localeItems.add(new LocaleItem(
                    Language.ENGLISH.getCode(),
                    enText));
        }

        if ((ruText != null) && !ruText.isEmpty()) {
            localeItems.add(new LocaleItem(
                    Language.RUSSIAN.getCode(),
                    ruText));
        }

        return localeItems;
    }

    /**
     * Extracts local items.
     *
     * @param enText               English text
     * @param ruText               Russian text
     * @param checkEnTextExistence {@code true} if need to check English text existence, {@code false} otherwise
     * @return local items
     */
    static List<LocaleItem> extractLocaleItems(String enText, String ruText, boolean checkEnTextExistence) {
        return extractLocaleItems(enText, ruText, checkEnTextExistence, false);
    }

    /**
     * Extracts local items.
     *
     * @param enText English text
     * @param ruText Russian text
     * @return local items
     */
    static List<LocaleItem> extractLocaleItems(String enText, String ruText) {
        return extractLocaleItems(enText, ruText, true);
    }

    /**
     * Gets fixed name.
     *
     * @param name name
     * @return fixed name
     */
    static String getSpeakerFixedName(String name) {
        Map<String, String> fixedLastNames = Map.of("Аксенов", "Аксёнов", "Богачев", "Богачёв", "Горбачев", "Горбачёв",
                "Королев", "Королёв", "Плетнев", "Плетнёв", "Пономарев", "Пономарёв",
                "Толкачев", "Толкачёв", "Усачев", "Усачёв", "Федоров", "Фёдоров", "Шипилев", "Шипилёв");
        Map<String, String> fixedFirstNames = Map.of("Алена", "Алёна", "Артем", "Артём",
                "Петр", "Пётр", "Семен", "Семён", "Федор", "Фёдор");

        if ((name == null) || name.isEmpty()) {
            return name;
        }

        // Change last names
        for (var fixedLastName : fixedLastNames.entrySet()) {
            String nameWithFixedLastName = name.replaceAll(String.format("(?U)\\b%s$", fixedLastName.getKey()), fixedLastName.getValue());

            if (!name.equals(nameWithFixedLastName)) {
                log.warn("Speaker last name is changed; original: {}, changed: {}", name, nameWithFixedLastName);
                name = nameWithFixedLastName;

                break;
            }
        }

        // Change first names
        for (var fixedFirstName : fixedFirstNames.entrySet()) {
            String nameWithFixedFirstName = name.replaceAll(String.format("(?U)^%s\\b", fixedFirstName.getKey()), fixedFirstName.getValue());

            if (!name.equals(nameWithFixedFirstName)) {
                log.warn("Speaker first name is changed; original: {}, changed: {}", name, nameWithFixedFirstName);
                name = nameWithFixedFirstName;

                break;
            }
        }

        return name;
    }

    /**
     * Extracts value.
     *
     * @param value      source value
     * @param extractSet extract set
     * @return property
     */
    public static String extractProperty(String value, ExtractSet extractSet) {
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return value;
        }

        for (ExtractPair extractPair : extractSet.pairs()) {
            var pattern = Pattern.compile(extractPair.patternRegex());
            var matcher = pattern.matcher(value);
            if (matcher.matches()) {
                return matcher.group(extractPair.groupIndex());
            }
        }

        throw new IllegalArgumentException(String.format(extractSet.exceptionMessage(), value));
    }

    /**
     * Extracts Twitter username.
     *
     * @param value source value
     * @return extracted Twitter username
     */
    static String extractTwitter(String value) {
        return extractProperty(value, new ExtractSet(
                List.of(
                        new ExtractPair("^[\\s]*[@]?(\\w{1,15})[\\s]*$", 1),
                        new ExtractPair("^[\\s]*((http(s)?://)?twitter.com/)?(\\w{1,15})[\\s]*$", 4)),
                "Invalid Twitter username: %s (change regular expression and rerun)"));
    }

    /**
     * Extracts GitHub username.
     *
     * @param value source value
     * @return extracted GitHub username
     */
    public static String extractGitHub(String value) {
        if (value != null) {
            value = value.replaceAll("\\.+", "-");
        }

        return extractProperty(value, new ExtractSet(
                List.of(
                        new ExtractPair("^[\\s]*((http(s)?://)?github.com/)?([a-zA-Z0-9\\-]+)(/)?[\\s]*$", 4),
                        new ExtractPair("^[\\s]*((http(s)?://)?github.com/)?([a-zA-Z0-9\\-]+)[/?]{1}.+$", 4),
                        new ExtractPair("^[\\s]*(http(s)?://)?([a-zA-Z0-9\\-]+).github.io/blog(/)?[\\s]*$", 3)),
                "Invalid GitHub username: %s (change regular expressions and rerun)"));
    }

    /**
     * Creates company list.
     *
     * @param enName               English name
     * @param ruName               Russian name
     * @param companyId            company identifier
     * @param checkEnTextExistence {@code true} if need to check English text existence, {@code false} otherwise
     * @return company list
     */
    static List<Company> createCompanies(String enName, String ruName, AtomicLong companyId, boolean checkEnTextExistence) {
        if (((enName != null) && !enName.isEmpty()) ||
                ((ruName != null) && !ruName.isEmpty())) {
            List<Company> companies = new ArrayList<>();

            companies.add(new Company(
                    companyId.getAndDecrement(),
                    extractLocaleItems(enName, ruName, checkEnTextExistence, true)));

            return companies;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Creates event local date from zoned date time string.
     *
     * @param zonedDateTimeString zoned date time string
     * @return event local date
     */
    static LocalDate createEventLocalDate(String zonedDateTimeString) {
        return ZonedDateTime.ofInstant(
                        ZonedDateTime.parse(zonedDateTimeString).toInstant(),
                        ZoneId.of(DateTimeUtils.MOSCOW_TIME_ZONE))
                .toLocalDate();
    }

    /**
     * Creates event local time from zoned date time string.
     *
     * @param zonedDateTimeString zoned date time string
     * @return event local time
     */
    static LocalTime createEventLocalTime(String zonedDateTimeString) {
        return ZonedDateTime.ofInstant(
                        ZonedDateTime.parse(zonedDateTimeString).toInstant(),
                        ZoneId.of(DateTimeUtils.MOSCOW_TIME_ZONE))
                .toLocalTime();
    }
}
