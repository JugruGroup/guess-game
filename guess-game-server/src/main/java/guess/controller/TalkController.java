package guess.controller;

import guess.domain.source.Talk;
import guess.dto.talk.TalkSuperBriefDto;
import guess.dto.talk.TalkDetailsDto;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.service.LocaleService;
import guess.service.TalkService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * Talk controller.
 */
@RestController
@RequestMapping("/api/talk")
public class TalkController {
    private final TalkService talkService;
    private final EventService eventService;
    private final EventTypeService eventTypeService;
    private final LocaleService localeService;

    @Autowired
    public TalkController(TalkService talkService, EventService eventService, EventTypeService eventTypeService, LocaleService localeService) {
        this.talkService = talkService;
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
        this.localeService = localeService;
    }

    @GetMapping("/talks")
    public List<TalkSuperBriefDto> getTalks(@RequestParam(required = false) Long eventTypeId,
                                            @RequestParam(required = false) Long eventId,
                                            @RequestParam(required = false) String talkName,
                                            @RequestParam(required = false) String speakerName,
                                            HttpSession httpSession) {
        List<Talk> talks = talkService.getTalks(eventTypeId, eventId, talkName, speakerName);
        var language = localeService.getLanguage(httpSession);
        List<TalkSuperBriefDto> talkSuperBriefDtoList = TalkSuperBriefDto.convertToSuperBriefDto(talks, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, language);

        Comparator<TalkSuperBriefDto> comparatorByEventName = Comparator.comparing(t -> t.getEvent().getName());
        Comparator<TalkSuperBriefDto> comparatorByName = Comparator.comparing(TalkSuperBriefDto::getName);

        return talkSuperBriefDtoList.stream()
                .sorted(comparatorByEventName.thenComparing(comparatorByName))
                .toList();
    }

    @GetMapping("/talk/{id}")
    public TalkDetailsDto getTalk(@PathVariable long id, HttpSession httpSession) {
        var talk = talkService.getTalkById(id);
        var language = localeService.getLanguage(httpSession);

        return TalkDetailsDto.convertToDto(talk, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, language);
    }
}
