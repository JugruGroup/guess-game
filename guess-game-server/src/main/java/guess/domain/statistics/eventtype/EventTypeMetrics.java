package guess.domain.statistics.eventtype;

import guess.domain.source.EventType;
import guess.domain.statistics.EventTypeEventMetrics;

import java.util.Objects;

/**
 * Event type metrics.
 */
public class EventTypeMetrics extends AbstractEventTypeMetrics {
    private final EventType eventType;

    public EventTypeMetrics(EventType eventType, long age, long eventsQuantity, EventTypeEventMetrics metrics) {
        super(age, eventsQuantity, metrics);

        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTypeMetrics that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(eventType, that.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), eventType);
    }

    @Override
    public String toString() {
        return "EventTypeMetrics{" +
                "eventType=" + eventType +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", age=" + getAge() +
                ", duration=" + getDuration() +
                ", eventsQuantity=" + getEventsQuantity() +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
