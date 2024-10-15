package guess.domain.source.cms.contentful.speaker;

import guess.domain.source.Speaker;
import guess.util.load.contentful.ContentfulDataLoader;

public abstract class NotResolvableSpeaker {
    private final ContentfulDataLoader.ConferenceSpaceInfo conferenceSpaceInfo;
    private final String entryId;

    protected NotResolvableSpeaker(ContentfulDataLoader.ConferenceSpaceInfo conferenceSpaceInfo, String entryId) {
        this.conferenceSpaceInfo = conferenceSpaceInfo;
        this.entryId = entryId;
    }

    public ContentfulDataLoader.ConferenceSpaceInfo getConferenceSpaceInfo() {
        return conferenceSpaceInfo;
    }

    public String getEntryId() {
        return entryId;
    }

    public abstract Speaker createSpeaker(long id);
}
