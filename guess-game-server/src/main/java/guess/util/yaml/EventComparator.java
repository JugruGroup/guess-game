package guess.util.yaml;

import guess.domain.source.Event;
import guess.domain.source.Talk;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Event comparator.
 */
public class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event event1, Event event2) {
        // Compare event
        if (event1 == null) {
            if (event2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (event2 == null) {
                return 1;
            } else {
                // Compare start date
                return compareStartDate(event1, event2);
            }
        }
    }

    static int compareStartDate(Event event1, Event event2) {
        LocalDate eventStartDate1 = event1.getFirstStartDate();
        LocalDate eventStartDate2 = event2.getFirstStartDate();

        if (eventStartDate1 == null) {
            if (eventStartDate2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (eventStartDate2 == null) {
                return 1;
            } else {
                // Compare track time
                if (eventStartDate1.isEqual(eventStartDate2)) {
                    return compareStartTime(event1, event2);
                } else {
                    return eventStartDate1.compareTo(eventStartDate2);
                }
            }
        }
    }

    static Optional<LocalTime> getFirstStartTime(List<Talk> talks) {
        return talks.stream()
                .filter(t -> (t.getTalkDay() != null) && (t.getStartTime() != null))
                .sorted(Comparator.comparing(Talk::getTalkDay).thenComparing(Talk::getStartTime))
                .map(Talk::getStartTime)
                .findFirst();
    }

    static int compareStartTime(Event event1, Event event2) {
        Optional<LocalTime> eventStartTime1 = getFirstStartTime(event1.getTalks());
        Optional<LocalTime> eventStartTime2 = getFirstStartTime(event2.getTalks());

        if (eventStartTime1.isEmpty()) {
            if (eventStartTime2.isEmpty()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (eventStartTime2.isEmpty()) {
                return 1;
            } else {
                if (eventStartTime1.get().equals(eventStartTime2.get())) {
                    return 0;
                } else {
                    return eventStartTime1.get().compareTo(eventStartTime2.get());
                }
            }
        }
    }
}
