package guess.domain.statistics.eventtype;

import guess.domain.statistics.Metrics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract event type metrics.
 */
public abstract class AbstractEventTypeMetrics extends Metrics {
    private final LocalDate startDate;
    private final long age;
    private final long duration;
    private final long eventsQuantity;
    private final long speakersQuantity;
    private final long companiesQuantity;

    protected AbstractEventTypeMetrics(LocalDate startDate, long age, long duration, long eventsQuantity,
                                       long speakersQuantity, long companiesQuantity, Metrics metrics) {
        super(metrics.getTalksQuantity(), metrics.getJavaChampionsQuantity(), metrics.getMvpsQuantity());

        this.startDate = startDate;
        this.age = age;
        this.duration = duration;
        this.eventsQuantity = eventsQuantity;
        this.speakersQuantity = speakersQuantity;
        this.companiesQuantity = companiesQuantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public long getAge() {
        return age;
    }

    public long getDuration() {
        return duration;
    }

    public long getEventsQuantity() {
        return eventsQuantity;
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
        if (!(o instanceof AbstractEventTypeMetrics)) return false;
        if (!super.equals(o)) return false;
        AbstractEventTypeMetrics that = (AbstractEventTypeMetrics) o;
        return age == that.age &&
                duration == that.duration &&
                eventsQuantity == that.eventsQuantity &&
                speakersQuantity == that.speakersQuantity &&
                companiesQuantity == that.companiesQuantity &&
                Objects.equals(startDate, that.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, age, duration, eventsQuantity, speakersQuantity, companiesQuantity);
    }

    @Override
    public String toString() {
        return "AbstractEventTypeMetrics{" +
                "startDate=" + startDate +
                ", age=" + age +
                ", duration=" + duration +
                ", eventsQuantity=" + eventsQuantity +
                ", speakersQuantity=" + speakersQuantity +
                ", companiesQuantity=" + companiesQuantity +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
