package guess.domain.source.cms.contentful;

import com.fasterxml.jackson.annotation.JsonProperty;
import guess.domain.source.cms.contentful.asset.ContentfulAsset;

import java.util.List;

public class ContentfulIncludes {
    @JsonProperty("Asset")
    private List<ContentfulAsset> asset;

    public List<ContentfulAsset> getAsset() {
        return asset;
    }

    public void setAsset(List<ContentfulAsset> asset) {
        this.asset = asset;
    }
}
