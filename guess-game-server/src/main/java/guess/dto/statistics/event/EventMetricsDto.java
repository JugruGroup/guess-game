package guess.dto.statistics.event;

import guess.domain.Language;
import guess.domain.statistics.EventTypeEventMetrics;
import guess.domain.statistics.event.EventMetrics;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;

/**
 * Event metrics DTO.
 */
public class EventMetricsDto extends EventTypeEventMetrics {
    private final long id;
    private final String name;
    private final String eventTypeLogoFileName;

    public EventMetricsDto(long id, String name, String eventTypeLogoFileName, EventTypeEventMetrics metrics) {
        super(metrics.getStartDate(), metrics.getEndDate(), metrics.getDuration(), metrics.getSpeakersQuantity(),
                metrics.getCompaniesQuantity(), metrics);

        this.id = id;
        this.name = name;
        this.eventTypeLogoFileName = eventTypeLogoFileName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEventTypeLogoFileName() {
        return eventTypeLogoFileName;
    }

    public static EventMetricsDto convertToDto(EventMetrics eventMetrics, Language language) {
        var event = eventMetrics.getEvent();

        return new EventMetricsDto(
                event.getId(),
                LocalizationUtils.getString(event.getName(), language),
                (event.getEventType() != null) ? event.getEventType().getLogoFileName() : null,
                eventMetrics);
    }

    public static List<EventMetricsDto> convertToDto(List<EventMetrics> eventMetricsList, Language language) {
        return eventMetricsList.stream()
                .map(etm -> convertToDto(etm, language))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventMetricsDto that)) return false;
        if (!super.equals(o)) return false;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(eventTypeLogoFileName, that.eventTypeLogoFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, eventTypeLogoFileName);
    }
}
