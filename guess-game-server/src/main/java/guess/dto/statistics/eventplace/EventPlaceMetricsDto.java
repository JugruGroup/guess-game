package guess.dto.statistics.eventplace;

import guess.domain.Language;
import guess.domain.statistics.eventplace.EventPlaceMetrics;

import java.util.List;

/**
 * Event place metrics DTO.
 */
public class EventPlaceMetricsDto {
    public static EventPlaceMetricsDto convertToDto(EventPlaceMetrics eventPlaceMetrics, Language language) {
        //TODO: implement
        return new EventPlaceMetricsDto();
    }

    public static List<EventPlaceMetricsDto> convertToDto(List<EventPlaceMetrics> eventPlaceMetricsList, Language language) {
        return eventPlaceMetricsList.stream()
                .map(epm -> convertToDto(epm, language))
                .toList();
    }
}
