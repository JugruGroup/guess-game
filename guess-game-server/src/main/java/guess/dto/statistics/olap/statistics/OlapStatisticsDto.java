package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.source.EventType;
import guess.domain.source.Speaker;
import guess.domain.statistics.olap.dimension.City;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;

/**
 * OLAP statistics DTO.
 */
public record OlapStatisticsDto(OlapEventTypeStatisticsDto eventTypeStatistics,
                                OlapSpeakerStatisticsDto speakerStatistics,
                                OlapCompanyStatisticsDto companyStatistics,
                                OlapTopicStatisticsDto topicStatistics,
                                Olap3dCubeStatisticsDto threeDimensionsStatistics) {
    public static OlapStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        OlapEntityStatistics<Integer, City, EventType> eventTypeStatistics = olapStatistics.yearEventTypeStatistics();
        OlapEntityStatistics<Integer, EventType, Speaker> speakerStatistics = olapStatistics.yearSpeakerStatistics();
        OlapEntityStatistics<Integer, EventType, Company> companyStatistics = olapStatistics.yearCompanyStatistics();

        return new OlapStatisticsDto(
                (eventTypeStatistics != null) ? OlapEventTypeStatisticsDto.convertToDto(eventTypeStatistics, language) : null,
                (speakerStatistics != null) ? OlapSpeakerStatisticsDto.convertToDto(speakerStatistics, language) : null,
                (companyStatistics != null) ? OlapCompanyStatisticsDto.convertToDto(companyStatistics, language) : null,
                OlapTopicStatisticsDto.convertToDto(olapStatistics, language),
                Olap3dCubeStatisticsDto.convertToDto(olapStatistics, language));
    }
}
