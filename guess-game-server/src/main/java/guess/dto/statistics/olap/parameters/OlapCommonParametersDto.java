package guess.dto.statistics.olap.parameters;

import java.util.List;

/**
 * OLAP common parameters DTO.
 */
public abstract class OlapCommonParametersDto extends OlapBasicParametersDto {
    private boolean isConferences;
    private boolean isMeetups;
    private Long organizerId;
    private List<Long> eventTypeIds;

    public boolean isConferences() {
        return isConferences;
    }

    public void setConferences(boolean conferences) {
        isConferences = conferences;
    }

    public boolean isMeetups() {
        return isMeetups;
    }

    public void setMeetups(boolean meetups) {
        isMeetups = meetups;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public List<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public void setEventTypeIds(List<Long> eventTypeIds) {
        this.eventTypeIds = eventTypeIds;
    }
}
