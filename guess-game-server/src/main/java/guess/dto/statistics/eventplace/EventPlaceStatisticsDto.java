package guess.dto.statistics.eventplace;

import guess.domain.Language;
import guess.domain.statistics.eventplace.EventPlaceStatistics;

import java.util.List;

/**
 * Event place statistics DTO.
 */
public record EventPlaceStatisticsDto(List<EventPlaceMetricsDto> eventPlaceMetricsList, EventPlaceMetricsDto totals) {
    public static EventPlaceStatisticsDto convertToDto(EventPlaceStatistics eventPlaceStatistics, Language language) {
        return new EventPlaceStatisticsDto(
                EventPlaceMetricsDto.convertToDto(eventPlaceStatistics.eventPlaceMetricsList(), language),
                EventPlaceMetricsDto.convertToDto(eventPlaceStatistics.totals(), language));
    }
}
