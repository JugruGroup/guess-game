package guess.domain.statistics.eventplace;

import guess.domain.source.Place;
import guess.domain.statistics.EventTypeEventMetrics;

import java.util.Objects;

/**
 * Event place metrics.
 */
public class EventPlaceMetrics extends AbstractEventPlaceMetrics {
    private final Place place;

    public EventPlaceMetrics(Place place, long eventsQuantity, long eventTypesQuantity, EventTypeEventMetrics metrics) {
        super(eventsQuantity, eventTypesQuantity, metrics);

        this.place = place;
    }

    public Place getPlace() {
        return place;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventPlaceMetrics that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getPlace(), that.getPlace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPlace());
    }
}
