package guess.domain.source.cms.contentful.talk.fields;

import com.fasterxml.jackson.annotation.JsonSetter;
import guess.domain.source.cms.contentful.ContentfulLink;

import java.util.List;

public class ContentfulTalkFieldsCommon extends ContentfulTalkFields {
    @Override
    @JsonSetter("talksPresentation")
    public void setPresentations(List<ContentfulLink> presentations) {
        super.setPresentations(presentations);
    }

    @Override
    @JsonSetter("talksPresentationLink")
    public void setMaterial(String material) {
        super.setMaterial(material);
    }
}
