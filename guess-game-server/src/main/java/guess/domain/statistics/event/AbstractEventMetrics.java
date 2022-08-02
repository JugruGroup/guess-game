package guess.domain.statistics.event;

import guess.domain.statistics.EventTypeEventMetrics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract event metrics.
 */
public abstract class AbstractEventMetrics extends EventTypeEventMetrics {
    private final LocalDate startDate;
    private final long duration;

    protected AbstractEventMetrics(LocalDate startDate, long duration, EventTypeEventMetrics metrics) {
        super(metrics.getTalksQuantity(), metrics.getSpeakersQuantity(), metrics.getCompaniesQuantity(),
                metrics.getJavaChampionsQuantity(), metrics.getMvpsQuantity());

        this.startDate = startDate;
        this.duration = duration;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEventMetrics)) return false;
        if (!super.equals(o)) return false;
        AbstractEventMetrics that = (AbstractEventMetrics) o;
        return duration == that.duration &&
                Objects.equals(startDate, that.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, duration);
    }

    @Override
    public String toString() {
        return "AbstractEventMetrics{" +
                "startDate=" + startDate +
                ", duration=" + duration +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
