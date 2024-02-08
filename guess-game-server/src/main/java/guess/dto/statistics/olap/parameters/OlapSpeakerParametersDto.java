package guess.dto.statistics.olap.parameters;

/**
 * OLAP speaker parameters DTO.
 */
public class OlapSpeakerParametersDto extends OlapBasicParametersDto {
    private Long companyId;
    private Long eventTypeId;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }
}
