package guess.domain.source;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Event.
 */
public class Event extends AbstractEvent {
    private List<EventDays> days;

    public Event() {
    }

    public Event(Nameable nameable, EventType eventType, List<EventDays> days, EventLinks links, String timeZone,
                 List<Talk> talks) {
        super(nameable, eventType, links, timeZone, talks);

        this.days = days;
    }

    public List<EventDays> getDays() {
        return days;
    }

    public void setDays(List<EventDays> days) {
        this.days = days;
    }

    public LocalDate getFirstStartDate() {
        if ((days != null) && !days.isEmpty()) {
            return days.get(0).getStartDate();
        } else {
            return null;
        }
    }

    public LocalDate getLastEndDate() {
        if ((days != null) && !days.isEmpty()) {
            return days.get(days.size() - 1).getEndDate();
        } else {
            return null;
        }
    }

    public long getDuration() {
        return days.stream()
                .mapToLong(ed -> ChronoUnit.DAYS.between(ed.getStartDate(), ed.getEndDate()) + 1)
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + getId() +
                ", name=" + getName() +
                ", talks=" + getTalks() +
                '}';
    }
}
