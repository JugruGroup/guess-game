package guess.dto.statistics.olap.parameters;

/**
 * OLAP city parameters DTO.
 */
public class OlapCityParametersDto extends OlapBasicParametersDto {
    private Long eventTypeId;

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }
}
