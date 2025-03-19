package guess.controller;

import guess.domain.Language;
import guess.domain.source.Talk;
import guess.dto.talk.TalkDetailsDto;
import guess.dto.talk.TalkSuperBriefDto;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.service.TalkService;
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

    @Autowired
    public TalkController(TalkService talkService, EventService eventService, EventTypeService eventTypeService) {
        this.talkService = talkService;
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    @GetMapping("/talks")
    public List<TalkSuperBriefDto> getTalks(@RequestParam(required = false) Long eventTypeId,
                                            @RequestParam(required = false) Long eventId,
                                            @RequestParam(required = false) String talkName,
                                            @RequestParam(required = false) String speakerName,
                                            @RequestParam(required = false) Long topicId,
                                            @RequestParam(required = false) String talkLanguage,
                                            @RequestParam String language) {
        List<Talk> talks = talkService.getTalks(eventTypeId, eventId, talkName, speakerName, topicId, talkLanguage);
        List<TalkSuperBriefDto> talkSuperBriefDtoList = TalkSuperBriefDto.convertToSuperBriefDto(talks, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, Language.getLanguageByCode(language));

        Comparator<TalkSuperBriefDto> comparatorByEventName = Comparator.comparing(t -> t.getEvent().getName());
        Comparator<TalkSuperBriefDto> comparatorByName = Comparator.comparing(TalkSuperBriefDto::getName);

        return talkSuperBriefDtoList.stream()
                .sorted(comparatorByEventName.thenComparing(comparatorByName))
                .toList();
    }

    @GetMapping("/talk/{id}")
    public TalkDetailsDto getTalk(@PathVariable long id, @RequestParam String language) {
        var talk = talkService.getTalkById(id);

        return TalkDetailsDto.convertToDto(talk, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, Language.getLanguageByCode(language));
    }
}
