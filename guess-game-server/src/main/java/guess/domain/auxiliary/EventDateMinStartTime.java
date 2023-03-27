package guess.domain.auxiliary;

import guess.domain.source.Event;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Event, date, minimal start time.
 */
public record EventDateMinStartTime(Event event, LocalDate date, LocalTime minStartTime) {
}
