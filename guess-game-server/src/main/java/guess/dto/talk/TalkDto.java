package guess.dto.talk;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.domain.source.Talk;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Talk DTO.
 */
public class TalkDto extends TalkBriefDto {
    private final String description;
    private final String language;

    public TalkDto(TalkSuperBriefDto talkSuperBriefDto, TalkBriefDto talkBriefDto, String description, String language) {
        super(talkSuperBriefDto, talkBriefDto.getTopicName(), talkBriefDto.getPresentationLinks(), talkBriefDto.getMaterialLinks(),
                talkBriefDto.getVideoLinks());

        this.description = description;
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public static TalkDto convertToDto(Talk talk, Function<Talk, Event> talkEventFunction,
                                       Function<Event, EventType> eventEventTypeFunction, Language language) {
        var talkSuperBriefDto = convertToSuperBriefDto(talk, talkEventFunction, eventEventTypeFunction, language);
        var description = LocalizationUtils.getString(talk.getLongDescription(), language);

        if ((description == null) || description.isEmpty()) {
            description = LocalizationUtils.getString(talk.getShortDescription(), language);
        }

        return new TalkDto(
                talkSuperBriefDto,
                convertToBriefDto(talkSuperBriefDto, talk, talkEventFunction, eventEventTypeFunction, language),
                description,
                talk.getLanguage());
    }

    public static List<TalkDto> convertToDto(List<Talk> talks, Function<Talk, Event> talkEventFunction,
                                             Function<Event, EventType> eventEventTypeFunction, Language language) {
        return talks.stream()
                .map(t -> convertToDto(t, talkEventFunction, eventEventTypeFunction, language))
                .toList();
    }
}
