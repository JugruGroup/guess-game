package guess.service;

import guess.domain.source.EventType;
import guess.domain.statistics.CompanyStatistics;
import guess.domain.statistics.EventStatistics;
import guess.domain.statistics.EventTypeStatistics;
import guess.domain.statistics.SpeakerStatistics;

import java.util.List;

/**
 * Statistics service.
 */
public interface StatisticsService {
    List<EventType> getStatisticsEventTypes(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    EventTypeStatistics getEventTypeStatistics(boolean isConferences, boolean isMeetups, Long organizerId);

    EventStatistics getEventStatistics(Long eventTypeId);

    SpeakerStatistics getSpeakerStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    CompanyStatistics getCompanyStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    List<EventType> getConferences();
}
