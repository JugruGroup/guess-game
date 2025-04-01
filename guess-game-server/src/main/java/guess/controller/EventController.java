package guess.controller;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventPart;
import guess.domain.source.Speaker;
import guess.domain.source.Talk;
import guess.dto.company.CompanyBriefDto;
import guess.dto.event.*;
import guess.dto.speaker.SpeakerBriefDto;
import guess.dto.talk.TalkBriefDto;
import guess.service.EventService;
import guess.service.EventTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Event controller.
 */
@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;
    private final EventTypeService eventTypeService;

    @Autowired
    public EventController(EventService eventService, EventTypeService eventTypeService) {
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    @GetMapping("/events")
    public List<EventBriefDto> getEvents(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                         @RequestParam(required = false) Long organizerId,
                                         @RequestParam(required = false) Long eventTypeId,
                                         @RequestParam String language) {
        List<Event> events = eventService.getEvents(conferences, meetups, organizerId, eventTypeId);

        List<Event> sortedEvents = events.stream()
                .sorted(Comparator.comparing(Event::getFirstStartDate).reversed())
                .toList();

        return EventBriefDto.convertToBriefDto(sortedEvents, Language.getLanguageByCode(language));
    }

    @GetMapping("/event-parts")
    public List<EventPartBriefDto> getEventParts(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                 @RequestParam(required = false) Long organizerId,
                                                 @RequestParam(required = false) Long eventTypeId,
                                                 @RequestParam String language) {
        List<Event> events = eventService.getEvents(conferences, meetups, organizerId, eventTypeId);
        List<EventPart> eventParts = eventService.convertEventsToEventParts(events);

        return EventPartBriefDto.convertToBriefDto(eventParts, Language.getLanguageByCode(language)).stream()
                .sorted(Comparator.comparing(EventPartBriefDto::getStartDate).reversed())
                .toList();
    }

    @GetMapping("/default-event")
    public EventSuperBriefDto getDefaultEvent(@RequestParam String language) {
        var defaultEvent = eventService.getDefaultEvent(true, true);

        return (defaultEvent != null) ? EventSuperBriefDto.convertToSuperBriefDto(defaultEvent, Language.getLanguageByCode(language)) : null;
    }

    @GetMapping("/default-event-part-home-info")
    public EventPartHomeInfoDto getDefaultEventPartHomeInfo(@RequestParam String language) {
        var defaultEventPart = eventService.getDefaultEventPart(true, true);

        return (defaultEventPart != null) ?
                EventPartHomeInfoDto.convertToDto(defaultEventPart, Language.getLanguageByCode(language)) : null;
    }

    @GetMapping("/event/{id}")
    public EventDetailsDto getEvent(@PathVariable long id, @RequestParam String language) {
        var event = eventService.getEventById(id);
        List<Talk> talks = event.getTalks();
        List<Speaker> speakers = talks.stream()
                .flatMap(t -> t.getSpeakers().stream())
                .distinct()
                .toList();
        var eventDetailsDto = EventDetailsDto.convertToDto(event, speakers, talks, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, Language.getLanguageByCode(language));

        Comparator<SpeakerBriefDto> comparatorByName = Comparator.comparing(SpeakerBriefDto::getDisplayName, String.CASE_INSENSITIVE_ORDER);
        Comparator<SpeakerBriefDto> comparatorByCompany = Comparator.comparing(
                s -> s.getCompanies().stream()
                        .map(CompanyBriefDto::getName)
                        .collect(Collectors.joining(", ")), String.CASE_INSENSITIVE_ORDER);
        List<SpeakerBriefDto> sortedSpeakers = eventDetailsDto.speakers().stream()
                .sorted(comparatorByName.thenComparing(comparatorByCompany))
                .toList();

        Comparator<TalkBriefDto> comparatorByTalkDate = Comparator.nullsLast(
                Comparator.comparing(
                        TalkBriefDto::getTalkDay,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        Comparator<TalkBriefDto> comparatorByTalkStartTime = Comparator.nullsLast(
                Comparator.comparing(
                        TalkBriefDto::getTalkStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        Comparator<TalkBriefDto> comparatorByTalkEndTime = Comparator.nullsLast(
                Comparator.comparing(
                        TalkBriefDto::getTalkEndTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        Comparator<TalkBriefDto> comparatorByTrack = Comparator.nullsLast(
                Comparator.comparing(
                        TalkBriefDto::getTrack,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        List<TalkBriefDto> sortedTalks = eventDetailsDto.talks().stream()
                .sorted(comparatorByTalkDate.thenComparing(comparatorByTalkStartTime).thenComparing(comparatorByTrack).thenComparing(comparatorByTalkEndTime))
                .toList();

        return new EventDetailsDto(eventDetailsDto.event(), sortedSpeakers, sortedTalks);
    }
}
