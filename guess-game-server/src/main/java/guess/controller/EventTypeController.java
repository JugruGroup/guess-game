package guess.controller;

import guess.domain.Language;
import guess.domain.source.EventType;
import guess.dto.event.EventPartBriefDto;
import guess.dto.eventtype.EventTypeBriefDto;
import guess.dto.eventtype.EventTypeDetailsDto;
import guess.dto.eventtype.EventTypeSuperBriefDto;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.util.LocalizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * Event type controller.
 */
@RestController
@RequestMapping("/api/event-type")
public class EventTypeController {
    private final EventTypeService eventTypeService;
    private final EventService eventService;

    @Autowired
    public EventTypeController(EventTypeService eventTypeService, EventService eventService) {
        this.eventTypeService = eventTypeService;
        this.eventService = eventService;
    }

    @GetMapping("/event-types")
    public List<EventTypeBriefDto> getEventTypes(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                 @RequestParam(required = false) Long organizerId,
                                                 @RequestParam(required = false) Long topicId,
                                                 @RequestParam String language) {
        var languageEnum = Language.getLanguageByCode(language);
        List<EventType> eventTypes = getEventTypesAndSort(conferences, meetups, organizerId, topicId, languageEnum);

        return EventTypeBriefDto.convertToBriefDto(eventTypes, languageEnum);
    }

    @GetMapping("/filter-event-types")
    public List<EventTypeSuperBriefDto> getFilterEventTypes(@RequestParam boolean conferences, @RequestParam boolean meetups,
                                                            @RequestParam(required = false) Long organizerId,
                                                            @RequestParam String language) {
        var languageEnum = Language.getLanguageByCode(language);
        List<EventType> eventTypes = getEventTypesAndSort(conferences, meetups, organizerId, null, languageEnum);

        return EventTypeSuperBriefDto.convertToSuperBriefDto(eventTypes, languageEnum);
    }

    List<EventType> getEventTypesAndSort(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId, Language language) {
        List<EventType> eventTypes = eventTypeService.getEventTypes(isConferences, isMeetups, organizerId, topicId);
        Comparator<EventType> comparatorByIsConference = Comparator.comparing(EventType::isEventTypeConference).reversed();
        Comparator<EventType> comparatorByOrganizerName = Comparator.comparing(et -> LocalizationUtils.getString(et.getOrganizer().getName(), language), String.CASE_INSENSITIVE_ORDER);
        Comparator<EventType> comparatorByName = Comparator.comparing(et -> LocalizationUtils.getString(et.getName(), language), String.CASE_INSENSITIVE_ORDER);

        return eventTypes.stream()
                .sorted(comparatorByIsConference.thenComparing(comparatorByOrganizerName).thenComparing(comparatorByName))
                .toList();
    }

    @GetMapping("/event-type/{id}")
    public EventTypeDetailsDto getEventType(@PathVariable long id, @RequestParam String language) {
        var eventType = eventTypeService.getEventTypeById(id);
        var eventParts = eventService.convertEventsToEventParts(eventType.getEvents());
        var eventTypeDetailsDto = EventTypeDetailsDto.convertToDto(eventType, eventParts, Language.getLanguageByCode(language));

        List<EventPartBriefDto> sortedEvents = eventTypeDetailsDto.eventParts().stream()
                .sorted(Comparator.comparing(EventPartBriefDto::getStartDate).reversed())
                .toList();

        return new EventTypeDetailsDto(eventTypeDetailsDto.eventType(), sortedEvents);
    }
}
