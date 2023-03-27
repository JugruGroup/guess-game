package guess.domain.auxiliary;

import guess.domain.source.Event;

import java.time.LocalDateTime;

/**
 * Event, minimal start time, end date time.
 */
public record EventMinStartTimeEndDayTime(Event event, LocalDateTime minStartDateTime, LocalDateTime endDayDateTime) {
}
