package guess.controller;

import guess.dao.exception.QuestionSetNotExistsException;
import guess.domain.GuessMode;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.dto.event.EventSuperBriefDto;
import guess.dto.eventtype.EventTypeSuperBriefDto;
import guess.service.EventTypeService;
import guess.service.LocaleService;
import guess.service.QuestionService;
import guess.util.LocalizationUtils;
import jakarta.servlet.http.HttpSession;
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
    private final LocaleService localeService;

    @Autowired
    public QuestionController(QuestionService questionService, EventTypeService eventTypeService, LocaleService localeService) {
        this.questionService = questionService;
        this.eventTypeService = eventTypeService;
        this.localeService = localeService;
    }

    @GetMapping("/event-types")
    public List<EventTypeSuperBriefDto> getEventTypes(HttpSession httpSession) {
        List<EventType> eventTypes = eventTypeService.getEventTypes();
        var language = localeService.getLanguage(httpSession);
        Comparator<EventType> comparatorByIsConference = Comparator.comparing(EventType::isEventTypeConference).reversed();
        Comparator<EventType> comparatorByInactive = Comparator.comparing(EventType::isInactive);
        Comparator<EventType> comparatorByName = Comparator.comparing(et -> LocalizationUtils.getString(et.getName(), language), String.CASE_INSENSITIVE_ORDER);

        List<EventType> sortedEventTypes = eventTypes.stream()
                .sorted(comparatorByIsConference.thenComparing(comparatorByInactive).thenComparing(comparatorByName))
                .toList();

        return EventTypeSuperBriefDto.convertToSuperBriefDto(sortedEventTypes, language);
    }

    @GetMapping("/events")
    public List<EventSuperBriefDto> getEvents(@RequestParam List<Long> eventTypeIds, HttpSession httpSession) {
        List<Event> events = questionService.getEvents(eventTypeIds);
        var language = localeService.getLanguage(httpSession);

        List<Event> sortedEvents = events.stream()
                .sorted(Comparator.comparing(Event::getFirstStartDate).reversed())
                .toList();

        return EventSuperBriefDto.convertToSuperBriefDto(sortedEvents, language);
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
