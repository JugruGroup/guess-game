package guess.service;

import guess.domain.source.Speaker;
import guess.domain.source.Talk;

import java.util.List;

/**
 * Talk service.
 */
public interface TalkService {
    Talk getTalkById(long id);

    List<Talk> getTalks(Long eventTypeId, Long eventId, String talkName, String speakerName, Long topicId, String talkLanguage);

    List<Talk> getTalksBySpeaker(Speaker speaker);
}
