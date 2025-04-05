package guess.service;

import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.domain.statistics.company.CompanyStatistics;
import guess.domain.statistics.event.EventStatistics;
import guess.domain.statistics.eventplace.EventPlaceStatistics;
import guess.domain.statistics.eventtype.EventTypeStatistics;
import guess.domain.statistics.speaker.SpeakerStatistics;

import java.util.List;

/**
 * Statistics service.
 */
public interface StatisticsService {
    List<EventType> getStatisticsEventTypes(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId, Long eventTypeId);

    EventTypeStatistics getEventTypeStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId);

    List<Event> getFilteredEvents(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    EventStatistics getEventStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    EventPlaceStatistics getEventPlaceStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    SpeakerStatistics getSpeakerStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);

    CompanyStatistics getCompanyStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId);
}
