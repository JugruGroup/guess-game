package guess.domain.source;

import java.util.List;

/**
 * Topic list.
 */
public class TopicList {
    List<Topic> topics;

    public TopicList() {
    }

    public TopicList(List<Topic> topics) {
        this.topics = topics;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}
