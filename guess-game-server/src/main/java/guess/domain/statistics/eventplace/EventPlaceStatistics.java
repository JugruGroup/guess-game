package guess.domain.statistics.eventplace;

import java.util.List;

/**
 * Event place statistics.
 */
public record EventPlaceStatistics(List<EventPlaceMetrics> eventPlaceMetricsList, EventPlaceMetrics totals) {
}
