package guess.controller;

import guess.dao.exception.QuestionSetNotExistsException;
import guess.domain.GuessMode;
import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.dto.event.EventSuperBriefDto;
import guess.dto.eventtype.EventTypeSuperBriefDto;
import guess.service.EventTypeService;
import guess.service.QuestionService;
import guess.util.LocalizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Question controller.
 */
@RestController
@RequestMapping("/api/question")
public class QuestionController {
    private final QuestionService questionService;
    private final EventTypeService eventTypeService;

    @Autowired
    public QuestionController(QuestionService questionService, EventTypeService eventTypeService) {
        this.questionService = questionService;
        this.eventTypeService = eventTypeService;
    }

    @GetMapping("/event-types")
    public List<EventTypeSuperBriefDto> getEventTypes(@RequestParam String language) {
        List<EventType> eventTypes = eventTypeService.getEventTypes();
        var languageEnum = Language.getLanguageByCode(language);
        Comparator<EventType> comparatorByIsConference = Comparator.comparing(EventType::isEventTypeConference).reversed();
        Comparator<EventType> comparatorByInactive = Comparator.comparing(EventType::isInactive);
        Comparator<EventType> comparatorByName = Comparator.comparing(et ->
                LocalizationUtils.getString(et.getName(), languageEnum), String.CASE_INSENSITIVE_ORDER);

        List<EventType> sortedEventTypes = eventTypes.stream()
                .sorted(comparatorByIsConference.thenComparing(comparatorByInactive).thenComparing(comparatorByName))
                .toList();

        return EventTypeSuperBriefDto.convertToSuperBriefDto(sortedEventTypes, languageEnum);
    }

    @GetMapping("/events")
    public List<EventSuperBriefDto> getEvents(@RequestParam List<Long> eventTypeIds, @RequestParam String language) {
        List<Event> events = questionService.getEvents(eventTypeIds);

        List<Event> sortedEvents = events.stream()
                .sorted(Comparator.comparing(Event::getFirstStartDate).reversed())
                .toList();

        return EventSuperBriefDto.convertToSuperBriefDto(sortedEvents, Language.getLanguageByCode(language));
    }

    @GetMapping("/quantities")
    public List<Integer> getQuantities(@RequestParam List<Long> eventTypeIds, @RequestParam List<Long> eventIds,
                                       @RequestParam String guessMode) throws QuestionSetNotExistsException {
        List<Integer> quantities = questionService.getQuantities(eventTypeIds, eventIds, GuessMode.valueOf(guessMode));

        return quantities.stream()
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
