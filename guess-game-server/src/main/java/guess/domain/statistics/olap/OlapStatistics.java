package guess.domain.statistics.olap;

import guess.domain.source.Company;
import guess.domain.source.EventType;
import guess.domain.source.Speaker;
import guess.domain.source.Topic;

/**
 * OLAP statistics.
 */
public record OlapStatistics(OlapEntityStatistics<Integer, EventType> yearEventTypeStatistics,
                             OlapEntityStatistics<Integer, Speaker> yearSpeakerStatistics,
                             OlapEntityStatistics<Integer, Company> yearCompanyStatistics,
                             OlapEntityStatistics<Topic, EventType> topicEventTypeStatistics,
                             OlapEntityStatistics<Topic, Speaker> topicSpeakerStatistics,
                             OlapEntityStatistics<Topic, Company> topicCompanyStatistics) {
}
