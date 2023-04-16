package guess.dto.talk;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.domain.source.Talk;
import guess.util.LocalizationUtils;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * Talk DTO.
 */
public class TalkDto extends TalkBriefDto {
    private final String description;
    private final String language;
    private final String utcTimeZone;

    public TalkDto(TalkSuperBriefDto talkSuperBriefDto, TalkBriefDto talkBriefDto, String description, String language,
                   String utcTimeZone) {
        super(talkSuperBriefDto, talkBriefDto.getTopicName(), talkBriefDto.getPresentationLinks(), talkBriefDto.getMaterialLinks(),
                talkBriefDto.getVideoLinks());

        this.description = description;
        this.language = language;
        this.utcTimeZone = utcTimeZone;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public String getUtcTimeZone() {
        return utcTimeZone;
    }

    public static TalkDto convertToDto(Talk talk, Function<Talk, Event> talkEventFunction,
                                       Function<Event, EventType> eventEventTypeFunction, Language language) {
        var talkSuperBriefDto = convertToSuperBriefDto(talk, talkEventFunction, eventEventTypeFunction, language);
        var description = LocalizationUtils.getString(talk.getLongDescription(), language);

        if ((description == null) || description.isEmpty()) {
            description = LocalizationUtils.getString(talk.getShortDescription(), language);
        }

        var event = talkEventFunction.apply(talk);
        var eventDates = event.getDays().stream()
                .flatMap(d -> d.getStartDate().datesUntil(d.getEndDate().plusDays(1)))
                .sorted()
                .toList();
        var talkDate = (!eventDates.isEmpty() && (talk.getTalkDay() != null) && (talk.getTalkDay() > 0) && (talk.getTalkDay() <= eventDates.size())) ?
                eventDates.get(talk.getTalkDay().intValue() - 1) : event.getFirstStartDate();
        var talkTime = (talk.getStartTime() != null) ? talk.getStartTime() : LocalTime.of(0, 0, 0);
        var zonedDateTime = ZonedDateTime.of(
                talkDate,
                talkTime,
                event.getFinalTimeZoneId());
        var formatter = DateTimeFormatter.ofPattern("O");
        var utcTimeZone = zonedDateTime.format(formatter).replace("GMT", "UTC");

        return new TalkDto(
                talkSuperBriefDto,
                convertToBriefDto(talkSuperBriefDto, talk, language),
                description,
                talk.getLanguage(),
                utcTimeZone);
    }

    public static List<TalkDto> convertToDto(List<Talk> talks, Function<Talk, Event> talkEventFunction,
                                             Function<Event, EventType> eventEventTypeFunction, Language language) {
        return talks.stream()
                .map(t -> convertToDto(t, talkEventFunction, eventEventTypeFunction, language))
                .toList();
    }
}
