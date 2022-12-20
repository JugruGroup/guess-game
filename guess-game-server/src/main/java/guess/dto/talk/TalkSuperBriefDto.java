package guess.dto.talk;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventDays;
import guess.domain.source.EventType;
import guess.domain.source.Talk;
import guess.dto.event.EventSuperBriefDto;
import guess.dto.speaker.SpeakerSuperBriefDto;
import guess.util.LocalizationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Talk DTO (super brief).
 */
public class TalkSuperBriefDto {
    public static class TalkSuperBriefDtoDetails {
        private final EventSuperBriefDto event;
        private final String eventTypeLogoFileName;
        private final List<SpeakerSuperBriefDto> speakers;

        public TalkSuperBriefDtoDetails(EventSuperBriefDto event, String eventTypeLogoFileName, List<SpeakerSuperBriefDto> speakers) {
            this.event = event;
            this.eventTypeLogoFileName = eventTypeLogoFileName;
            this.speakers = speakers;
        }
    }

    private final long id;
    private final String name;
    private final LocalDate talkDate;
    private final Long talkDay;
    private final LocalDateTime talkTime;
    private final Long track;
    private final TalkSuperBriefDtoDetails details;

    public TalkSuperBriefDto(long id, String name, LocalDate talkDate, Long talkDay, LocalDateTime talkTime, Long track,
                             TalkSuperBriefDtoDetails details) {
        this.id = id;
        this.name = name;
        this.talkDate = talkDate;
        this.talkDay = talkDay;
        this.talkTime = talkTime;
        this.track = track;
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getTalkDate() {
        return talkDate;
    }

    public Long getTalkDay() {
        return talkDay;
    }

    public LocalDateTime getTalkTime() {
        return talkTime;
    }

    public Long getTrack() {
        return track;
    }

    public EventSuperBriefDto getEvent() {
        return details.event;
    }

    public String getEventTypeLogoFileName() {
        return details.eventTypeLogoFileName;
    }

    public List<SpeakerSuperBriefDto> getSpeakers() {
        return details.speakers;
    }

    public static TalkSuperBriefDto convertToSuperBriefDto(Talk talk, Function<Talk, Event> talkEventFunction,
                                                           Function<Event, EventType> eventEventTypeFunction, Language language) {
        var event = talkEventFunction.apply(talk);
        var eventType = eventEventTypeFunction.apply(event);
        Map<Long, LocalDate> talkDayDates = new HashMap<>();
        long previousDays = 0;

        // Map (talk day, date)
        for (EventDays eventDays : event.getDays()) {
            long days = ChronoUnit.DAYS.between(eventDays.getStartDate(), eventDays.getEndDate()) + 1;

            for (long i = 1; i <= days; i++) {
                LocalDate date = eventDays.getStartDate().plusDays(i - 1);
                talkDayDates.put(previousDays + i, date);
            }

            previousDays += days;
        }

        Long talkDay = talk.getTalkDay();
        long safeTalkDay = Optional.ofNullable(talkDay).orElse(1L);
        LocalDate talkDate = talkDayDates.get(safeTalkDay);

        var safeLocalDate = Optional.ofNullable(talkDate).orElse(LocalDate.now());
        LocalDateTime talkTime = (talk.getTrackTime() != null) ? LocalDateTime.of(safeLocalDate, talk.getTrackTime()) : null;

        EventSuperBriefDto eventSuperBriefDto = EventSuperBriefDto.convertToSuperBriefDto(event, language);
        String eventTypeLogoFileName = (eventType != null) ? eventType.getLogoFileName() : null;
        List<SpeakerSuperBriefDto> speakers = SpeakerSuperBriefDto.convertToSuperBriefDto(talk.getSpeakers(), language);

        return new TalkSuperBriefDto(
                talk.getId(),
                LocalizationUtils.getString(talk.getName(), language),
                talkDate,
                talk.getTalkDay(),
                talkTime,
                talk.getTrack(),
                new TalkSuperBriefDtoDetails(
                        eventSuperBriefDto,
                        eventTypeLogoFileName,
                        speakers
                )
        );
    }

    public static List<TalkSuperBriefDto> convertToSuperBriefDto(List<Talk> talks, Function<Talk, Event> talkEventFunction,
                                                                 Function<Event, EventType> eventEventTypeFunction,
                                                                 Language language) {
        return talks.stream()
                .map(t -> convertToSuperBriefDto(t, talkEventFunction, eventEventTypeFunction, language))
                .toList();
    }
}
