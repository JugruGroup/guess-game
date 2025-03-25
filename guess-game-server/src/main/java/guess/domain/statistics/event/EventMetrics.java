package guess.domain.statistics.event;

import guess.domain.source.Event;
import guess.domain.statistics.EventTypeEventMetrics;

import java.util.Objects;

/**
 * Event metrics.
 */
public class EventMetrics extends EventTypeEventMetrics {
    private final Event event;

    public EventMetrics(Event event, EventTypeEventMetrics metrics) {
        super(metrics.getStartDate(), metrics.getEndDate(), metrics.getDuration(), metrics.getSpeakersQuantity(),
                metrics.getCompaniesQuantity(), metrics);

        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventMetrics that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), event);
    }

    @Override
    public String toString() {
        return "EventMetrics{" +
                "event=" + event +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", duration=" + getDuration() +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
