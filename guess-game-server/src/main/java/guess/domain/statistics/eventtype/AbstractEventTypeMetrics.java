package guess.domain.statistics.eventtype;

import guess.domain.statistics.EventTypeEventMetrics;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract event type metrics.
 */
public abstract class AbstractEventTypeMetrics extends EventTypeEventMetrics {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long age;
    private final long duration;
    private final long eventsQuantity;

    protected AbstractEventTypeMetrics(LocalDate startDate, LocalDate endDate, long age, long duration, long eventsQuantity,
                                       EventTypeEventMetrics metrics) {
        super(metrics.getTalksQuantity(), metrics.getSpeakersQuantity(), metrics.getCompaniesQuantity(),
                metrics.getJavaChampionsQuantity(), metrics.getMvpsQuantity());

        this.startDate = startDate;
        this.endDate = endDate;
        this.age = age;
        this.duration = duration;
        this.eventsQuantity = eventsQuantity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEventTypeMetrics)) return false;
        if (!super.equals(o)) return false;
        AbstractEventTypeMetrics that = (AbstractEventTypeMetrics) o;
        return age == that.age &&
                duration == that.duration &&
                eventsQuantity == that.eventsQuantity &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, endDate, age, duration, eventsQuantity);
    }

    @Override
    public String toString() {
        return "AbstractEventTypeMetrics{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", age=" + age +
                ", duration=" + duration +
                ", eventsQuantity=" + eventsQuantity +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
