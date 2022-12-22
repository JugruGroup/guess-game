package guess.dto.eventtype;

import guess.domain.Language;
import guess.domain.source.EventType;
import guess.util.LocalizationUtils;

import java.util.List;

/**
 * Event type DTO (brief).
 */
public class EventTypeBriefDto extends EventTypeSuperBriefDto {
    private final String shortDescription;
    private final String organizerName;
    private final String topicName;

    public EventTypeBriefDto(EventTypeSuperBriefDto eventTypeSuperBriefDto, String shortDescription, String organizerName,
                             String topicName) {
        super(eventTypeSuperBriefDto.getId(), eventTypeSuperBriefDto.isConference(), eventTypeSuperBriefDto.getName(),
                eventTypeSuperBriefDto.getDisplayName(), eventTypeSuperBriefDto.getLogoFileName(), eventTypeSuperBriefDto.isInactive());
        this.shortDescription = shortDescription;
        this.organizerName = organizerName;
        this.topicName = topicName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public String getTopicName() {
        return topicName;
    }

    public static EventTypeBriefDto convertToBriefDto(EventTypeSuperBriefDto eventTypeSuperBriefDto, EventType eventType,
                                                      Language language) {
        var shortDescription = LocalizationUtils.getString(eventType.getShortDescription(), language);
        var organizerName = LocalizationUtils.getString(eventType.getOrganizer().getName(), language);
        var topicName = (eventType.getTopic() != null) ? LocalizationUtils.getString(eventType.getTopic().getName(), language) : null;

        return new EventTypeBriefDto(
                eventTypeSuperBriefDto,
                shortDescription,
                organizerName,
                topicName);
    }

    public static EventTypeBriefDto convertToBriefDto(EventType eventType, Language language) {
        return convertToBriefDto(convertToSuperBriefDto(eventType, language), eventType, language);
    }

    public static List<EventTypeBriefDto> convertToBriefDto(List<EventType> eventTypes, Language language) {
        return eventTypes.stream()
                .map(et -> convertToBriefDto(et, language))
                .toList();
    }
}
