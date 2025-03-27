package guess.domain.source;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Event part.
 */
public class EventPart extends AbstractEvent {
    private LocalDate startDate;
    private LocalDate endDate;
    private Place place;

    public EventPart() {
    }

    public EventPart(Nameable nameable, EventType eventType, EventLinks links, String timeZone, List<Talk> talks,
                     EventDays eventDays) {
        super(nameable, eventType, links, timeZone, talks);

        this.startDate = eventDays.getStartDate();
        this.endDate = eventDays.getEndDate();
        this.place = eventDays.getPlace();
    }

    public EventPart(long id, EventType eventType, LocalDate startDate, LocalDate endDate) {
        setId(id);
        setEventType(eventType);

        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Place getPlace() {
        return place;
    }

    public long getDuration() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public static EventPart of(Event event, EventDays eventDays) {
        return new EventPart(
                new Nameable(
                        event.getId(),
                        event.getName()
                ),
                event.getEventType(),
                new EventLinks(
                        event.getSiteLink(),
                        event.getYoutubeLink()
                ),
                event.getTimeZone(),
                event.getTalks(),
                eventDays
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventPart eventPart)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getStartDate(), eventPart.getStartDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getStartDate());
    }

    @Override
    public String toString() {
        return "EventPart{" +
                "id=" + getId() +
                ", eventType=" + getEventType() +
                ", name=" + getName() +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", place=" + place +
                ", talks=" + getTalks() +
                '}';
    }
}
