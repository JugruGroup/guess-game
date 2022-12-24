package guess.service;

import guess.domain.source.Topic;

import java.util.List;

/**
 * Topic service.
 */
public interface TopicService {
    List<Topic> getTopics(boolean isConferences, boolean isMeetups, Long organizerId, Boolean defaultTopic);
}
