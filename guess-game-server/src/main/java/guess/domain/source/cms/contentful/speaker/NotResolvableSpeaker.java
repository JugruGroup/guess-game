package guess.domain.source.cms.contentful.speaker;

import guess.domain.source.Speaker;
import guess.util.load.contentful.ConferenceSpaceInfo;

public abstract class NotResolvableSpeaker {
    private final ConferenceSpaceInfo conferenceSpaceInfo;
    private final String entryId;

    protected NotResolvableSpeaker(ConferenceSpaceInfo conferenceSpaceInfo, String entryId) {
        this.conferenceSpaceInfo = conferenceSpaceInfo;
        this.entryId = entryId;
    }

    public ConferenceSpaceInfo getConferenceSpaceInfo() {
        return conferenceSpaceInfo;
    }

    public String getEntryId() {
        return entryId;
    }

    public abstract Speaker createSpeaker(long id);
}
