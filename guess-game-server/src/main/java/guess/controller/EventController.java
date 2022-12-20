package guess.controller;

import guess.domain.source.Event;
import guess.domain.source.EventPart;
import guess.domain.source.Speaker;
import guess.domain.source.Talk;
import guess.dto.company.CompanyBriefDto;
import guess.dto.event.*;
import guess.dto.speaker.SpeakerBriefDto;
import guess.dto.talk.TalkBriefDto;
import guess.dto.talk.TalkSuperBriefDto;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.service.LocaleService;
import jakarta.servlet.http.HttpSession;
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
    private final LocaleService localeService;

    @Autowired
    public EventController(EventService eventService, EventTypeService eventTypeService, LocaleService localeService) {
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
        this.localeService = localeService;
    }

    @GetMapping("/events")
    public List<EventBriefDto> getEvents(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                         @RequestParam(required = false) Long organizerId,
                                         @RequestParam(required = false) Long eventTypeId, HttpSession httpSession) {
        List<Event> events = eventService.getEvents(conferences, meetups, organizerId, eventTypeId);
        var language = localeService.getLanguage(httpSession);

        List<Event> sortedEvents = events.stream()
                .sorted(Comparator.comparing(Event::getFirstStartDate).reversed())
                .toList();

        return EventBriefDto.convertToBriefDto(sortedEvents, language);
    }

    @GetMapping("/event-parts")
    public List<EventPartBriefDto> getEventParts(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                 @RequestParam(required = false) Long organizerId,
                                                 @RequestParam(required = false) Long eventTypeId, HttpSession httpSession) {
        List<Event> events = eventService.getEvents(conferences, meetups, organizerId, eventTypeId);
        List<EventPart> eventParts = eventService.convertEventsToEventParts(events);
        var language = localeService.getLanguage(httpSession);

        return EventPartBriefDto.convertToBriefDto(eventParts, language).stream()
                .sorted(Comparator.comparing(EventPartBriefDto::getStartDate).reversed())
                .toList();
    }

    @GetMapping("/default-event")
    public EventSuperBriefDto getDefaultEvent(HttpSession httpSession) {
        var defaultEvent = eventService.getDefaultEvent(true, true);
        var language = localeService.getLanguage(httpSession);

        return (defaultEvent != null) ? EventSuperBriefDto.convertToSuperBriefDto(defaultEvent, language) : null;
    }

    @GetMapping("/default-event-part-home-info")
    public EventPartHomeInfoDto getDefaultEventPartHomeInfo(HttpSession httpSession) {
        var defaultEventPart = eventService.getDefaultEventPart(true, true);
        var language = localeService.getLanguage(httpSession);

        return (defaultEventPart != null) ? EventPartHomeInfoDto.convertToDto(defaultEventPart, language) : null;
    }

    @GetMapping("/event/{id}")
    public EventDetailsDto getEvent(@PathVariable long id, HttpSession httpSession) {
        var event = eventService.getEventById(id);
        var language = localeService.getLanguage(httpSession);
        List<Talk> talks = event.getTalks();
        List<Speaker> speakers = talks.stream()
                .flatMap(t -> t.getSpeakers().stream())
                .distinct()
                .toList();
        var eventDetailsDto = EventDetailsDto.convertToDto(event, speakers, talks, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, language);

        Comparator<SpeakerBriefDto> comparatorByName = Comparator.comparing(SpeakerBriefDto::getDisplayName, String.CASE_INSENSITIVE_ORDER);
        Comparator<SpeakerBriefDto> comparatorByCompany = Comparator.comparing(
                s -> s.getCompanies().stream()
                        .map(CompanyBriefDto::getName)
                        .collect(Collectors.joining(", ")), String.CASE_INSENSITIVE_ORDER);
        List<SpeakerBriefDto> sortedSpeakers = eventDetailsDto.speakers().stream()
                .sorted(comparatorByName.thenComparing(comparatorByCompany))
                .toList();

        Comparator<TalkSuperBriefDto> comparatorByTalkDate = Comparator.nullsLast(
                Comparator.comparing(
                        TalkSuperBriefDto::getTalkDay,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        Comparator<TalkSuperBriefDto> comparatorByTalkTime = Comparator.nullsLast(
                Comparator.comparing(
                        TalkSuperBriefDto::getTalkTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        Comparator<TalkSuperBriefDto> comparatorByTrack = Comparator.nullsLast(
                Comparator.comparing(
                        TalkSuperBriefDto::getTrack,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        List<TalkBriefDto> sortedTalks = eventDetailsDto.talks().stream()
                .sorted(comparatorByTalkDate.thenComparing(comparatorByTalkTime).thenComparing(comparatorByTrack))
                .toList();

        return new EventDetailsDto(eventDetailsDto.event(), sortedSpeakers, sortedTalks);
    }
}
