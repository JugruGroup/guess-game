package guess.domain.statistics.olap.statistics;

import guess.domain.source.Company;
import guess.domain.source.EventType;
import guess.domain.source.Speaker;
import guess.domain.source.Topic;
import guess.domain.statistics.olap.dimension.City;

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
