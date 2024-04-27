package guess.domain.statistics.event;

import guess.domain.statistics.EventTypeEventMetrics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract event metrics.
 */
public abstract class AbstractEventMetrics extends EventTypeEventMetrics {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long duration;

    protected AbstractEventMetrics(LocalDate startDate, LocalDate endDate, long duration, EventTypeEventMetrics metrics) {
        super(metrics.getTalksQuantity(), metrics.getSpeakersQuantity(), metrics.getCompaniesQuantity(),
                metrics.getJavaChampionsQuantity(), metrics.getMvpsQuantity());

        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEventMetrics)) return false;
        if (!super.equals(o)) return false;
        AbstractEventMetrics that = (AbstractEventMetrics) o;
        return duration == that.duration &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, endDate, duration);
    }

    @Override
    public String toString() {
        return "AbstractEventMetrics{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", duration=" + duration +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
