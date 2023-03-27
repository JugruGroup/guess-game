package guess.domain.source.cms.jrgcms.talk.schedule;

import java.time.LocalTime;

public record DayTrackTime(Long dayNumber, Long trackNumber, LocalTime startTime, LocalTime endTime) {
}
