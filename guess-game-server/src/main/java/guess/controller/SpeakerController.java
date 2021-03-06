package guess.controller;

import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.domain.source.Talk;
import guess.dto.company.CompanyDto;
import guess.dto.speaker.SpeakerBriefDto;
import guess.dto.speaker.SpeakerDetailsDto;
import guess.dto.talk.TalkBriefDto;
import guess.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Speaker controller.
 */
@Controller
@RequestMapping("/api/speaker")
public class SpeakerController {
    private final SpeakerService speakerService;
    private final TalkService talkService;
    private final EventService eventService;
    private final EventTypeService eventTypeService;
    private final LocaleService localeService;

    @Autowired
    public SpeakerController(SpeakerService speakerService, TalkService talkService, EventService eventService,
                             EventTypeService eventTypeService, LocaleService localeService) {
        this.speakerService = speakerService;
        this.talkService = talkService;
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
        this.localeService = localeService;
    }

    @GetMapping("/first-letter-speakers")
    @ResponseBody
    public List<SpeakerBriefDto> getSpeakersByFirstLetter(@RequestParam String firstLetter, HttpSession httpSession) {
        Language language = localeService.getLanguage(httpSession);
        List<Speaker> speakers = speakerService.getSpeakersByFirstLetter(firstLetter, language);

        return convertToBriefDtoAndSort(speakers, language);
    }

    @GetMapping("/speakers")
    @ResponseBody
    public List<SpeakerBriefDto> getSpeakers(@RequestParam(required = false) String name, @RequestParam(required = false) String company,
                                             @RequestParam(required = false) String twitter, @RequestParam(required = false) String gitHub,
                                             @RequestParam boolean javaChampion, @RequestParam boolean mvp,
                                             HttpSession httpSession) {
        Language language = localeService.getLanguage(httpSession);
        List<Speaker> speakers = speakerService.getSpeakers(name, company, twitter, gitHub, javaChampion, mvp);

        return convertToBriefDtoAndSort(speakers, language);
    }

    List<SpeakerBriefDto> convertToBriefDtoAndSort(List<Speaker> speakers, Language language) {
        List<SpeakerBriefDto> speakerBriefDtoList = SpeakerBriefDto.convertToBriefDto(speakers, language);

        Comparator<SpeakerBriefDto> comparatorByName = Comparator.comparing(SpeakerBriefDto::getDisplayName, String.CASE_INSENSITIVE_ORDER);
        Comparator<SpeakerBriefDto> comparatorByCompany = Comparator.comparing(
                s -> s.getCompanies().stream()
                        .map(CompanyDto::getName)
                        .collect(Collectors.joining(", ")), String.CASE_INSENSITIVE_ORDER);

        speakerBriefDtoList.sort(comparatorByName.thenComparing(comparatorByCompany));

        return speakerBriefDtoList;
    }

    @GetMapping("/speaker/{id}")
    @ResponseBody
    public SpeakerDetailsDto getSpeaker(@PathVariable long id, HttpSession httpSession) {
        Speaker speaker = speakerService.getSpeakerById(id);
        List<Talk> talks = talkService.getTalksBySpeaker(speaker);
        Language language = localeService.getLanguage(httpSession);
        SpeakerDetailsDto speakerDetailsDto = SpeakerDetailsDto.convertToDto(speaker, talks, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, language);

        speakerDetailsDto.getTalks().sort(Comparator.comparing(TalkBriefDto::getTalkDate).reversed());

        return speakerDetailsDto;
    }
}
