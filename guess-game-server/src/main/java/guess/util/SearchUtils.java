package guess.util;

import guess.domain.source.EventDays;
import guess.domain.source.LocaleItem;
import guess.domain.source.Place;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Search methods.
 */
public class SearchUtils {
    private static final String ALPHA_NUMERIC_CHARACTERS = "\\p{L}0-9";
    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile(String.format("^[^%s]*([%s]{1}.*)$", ALPHA_NUMERIC_CHARACTERS, ALPHA_NUMERIC_CHARACTERS));

    private SearchUtils() {
    }

    public static String trimAndLowerCase(String value) {
        return (value != null) ? value.trim().toLowerCase() : null;
    }

    public static boolean isStringSet(String value) {
        return ((value != null) && !value.isEmpty());
    }

    public static boolean isSubstringFound(String trimmedLowerCasedSubstring, String item) {
        if (!isStringSet(trimmedLowerCasedSubstring)) {
            return false;
        }

        String trimmedLowerCasedItem = trimAndLowerCase(item);

        if (!isStringSet(trimmedLowerCasedItem)) {
            return false;
        }

        return trimmedLowerCasedItem.contains(trimmedLowerCasedSubstring);
    }

    public static boolean isSubstringFound(String trimmedLowerCasedSubstring, List<LocaleItem> localeItems) {
        if (!isStringSet(trimmedLowerCasedSubstring) || (localeItems == null)) {
            return false;
        }

        for (LocaleItem localeItem : localeItems) {
            if (isSubstringFound(trimmedLowerCasedSubstring, localeItem.getText())) {
                return true;
            }
        }

        return false;
    }

    public static String getSubStringWithFirstAlphaNumeric(String value) {
        if (value == null) {
            return null;
        }

        var matcher = ALPHA_NUMERIC_PATTERN.matcher(value);

        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return value;
        }
    }

    public static Map<Long, Place> getTalkDayPlaces(List<EventDays> eventDaysList) {
        Map<Long, Place> talkDayDates = new HashMap<>();
        long previousDays = 0;

        for (EventDays eventDays : eventDaysList) {
            long days = ChronoUnit.DAYS.between(eventDays.getStartDate(), eventDays.getEndDate()) + 1;

            for (long i = 1; i <= days; i++) {
                talkDayDates.put(previousDays + i, eventDays.getPlace());
            }

            previousDays += days;
        }

        return talkDayDates;
    }
}
