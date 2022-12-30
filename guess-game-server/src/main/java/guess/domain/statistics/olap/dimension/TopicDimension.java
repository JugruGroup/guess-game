package guess.domain.statistics.olap.dimension;

import guess.domain.source.Topic;

/**
 * Topic dimension.
 */
public class TopicDimension extends Dimension<Topic> {
    public TopicDimension(Object value) {
        super(Topic.class, value);
    }
}
