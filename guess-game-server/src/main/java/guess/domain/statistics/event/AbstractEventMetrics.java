package guess.domain.statistics.event;

import guess.domain.statistics.Metrics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract event metrics.
 */
public abstract class AbstractEventMetrics extends Metrics {
    private final LocalDate startDate;
    private final long duration;
    private final long speakersQuantity;
    private final long companiesQuantity;

    protected AbstractEventMetrics(LocalDate startDate, long duration, long talksQuantity, long speakersQuantity,
                                   long companiesQuantity, long javaChampionsQuantity, long mvpsQuantity) {
        super(talksQuantity, javaChampionsQuantity, mvpsQuantity);

        this.startDate = startDate;
        this.duration = duration;
        this.speakersQuantity = speakersQuantity;
        this.companiesQuantity = companiesQuantity;
    }

    public LocalDate getStartDate() {
        return startDate;
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
        if (!(o instanceof AbstractEventMetrics)) return false;
        if (!super.equals(o)) return false;
        AbstractEventMetrics that = (AbstractEventMetrics) o;
        return duration == that.duration &&
                speakersQuantity == that.speakersQuantity &&
                companiesQuantity == that.companiesQuantity &&
                Objects.equals(startDate, that.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, duration, speakersQuantity, companiesQuantity);
    }

    @Override
    public String toString() {
        return "AbstractEventMetrics{" +
                "startDate=" + startDate +
                ", duration=" + duration +
                ", speakersQuantity=" + speakersQuantity +
                ", companiesQuantity=" + companiesQuantity +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
