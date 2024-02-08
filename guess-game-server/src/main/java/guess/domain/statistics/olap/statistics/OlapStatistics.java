package guess.domain.statistics.olap.statistics;

import guess.domain.source.*;

/**
 * OLAP statistics.
 */
public record OlapStatistics(OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics,
                             OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics,
                             OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics,
                             OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics,
                             OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics,
                             OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics) {
}
