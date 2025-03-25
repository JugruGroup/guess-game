package guess.domain.statistics.eventplace;

import guess.domain.statistics.EventTypeEventMetrics;

import java.util.Objects;

/**
 * Abstract event place metrics.
 */
public class AbstractEventPlaceMetrics extends EventTypeEventMetrics {
    private final long eventsQuantity;
    private final long eventTypesQuantity;

    public AbstractEventPlaceMetrics(long eventsQuantity, long eventTypesQuantity, EventTypeEventMetrics metrics) {
        super(metrics.getStartDate(), metrics.getEndDate(), metrics.getDuration(), metrics.getSpeakersQuantity(),
                metrics.getCompaniesQuantity(), metrics);

        this.eventsQuantity = eventsQuantity;
        this.eventTypesQuantity = eventTypesQuantity;
    }

    public long getEventsQuantity() {
        return eventsQuantity;
    }

    public long getEventTypesQuantity() {
        return eventTypesQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEventPlaceMetrics that)) return false;
        if (!super.equals(o)) return false;
        return getEventsQuantity() == that.getEventsQuantity() && getEventTypesQuantity() == that.getEventTypesQuantity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getEventsQuantity(), getEventTypesQuantity());
    }
}
