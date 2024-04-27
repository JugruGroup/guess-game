package guess.dto.statistics.eventtype;

import guess.domain.Language;
import guess.domain.statistics.EventTypeEventMetrics;
import guess.domain.statistics.eventtype.AbstractEventTypeMetrics;
import guess.domain.statistics.eventtype.EventTypeMetrics;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;

/**
 * Event type metrics DTO.
 */
public class EventTypeMetricsDto extends AbstractEventTypeMetrics {
    private final long id;
    private final String displayName;
    private final boolean conference;
    private final String logoFileName;
    private final String organizerName;
    private final String topicName;

    public EventTypeMetricsDto(long id, String displayName, boolean conference, String logoFileName,
                               AbstractEventTypeMetrics eventTypeMetrics, String organizerName, String topicName) {
        super(eventTypeMetrics.getStartDate(), eventTypeMetrics.getEndDate(), eventTypeMetrics.getAge(), eventTypeMetrics.getDuration(),
                eventTypeMetrics.getEventsQuantity(),
                new EventTypeEventMetrics(eventTypeMetrics.getTalksQuantity(), eventTypeMetrics.getSpeakersQuantity(),
                        eventTypeMetrics.getCompaniesQuantity(), eventTypeMetrics.getJavaChampionsQuantity(),
                        eventTypeMetrics.getMvpsQuantity()));

        this.id = id;
        this.displayName = displayName;
        this.conference = conference;
        this.logoFileName = logoFileName;
        this.organizerName = organizerName;
        this.topicName = topicName;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isConference() {
        return conference;
    }

    public String getLogoFileName() {
        return logoFileName;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public String getTopicName() {
        return topicName;
    }

    public static EventTypeMetricsDto convertToDto(EventTypeMetrics eventTypeMetrics, Language language) {
        var eventType = eventTypeMetrics.getEventType();
        var displayName = LocalizationUtils.getString(eventType.getName(), language);
        var organizerName = (eventType.getOrganizer() != null) ? LocalizationUtils.getString(eventType.getOrganizer().getName(), language) : null;
        var topicName = (eventType.getTopic() != null) ? LocalizationUtils.getString(eventType.getTopic().getName(), language) : null;

        return new EventTypeMetricsDto(
                eventType.getId(),
                displayName,
                eventType.isEventTypeConference(),
                eventType.getLogoFileName(),
                eventTypeMetrics,
                organizerName,
                topicName);
    }

    public static List<EventTypeMetricsDto> convertToDto(List<EventTypeMetrics> eventTypeMetricsList, Language language) {
        return eventTypeMetricsList.stream()
                .map(etm -> convertToDto(etm, language))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTypeMetricsDto)) return false;
        if (!super.equals(o)) return false;
        EventTypeMetricsDto that = (EventTypeMetricsDto) o;
        return id == that.id &&
                conference == that.conference &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(logoFileName, that.logoFileName) &&
                Objects.equals(organizerName, that.organizerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, displayName, conference, logoFileName, organizerName);
    }
}
