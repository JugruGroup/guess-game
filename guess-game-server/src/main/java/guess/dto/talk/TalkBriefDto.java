package guess.dto.talk;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.domain.source.Talk;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.function.Function;

/**
 * Talk DTO (brief).
 */
public class TalkBriefDto extends TalkSuperBriefDto {
    private final String topicName;

    public TalkBriefDto(TalkSuperBriefDto talkSuperBriefDto, String topicName) {
        super(talkSuperBriefDto.getId(), talkSuperBriefDto.getName(), talkSuperBriefDto.getTalkDate(), talkSuperBriefDto.getTalkDay(),
                talkSuperBriefDto.getTalkStartTime(), talkSuperBriefDto.getTalkEndTime(),
                new TalkSuperBriefDtoDetails(
                        talkSuperBriefDto.getTrack(),
                        talkSuperBriefDto.getEvent(),
                        talkSuperBriefDto.getEventTypeLogoFileName(),
                        talkSuperBriefDto.getSpeakers(),
                        talkSuperBriefDto.getPresentationLinks(),
                        talkSuperBriefDto.getMaterialLinks(),
                        talkSuperBriefDto.getVideoLinks()));

        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    public static TalkBriefDto convertToBriefDto(TalkSuperBriefDto talkSuperBriefDto, Talk talk, Language language) {
        var topicName = (talk.getTopic() != null) ? LocalizationUtils.getString(talk.getTopic().getName(), language) : null;

        return new TalkBriefDto(
                talkSuperBriefDto,
                topicName);
    }

    public static TalkBriefDto convertToBriefDto(Talk talk, Function<Talk, Event> talkEventFunction,
                                                 Function<Event, EventType> eventEventTypeFunction, Language language) {
        return convertToBriefDto(convertToSuperBriefDto(talk, talkEventFunction, eventEventTypeFunction, language), talk, language);
    }

    public static List<TalkBriefDto> convertToBriefDto(List<Talk> talks, Function<Talk, Event> talkEventFunction,
                                                       Function<Event, EventType> eventEventTypeFunction, Language language) {
        return talks.stream()
                .map(t -> convertToBriefDto(t, talkEventFunction, eventEventTypeFunction, language))
                .toList();
    }
}
