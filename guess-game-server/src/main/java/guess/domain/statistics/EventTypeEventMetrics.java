package guess.domain.statistics;

import java.util.Objects;

/**
 * Event type and event metrics.
 */
public class EventTypeEventMetrics extends Metrics {
    private final long speakersQuantity;
    private final long companiesQuantity;

    public EventTypeEventMetrics(long talksQuantity, long speakersQuantity, long companiesQuantity,
                                 long javaChampionsQuantity, long mvpsQuantity) {
        super(talksQuantity, javaChampionsQuantity, mvpsQuantity);
        
        this.speakersQuantity = speakersQuantity;
        this.companiesQuantity = companiesQuantity;
    }

    public long getSpeakersQuantity() {
        return speakersQuantity;
    }

    public long getCompaniesQuantity() {
        return companiesQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTypeEventMetrics that)) return false;
        if (!super.equals(o)) return false;
        return getSpeakersQuantity() == that.getSpeakersQuantity() && getCompaniesQuantity() == that.getCompaniesQuantity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSpeakersQuantity(), getCompaniesQuantity());
    }
}
