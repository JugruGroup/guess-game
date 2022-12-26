package guess.service;

import guess.dao.EventTypeDao;
import guess.domain.source.EventType;
import guess.domain.source.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Topic service implementation.
 */
@Service
public class TopicServiceImpl implements TopicService {
    private final EventTypeDao eventTypeDao;

    @Autowired
    public TopicServiceImpl(EventTypeDao eventTypeDao) {
        this.eventTypeDao = eventTypeDao;
    }

    @Override
    public List<Topic> getTopics(boolean isConferences, boolean isMeetups, Long organizerId, Boolean defaultTopic) {
        return eventTypeDao.getEventTypes().stream()
                .filter(et -> ((isConferences && et.isEventTypeConference()) || (isMeetups && !et.isEventTypeConference())) &&
                        ((organizerId == null) || (et.getOrganizer().getId() == organizerId)) &&
                        (et.getTopic() != null))
                .map(EventType::getTopic)
                .distinct()
                .filter(t -> (defaultTopic == null) || (t.isDefaultTopic() == defaultTopic))
                .toList();
    }
}
