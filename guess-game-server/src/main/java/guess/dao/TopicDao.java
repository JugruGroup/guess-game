package guess.dao;

import guess.domain.source.Topic;

import java.util.List;

/**
 * Topic DAO.
 */
public interface TopicDao {
    List<Topic> getTopics();
}
