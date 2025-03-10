package guess.domain.source.cms.contentful.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import guess.domain.source.cms.contentful.ContentfulIncludes;
import guess.domain.source.cms.contentful.city.ContentfulCity;

import java.util.List;

public class ContentfulEventIncludes extends ContentfulIncludes {
    @JsonProperty("Entry")
    private List<ContentfulCity> entry;

    public List<ContentfulCity> getEntry() {
        return entry;
    }

    public void setEntry(List<ContentfulCity> entry) {
        this.entry = entry;
    }
}
