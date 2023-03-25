package guess.service;

import guess.dao.EventDao;
import guess.dao.EventTypeDao;
import guess.domain.auxiliary.EventDateMinStartTime;
import guess.domain.auxiliary.EventMinTrackTimeEndDayTime;
import guess.domain.source.Event;
import guess.domain.source.EventDays;
import guess.domain.source.EventPart;
import guess.domain.source.Talk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Event service implementation.
 */
@Service
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final EventTypeDao eventTypeDao;

    @Autowired
    public EventServiceImpl(EventDao eventDao, EventTypeDao eventTypeDao) {
        this.eventDao = eventDao;
        this.eventTypeDao = eventTypeDao;
    }

    @Override
    public Event getEventById(long id) {
        return eventDao.getEventById(id);
    }

    @Override
    public List<Event> getEvents(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId) {
        return eventTypeDao.getEventTypes().stream()
                .filter(et -> ((isConferences && et.isEventTypeConference()) || (isMeetups && !et.isEventTypeConference())) &&
                        ((organizerId == null) || (et.getOrganizer().getId() == organizerId)) &&
                        ((eventTypeId == null) || (et.getId() == eventTypeId)))
                .flatMap(et -> et.getEvents().stream())
                .toList();
    }

    @Override
    public Event getDefaultEvent(boolean isConferences, boolean isMeetups) {
        return getDefaultEvent(isConferences, isMeetups, LocalDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public EventPart getDefaultEventPart(boolean isConferences, boolean isMeetups) {
        return getDefaultEventPart(isConferences, isMeetups, LocalDateTime.now(ZoneId.of("UTC")));
    }

    List<Event> getEventsFromDateTime(boolean isConferences, boolean isMeetups, LocalDateTime dateTime) {
        // Find current and future events
        List<Event> eventsFromDate = eventDao.getEventsFromDateTime(dateTime);

        // Use conferences and meetups flags
        return eventsFromDate.stream()
                .filter(e -> ((isConferences && e.getEventType().isEventTypeConference()) || (isMeetups && !e.getEventType().isEventTypeConference())))
                .toList();
    }

    EventPart getDefaultEventPart(boolean isConferences, boolean isMeetups, LocalDateTime dateTime) {
        return getEventPartFromEvent(getDefaultEvent(isConferences, isMeetups, dateTime), dateTime);
    }

    EventPart getEventPartFromEvent(Event event, LocalDateTime dateTime) {
        if (event == null) {
            return null;
        } else {
            if (event.getDays() == null) {
                return null;
            } else {
                List<EventDays> eventDaysFromDateTime = event.getDays().stream()
                        .filter(ed -> {
                            ZonedDateTime zonedEndDateTime = ZonedDateTime.of(
                                    ed.getEndDate(),
                                    LocalTime.of(0, 0, 0),
                                    event.getFinalTimeZoneId());
                            ZonedDateTime zonedNextDayEndDateTime = zonedEndDateTime.plus(1, ChronoUnit.DAYS);
                            LocalDateTime eventUtcEndLocalDateTime = zonedNextDayEndDateTime
                                    .withZoneSameInstant(ZoneId.of("UTC"))
                                    .toLocalDateTime();

                            return dateTime.isBefore(eventUtcEndLocalDateTime);
                        })
                        .toList();

                if (!eventDaysFromDateTime.isEmpty()) {
                    EventDays eventDays = eventDaysFromDateTime.get(0);

                    return EventPart.of(event, eventDays);
                } else {
                    return null;
                }
            }
        }
    }

    Event getDefaultEvent(boolean isConferences, boolean isMeetups, LocalDateTime dateTime) {
        // Find current and future conferences
        List<Event> conferencesFromDate = getEventsFromDateTime(isConferences, isMeetups, dateTime);
        if (conferencesFromDate.isEmpty()) {
            // Conferences not exist
            return null;
        }

        // Find (event, date, minimal track time) items
        List<EventDateMinStartTime> eventDateMinStartTimeList = getEventDateMinTrackTimeList(conferencesFromDate);
        if (eventDateMinStartTimeList.isEmpty()) {
            return null;
        }

        //Transform to (event, minimal track time, end date time) items
        List<EventMinTrackTimeEndDayTime> eventMinTrackTimeEndDayTimeList = getEventMinTrackTimeEndDayTimeList(eventDateMinStartTimeList);
        if (eventMinTrackTimeEndDayTimeList.isEmpty()) {
            return null;
        }

        // Find current and future event days, sort by minimal track date time and end day date time
        List<EventMinTrackTimeEndDayTime> eventMinTrackTimeEndDayTimeListFromDateOrdered = eventMinTrackTimeEndDayTimeList.stream()
                .filter(edt -> dateTime.isBefore(edt.endDayDateTime()))
                .sorted(Comparator.comparing(EventMinTrackTimeEndDayTime::minTrackDateTime).thenComparing(EventMinTrackTimeEndDayTime::endDayDateTime))
                .toList();
        if (eventMinTrackTimeEndDayTimeListFromDateOrdered.isEmpty()) {
            return null;
        }

        // Find first date
        LocalDateTime firstDateTime = eventMinTrackTimeEndDayTimeListFromDateOrdered.get(0).minTrackDateTime();

        if (dateTime.isBefore(firstDateTime)) {
            // No current day events, return nearest first event
            return eventMinTrackTimeEndDayTimeListFromDateOrdered.get(0).event();
        } else {
            // Current day events exist, find happened time, sort by reversed minimal track date time
            List<EventMinTrackTimeEndDayTime> eventMinTrackTimeEndDayTimeListOnCurrentDate = eventMinTrackTimeEndDayTimeListFromDateOrdered.stream()
                    .filter(edt -> !dateTime.isBefore(edt.minTrackDateTime()))
                    .sorted(Comparator.comparing(EventMinTrackTimeEndDayTime::minTrackDateTime).reversed())
                    .toList();

            // Return nearest last event
            return eventMinTrackTimeEndDayTimeListOnCurrentDate.get(0).event();
        }
    }

    /**
     * Gets list of (event, date, minimal start time) items.
     *
     * @param events events
     * @return list of (event, date, minimal start time) items
     */
    List<EventDateMinStartTime> getEventDateMinTrackTimeList(List<Event> events) {
        List<EventDateMinStartTime> result = new ArrayList<>();
        Map<Event, Map<Long, Optional<LocalTime>>> minStartTimeInTalkDaysForConferences = new LinkedHashMap<>();

        // Calculate start time minimum for each day of each event
        for (Event event : events) {
            List<Talk> talks = event.getTalks();
            Map<Long, Optional<LocalTime>> minStartTimeInTalkDays = talks.stream()
                    .filter(t -> (t.getTalkDay() != null))
                    .collect(
                            Collectors.groupingBy(
                                    Talk::getTalkDay,
                                    Collectors.mapping(
                                            Talk::getStartTime,
                                            Collectors.minBy(Comparator.naturalOrder())
                                    )
                            )
                    );

            // Fill map (event, (startTime, minStartTime))
            minStartTimeInTalkDaysForConferences.put(event, minStartTimeInTalkDays);
        }

        // Transform to (event, day, minStartTime) list
        for (Map.Entry<Event, Map<Long, Optional<LocalTime>>> entry : minStartTimeInTalkDaysForConferences.entrySet()) {
            var event = entry.getKey();
            Map<Long, Optional<LocalTime>> minTrackTimeInTalkDays = entry.getValue();
            long previousDays = 0;

            for (EventDays eventDays : event.getDays()) {
                if ((eventDays.getStartDate() != null) && (eventDays.getEndDate() != null) && (!eventDays.getStartDate().isAfter(eventDays.getEndDate()))) {
                    long days = ChronoUnit.DAYS.between(eventDays.getStartDate(), eventDays.getEndDate()) + 1;

                    iteratesDays(days, eventDays, previousDays, minTrackTimeInTalkDays, result, event);

                    previousDays += days;
                }
            }
        }

        return result;
    }

    void iteratesDays(long days, EventDays eventDays, long previousDays, Map<Long, Optional<LocalTime>> minTrackTimeInTalkDays,
                      List<EventDateMinStartTime> result, Event event) {
        for (long i = 1; i <= days; i++) {
            LocalDate date = eventDays.getStartDate().plusDays(i - 1);
            Optional<LocalTime> localTimeOptional;
            long totalDayNumber = previousDays + i;

            if (minTrackTimeInTalkDays.containsKey(totalDayNumber)) {
                localTimeOptional = minTrackTimeInTalkDays.get(totalDayNumber);
            } else {
                localTimeOptional = Optional.empty();
            }

            var minTrackTime = localTimeOptional.orElse(LocalTime.of(0, 0));

            result.add(new EventDateMinStartTime(event, date, minTrackTime));
        }
    }

    /**
     * Gets list of (event, minimal track time, end date time) items.
     *
     * @param eventDateMinStartTimeList list of (event, date, minimal track time) items
     * @return list of (event, minimal track time, end date time) items
     */
    List<EventMinTrackTimeEndDayTime> getEventMinTrackTimeEndDayTimeList(List<EventDateMinStartTime> eventDateMinStartTimeList) {
        return eventDateMinStartTimeList.stream()
                .map(edt -> {
                    var minStartDateTime = ZonedDateTime.of(
                                    edt.date(),
                                    edt.minStartTime(),
                                    edt.event().getFinalTimeZoneId())
                            .withZoneSameInstant(ZoneId.of("UTC"))
                            .toLocalDateTime();
                    var endDayDateTime = ZonedDateTime.of(
                                    edt.date().plus(1, ChronoUnit.DAYS),
                                    LocalTime.of(0, 0, 0),
                                    edt.event().getFinalTimeZoneId())
                            .withZoneSameInstant(ZoneId.of("UTC"))
                            .toLocalDateTime();

                    return new EventMinTrackTimeEndDayTime(
                            edt.event(),
                            minStartDateTime,
                            endDayDateTime
                    );
                })
                .toList();
    }

    @Override
    public Event getEventByTalk(Talk talk) {
        return eventDao.getEventByTalk(talk);
    }

    @Override
    public List<EventPart> convertEventToEventParts(Event event) {
        return event.getDays().stream()
                .map(ed -> EventPart.of(event, ed))
                .toList();
    }

    @Override
    public List<EventPart> convertEventsToEventParts(List<Event> events) {
        return events.stream()
                .flatMap(e -> convertEventToEventParts(e).stream())
                .toList();
    }
}
