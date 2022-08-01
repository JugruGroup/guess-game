package guess.domain.statistics.event;

import guess.domain.source.Event;
import guess.domain.statistics.EventTypeEventMetrics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Event metrics.
 */
public class EventMetrics extends AbstractEventMetrics {
    private final Event event;

    public EventMetrics(Event event, LocalDate startDate, long duration, EventTypeEventMetrics metrics) {
        super(startDate, duration, metrics);

        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventMetrics)) return false;
        if (!super.equals(o)) return false;
        EventMetrics that = (EventMetrics) o;
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
                "startDate=" + getStartDate() +
                ", duration=" + getDuration() +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
