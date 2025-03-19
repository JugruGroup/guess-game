package guess.controller;

import guess.domain.Language;
import guess.domain.source.Speaker;
import guess.domain.source.Talk;
import guess.dto.common.SelectedEntitiesDto;
import guess.dto.company.CompanyBriefDto;
import guess.dto.speaker.SpeakerBriefDto;
import guess.dto.speaker.SpeakerDetailsDto;
import guess.dto.speaker.SpeakerSuperBriefDto;
import guess.dto.talk.TalkBriefDto;
import guess.service.EventService;
import guess.service.EventTypeService;
import guess.service.SpeakerService;
import guess.service.TalkService;
import guess.util.LocalizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Speaker controller.
 */
@RestController
@RequestMapping("/api/speaker")
public class SpeakerController {
    private final SpeakerService speakerService;
    private final TalkService talkService;
    private final EventService eventService;
    private final EventTypeService eventTypeService;

    @Autowired
    public SpeakerController(SpeakerService speakerService, TalkService talkService, EventService eventService,
                             EventTypeService eventTypeService) {
        this.speakerService = speakerService;
        this.talkService = talkService;
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
    }

    @GetMapping("/first-letter-speakers")
    public List<SpeakerBriefDto> getSpeakersByFirstLetter(@RequestParam String firstLetter, @RequestParam String language) {
        var languageEnum = Language.getLanguageByCode(language);
        List<Speaker> speakers = speakerService.getSpeakersByFirstLetter(firstLetter, languageEnum);

        return convertToBriefDtoAndSort(speakers, s -> SpeakerBriefDto.convertToBriefDto(s, languageEnum));
    }

    @GetMapping("/first-letters-speakers")
    public List<SpeakerSuperBriefDto> getSpeakersByFirstLetters(@RequestParam String firstLetters,
                                                                @RequestParam String language) {
        var languageEnum = Language.getLanguageByCode(language);
        List<Speaker> speakers = speakerService.getSpeakersByFirstLetters(firstLetters, languageEnum);

        return createDuplicatesAndConvertToDtoAndSort(speakers, languageEnum);
    }

    @PostMapping("/selected-speakers")
    public List<SpeakerSuperBriefDto> getSelectedSpeakers(@RequestBody SelectedEntitiesDto selectedEntities,
                                                          @RequestParam String language) {
        List<Speaker> speakers = speakerService.getSpeakerByIds(selectedEntities.getIds());

        return createDuplicatesAndConvertToDtoAndSort(speakers, Language.getLanguageByCode(language));
    }

    @GetMapping("/speakers")
    public List<SpeakerBriefDto> getSpeakers(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String company,
                                             @RequestParam(required = false) String twitter,
                                             @RequestParam(required = false) String gitHub,
                                             @RequestParam(required = false) String habr,
                                             @RequestParam(required = false) String description,
                                             @RequestParam boolean javaChampion,
                                             @RequestParam boolean mvp,
                                             @RequestParam String language) {
        List<Speaker> speakers = speakerService.getSpeakers(name, company, new Speaker.SpeakerSocials(twitter, gitHub, habr),
                description, javaChampion, mvp);

        return convertToBriefDtoAndSort(speakers, s -> SpeakerBriefDto.convertToBriefDto(s, Language.getLanguageByCode(language)));
    }

    @GetMapping("/speaker/{id}")
    public SpeakerDetailsDto getSpeaker(@PathVariable long id, @RequestParam String language) {
        var speaker = speakerService.getSpeakerById(id);
        List<Talk> talks = talkService.getTalksBySpeaker(speaker);
        var speakerDetailsDto = SpeakerDetailsDto.convertToDto(speaker, talks, eventService::getEventByTalk,
                eventTypeService::getEventTypeByEvent, Language.getLanguageByCode(language));

        List<TalkBriefDto> sortedTalks = speakerDetailsDto.talks().stream()
                .sorted(Comparator.comparing(TalkBriefDto::getTalkDate).reversed())
                .toList();

        return new SpeakerDetailsDto(speakerDetailsDto.speaker(), sortedTalks);
    }

    List<SpeakerBriefDto> convertToBriefDtoAndSort(List<Speaker> speakers, Function<List<Speaker>, List<SpeakerBriefDto>> speakerFunction) {
        List<SpeakerBriefDto> speakerBriefDtoList = speakerFunction.apply(speakers);

        Comparator<SpeakerBriefDto> comparatorByName = Comparator.comparing(SpeakerBriefDto::getDisplayName, String.CASE_INSENSITIVE_ORDER);
        Comparator<SpeakerBriefDto> comparatorByCompany = Comparator.comparing(
                s -> s.getCompanies().stream()
                        .map(CompanyBriefDto::getName)
                        .collect(Collectors.joining(", ")), String.CASE_INSENSITIVE_ORDER);

        return speakerBriefDtoList.stream()
                .sorted(comparatorByName.thenComparing(comparatorByCompany))
                .toList();
    }

    List<SpeakerSuperBriefDto> createDuplicatesAndConvertToDtoAndSort(List<Speaker> speakers, Language language) {
        Set<Speaker> speakerDuplicates = LocalizationUtils.getSpeakerDuplicates(
                speakers,
                s -> LocalizationUtils.getString(s.getName(), language),
                s -> true);
        List<SpeakerBriefDto> speakerBriefDtoList = convertToBriefDtoAndSort(
                speakers,
                s -> SpeakerBriefDto.convertToBriefDto(s, language, speakerDuplicates));

        return speakerBriefDtoList.stream()
                .map(s -> new SpeakerSuperBriefDto(s.getId(), s.getDisplayName()))
                .toList();
    }
}
