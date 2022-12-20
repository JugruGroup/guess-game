package guess.dto.talk;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.domain.source.Talk;

import java.util.List;
import java.util.function.Function;

/**
 * Talk DTO (brief).
 */
public class TalkBriefDto extends TalkSuperBriefDto {
    private final List<String> presentationLinks;
    private final List<String> materialLinks;
    private final List<String> videoLinks;

    public TalkBriefDto(TalkSuperBriefDto talkSuperBriefDto, List<String> presentationLinks, List<String> materialLinks,
                        List<String> videoLinks) {
        super(talkSuperBriefDto.getId(), talkSuperBriefDto.getName(), talkSuperBriefDto.getTalkDate(), talkSuperBriefDto.getTalkDay(),
                talkSuperBriefDto.getTalkTime(), talkSuperBriefDto.getTrack(),
                new TalkSuperBriefDtoDetails(
                        talkSuperBriefDto.getEvent(),
                        talkSuperBriefDto.getEventTypeLogoFileName(),
                        talkSuperBriefDto.getSpeakers()));

        this.presentationLinks = presentationLinks;
        this.materialLinks = materialLinks;
        this.videoLinks = videoLinks;
    }

    public List<String> getPresentationLinks() {
        return presentationLinks;
    }

    public List<String> getMaterialLinks() {
        return materialLinks;
    }

    public List<String> getVideoLinks() {
        return videoLinks;
    }

    public static TalkBriefDto convertToBriefDto(TalkSuperBriefDto talkSuperBriefDto, Talk talk) {
        return new TalkBriefDto(talkSuperBriefDto, talk.getPresentationLinks(), talk.getMaterialLinks(), talk.getVideoLinks());
    }

    public static TalkBriefDto convertToBriefDto(Talk talk, Function<Talk, Event> talkEventFunction,
                                                 Function<Event, EventType> eventEventTypeFunction, Language language) {
        return new TalkBriefDto(
                convertToSuperBriefDto(talk, talkEventFunction, eventEventTypeFunction, language),
                talk.getPresentationLinks(),
                talk.getMaterialLinks(),
                talk.getVideoLinks());
    }

    public static List<TalkBriefDto> convertToBriefDto(List<Talk> talks, Function<Talk, Event> talkEventFunction,
                                                       Function<Event, EventType> eventEventTypeFunction, Language language) {
        return talks.stream()
                .map(t -> convertToBriefDto(t, talkEventFunction, eventEventTypeFunction, language))
                .toList();
    }
}
