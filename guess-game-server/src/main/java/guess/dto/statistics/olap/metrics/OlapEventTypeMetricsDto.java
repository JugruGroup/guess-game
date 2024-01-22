package guess.dto.statistics.olap.metrics;

import guess.domain.Language;
import guess.domain.source.EventType;
import guess.domain.statistics.olap.OlapEntityMetrics;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;

/**
 * OLAP event type metrics DTO.
 */
public class OlapEventTypeMetricsDto extends OlapEntityMetricsDto {
    private record MeasureValuesLists(List<Long> measureValues, List<Long> cumulativeMeasureValues) {
    }

    private final boolean conference;
    private final String logoFileName;
    private final String organizerName;

    public OlapEventTypeMetricsDto(long id, String name, boolean conference, String logoFileName,
                                   String organizerName, MeasureValuesLists measureValuesLists, Long total) {
        super(id, name, measureValuesLists.measureValues, measureValuesLists.cumulativeMeasureValues, total);

        this.conference = conference;
        this.logoFileName = logoFileName;
        this.organizerName = organizerName;
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

    public static OlapEventTypeMetricsDto convertToDto(OlapEntityMetrics<EventType> eventTypeMetrics, Language language) {
        var eventType = eventTypeMetrics.entity();
        var name = LocalizationUtils.getString(eventType.getName(), language);
        String organizerName = (eventType.getOrganizer() != null) ? LocalizationUtils.getString(eventType.getOrganizer().getName(), language) : null;

        return new OlapEventTypeMetricsDto(
                eventType.getId(),
                name,
                eventType.isEventTypeConference(),
                eventType.getLogoFileName(),
                organizerName,
                new MeasureValuesLists(eventTypeMetrics.measureValues(), eventTypeMetrics.cumulativeMeasureValues()),
                eventTypeMetrics.total());
    }

    public static List<OlapEventTypeMetricsDto> convertToDto(List<OlapEntityMetrics<EventType>> eventTypeMetricsList, Language language) {
        return eventTypeMetricsList.stream()
                .map(etm -> convertToDto(etm, language))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OlapEventTypeMetricsDto that)) return false;
        if (!super.equals(o)) return false;
        return isConference() == that.isConference() && Objects.equals(getLogoFileName(), that.getLogoFileName()) && Objects.equals(getOrganizerName(), that.getOrganizerName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isConference(), getLogoFileName(), getOrganizerName());
    }

    @Override
    public String toString() {
        return "OlapEventTypeMetricsDto{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", conference=" + conference +
                ", logoFileName='" + logoFileName + '\'' +
                ", organizerName='" + organizerName + '\'' +
                '}';
    }
}
