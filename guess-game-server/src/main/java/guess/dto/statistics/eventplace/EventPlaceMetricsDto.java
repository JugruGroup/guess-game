package guess.dto.statistics.eventplace;

import guess.domain.Language;
import guess.domain.statistics.eventplace.AbstractEventPlaceMetrics;
import guess.domain.statistics.eventplace.EventPlaceMetrics;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;

/**
 * Event place metrics DTO.
 */
public class EventPlaceMetricsDto extends AbstractEventPlaceMetrics {
    private final long id;
    private final String placeCity;
    private final String placeVenueAddress;
    private final String mapCoordinates;

    public EventPlaceMetricsDto(long id, String placeCity, String placeVenueAddress, String mapCoordinates,
                                AbstractEventPlaceMetrics metrics) {
        super(metrics.getEventsQuantity(), metrics.getEventTypesQuantity(), metrics);

        this.id = id;
        this.placeCity = placeCity;
        this.placeVenueAddress = placeVenueAddress;
        this.mapCoordinates = mapCoordinates;
    }

    public long getId() {
        return id;
    }

    public String getPlaceCity() {
        return placeCity;
    }

    public String getPlaceVenueAddress() {
        return placeVenueAddress;
    }

    public String getMapCoordinates() {
        return mapCoordinates;
    }

    public static EventPlaceMetricsDto convertToDto(EventPlaceMetrics eventPlaceMetrics, Language language) {
        var place = eventPlaceMetrics.getPlace();
        long id = (place != null) ? place.getId() : 0;
        String placeCity = (place != null) ? LocalizationUtils.getString(place.getCity(), language) : null;
        String placeVenueAddress = (place != null) ? LocalizationUtils.getString(place.getVenueAddress(), language) : null;
        String mapCoordinates = (place != null) ? place.getMapCoordinates() : null;

        return new EventPlaceMetricsDto(
                id,
                placeCity,
                placeVenueAddress,
                mapCoordinates,
                eventPlaceMetrics);
    }

    public static List<EventPlaceMetricsDto> convertToDto(List<EventPlaceMetrics> eventPlaceMetricsList, Language language) {
        return eventPlaceMetricsList.stream()
                .map(epm -> convertToDto(epm, language))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventPlaceMetricsDto that)) return false;
        if (!super.equals(o)) return false;
        return getId() == that.getId() &&
                Objects.equals(getPlaceCity(), that.getPlaceCity()) &&
                Objects.equals(getPlaceVenueAddress(), that.getPlaceVenueAddress()) &&
                Objects.equals(getMapCoordinates(), that.getMapCoordinates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), getPlaceCity(), getPlaceVenueAddress(), getMapCoordinates());
    }
}
