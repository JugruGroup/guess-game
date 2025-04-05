package guess.domain.statistics.eventtype;

import guess.domain.statistics.EventTypeEventMetrics;

import java.util.Objects;

/**
 * Abstract event type metrics.
 */
public abstract class AbstractEventTypeMetrics extends EventTypeEventMetrics {
    private final long age;
    private final long eventsQuantity;

    protected AbstractEventTypeMetrics(long age, long eventsQuantity, EventTypeEventMetrics metrics) {
        super(metrics.getStartDate(), metrics.getEndDate(), metrics.getDuration(), metrics.getSpeakersQuantity(),
                metrics.getCompaniesQuantity(), metrics);

        this.age = age;
        this.eventsQuantity = eventsQuantity;
    }

    public long getAge() {
        return age;
    }

    public long getEventsQuantity() {
        return eventsQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEventTypeMetrics that)) return false;
        if (!super.equals(o)) return false;
        return age == that.age &&
                eventsQuantity == that.eventsQuantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), age, eventsQuantity);
    }

    @Override
    public String toString() {
        return "AbstractEventTypeMetrics{" +
                "startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", age=" + age +
                ", duration=" + getDuration() +
                ", eventsQuantity=" + eventsQuantity +
                ", speakersQuantity=" + getSpeakersQuantity() +
                ", companiesQuantity=" + getCompaniesQuantity() +
                ", talksQuantity=" + getTalksQuantity() +
                ", javaChampionsQuantity=" + getJavaChampionsQuantity() +
                ", mvpsQuantity=" + getMvpsQuantity() +
                '}';
    }
}
