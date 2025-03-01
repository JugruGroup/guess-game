package guess.controller;

import guess.domain.Language;
import guess.domain.source.Organizer;
import guess.dto.organizer.OrganizerDto;
import guess.service.EventService;
import guess.service.OrganizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Organizer controller.
 */
@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {
    private final OrganizerService organizerService;
    private final EventService eventService;

    @Autowired
    public OrganizerController(OrganizerService organizerService, EventService eventService) {
        this.organizerService = organizerService;
        this.eventService = eventService;
    }

    @GetMapping("/organizers")
    public List<OrganizerDto> getOrganizers(@RequestParam String language) {
        List<Organizer> organizers = organizerService.getOrganizers();
        List<OrganizerDto> organizerDtoList = OrganizerDto.convertToDto(organizers, Language.getLanguageByCode(language));

        return organizerDtoList.stream()
                .sorted(Comparator.comparing(OrganizerDto::name))
                .toList();
    }

    @GetMapping("/default-event-organizer")
    public OrganizerDto getDefaultEventOrganizer(@RequestParam String language) {
        var defaultEvent = eventService.getDefaultEvent(true, true);

        if (defaultEvent != null) {
            return OrganizerDto.convertToDto(defaultEvent.getEventType().getOrganizer(), Language.getLanguageByCode(language));
        } else {
            return null;
        }
    }
}
