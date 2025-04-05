package guess.domain.statistics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Event type and event metrics.
 */
public class EventTypeEventMetrics extends Metrics {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long duration;
    private final long speakersQuantity;
    private final long companiesQuantity;

    public EventTypeEventMetrics(LocalDate startDate, LocalDate endDate, long duration, long speakersQuantity,
                                 long companiesQuantity, Metrics metrics) {
        super(metrics.getTalksQuantity(), metrics.getJavaChampionsQuantity(), metrics.getMvpsQuantity());

        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.speakersQuantity = speakersQuantity;
        this.companiesQuantity = companiesQuantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getDuration() {
        return duration;
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
        return duration == that.duration &&
                speakersQuantity == that.getSpeakersQuantity() &&
                companiesQuantity == that.getCompaniesQuantity() &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, endDate, duration, speakersQuantity, companiesQuantity);
    }
}
