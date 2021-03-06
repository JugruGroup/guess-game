package guess.dto.event;

import guess.domain.Language;
import guess.domain.source.Event;
import guess.domain.source.EventType;
import guess.domain.source.Place;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Event DTO.
 */
public class EventDto extends EventBriefDto {
    public static class EventDtoLinks {
        private final String siteLink;
        private final String youtubeLink;

        private final String vkLink;
        private final String twitterLink;
        private final String facebookLink;
        private final String telegramLink;

        public EventDtoLinks(String siteLink, String youtubeLink, String vkLink, String twitterLink, String facebookLink,
                             String telegramLink) {
            this.siteLink = siteLink;
            this.youtubeLink = youtubeLink;
            this.vkLink = vkLink;
            this.twitterLink = twitterLink;
            this.facebookLink = facebookLink;
            this.telegramLink = telegramLink;
        }
    }

    private final EventDtoLinks links;
    private final String mapCoordinates;
    private final String description;

    public EventDto(EventSuperBriefDto eventSuperBriefDto, EventBriefDto eventBriefDto, EventDtoLinks links,
                    String mapCoordinates, String description) {
        super(eventSuperBriefDto, eventBriefDto.getDuration(), eventBriefDto.getPlaceCity(), eventBriefDto.getPlaceVenueAddress(),
                eventBriefDto.getEventTypeLogoFileName());

        this.links = links;
        this.mapCoordinates = mapCoordinates;
        this.description = description;
    }

    public String getSiteLink() {
        return links.siteLink;
    }

    public String getYoutubeLink() {
        return links.youtubeLink;
    }

    public String getMapCoordinates() {
        return mapCoordinates;
    }

    public String getVkLink() {
        return links.vkLink;
    }

    public String getTwitterLink() {
        return links.twitterLink;
    }

    public String getFacebookLink() {
        return links.facebookLink;
    }

    public String getTelegramLink() {
        return links.telegramLink;
    }

    public String getDescription() {
        return description;
    }

    public static EventDto convertToDto(Event event, Function<Event, EventType> eventEventTypeFunction, Language language) {
        EventSuperBriefDto eventSuperBriefDto = convertToSuperBriefDto(event, language);
        Place place = event.getPlace();
        String mapCoordinates = (place != null) ? place.getMapCoordinates() : null;
        String eventSiteLink = LocalizationUtils.getString(event.getSiteLink(), language);
        String eventYoutubeLink = event.getYoutubeLink();

        EventType eventType = eventEventTypeFunction.apply(event);
        String eventTypeSiteLink = (eventType != null) ? LocalizationUtils.getString(eventType.getSiteLink(), language) : null;
        String eventTypeYoutubeLink = (eventType != null) ? eventType.getYoutubeLink() : null;
        String eventTypeVkLink = (eventType != null) ? eventType.getVkLink() : null;
        String eventTypeTwitterLink = (eventType != null) ? eventType.getTwitterLink() : null;
        String eventTypeFacebookLink = (eventType != null) ? eventType.getFacebookLink() : null;
        String eventTypeTelegramLink = (eventType != null) ? eventType.getTelegramLink() : null;
        String description = (eventType != null) ? LocalizationUtils.getString(eventType.getShortDescription(), language) : null;

        return new EventDto(
                eventSuperBriefDto,
                convertToBriefDto(eventSuperBriefDto, event, language),
                new EventDtoLinks(
                        ((eventSiteLink != null) && !eventSiteLink.isEmpty()) ? eventSiteLink : eventTypeSiteLink,
                        ((eventYoutubeLink != null) && !eventYoutubeLink.isEmpty()) ? eventYoutubeLink : eventTypeYoutubeLink,
                        eventTypeVkLink,
                        eventTypeTwitterLink,
                        eventTypeFacebookLink,
                        eventTypeTelegramLink
                ),
                mapCoordinates,
                description);
    }

    public static List<EventDto> convertToDto(List<Event> events, Function<Event, EventType> eventEventTypeFunction, Language language) {
        return events.stream()
                .map(e -> convertToDto(e, eventEventTypeFunction, language))
                .collect(Collectors.toList());
    }
}
