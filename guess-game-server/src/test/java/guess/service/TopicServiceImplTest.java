package guess.service;

import guess.dao.EventTypeDao;
import guess.domain.Conference;
import guess.domain.source.EventType;
import guess.domain.source.Organizer;
import guess.domain.source.Topic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;


@DisplayName("TopicServiceImpl class tests")
@ExtendWith(SpringExtension.class)
class TopicServiceImplTest {
    @TestConfiguration
    static class TopicServiceImplTestConfiguration {
        @MockBean
        EventTypeDao eventTypeDao;

        @Bean
        TopicService topicService() {
            return new TopicServiceImpl(eventTypeDao);
        }
    }

    @Autowired
    private EventTypeDao eventTypeDao;

    @Autowired
    private TopicService topicService;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getTopics method with parameters tests")
    class GetTopicsTest {
        private Stream<Arguments> data() {
            Organizer organizer0 = new Organizer();
            organizer0.setId(0);

            Topic topic0 = new Topic();
            topic0.setId(0);

            Topic topic1 = new Topic();
            topic1.setId(1);
            topic1.setDefaultTopic(true);

            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setConference(Conference.JPOINT);
            eventType0.setOrganizer(organizer0);
            eventType0.setTopic(topic0);

            EventType eventType1 = new EventType();
            eventType1.setId(1);
            eventType1.setOrganizer(organizer0);
            eventType1.setTopic(topic0);

            EventType eventType2 = new EventType();
            eventType2.setId(2);
            eventType2.setOrganizer(organizer0);
            eventType2.setTopic(topic1);

            EventType eventType3 = new EventType();
            eventType3.setId(3);
            eventType3.setOrganizer(organizer0);

            List<EventType> eventTypes = List.of(eventType0, eventType1, eventType2, eventType3);

            return Stream.of(
                    arguments(false, false, null, null, eventTypes, Collections.emptyList()),
                    arguments(true, false, null, null, eventTypes, List.of(topic0)),
                    arguments(false, true, null, null, eventTypes, List.of(topic0, topic1)),
                    arguments(true, true, null, null, eventTypes, List.of(topic0, topic1)),
                    arguments(false, false, 0L, null, eventTypes, Collections.emptyList()),
                    arguments(true, false, 0L, null, eventTypes, List.of(topic0)),
                    arguments(false, true, 0L, null, eventTypes, List.of(topic0, topic1)),
                    arguments(true, true, 0L, null, eventTypes, List.of(topic0, topic1)),
                    arguments(false, false, 1L, null, eventTypes, Collections.emptyList()),
                    arguments(true, false, 1L, null, eventTypes, Collections.emptyList()),
                    arguments(false, true, 1L, null, eventTypes, Collections.emptyList()),
                    arguments(true, true, 1L, null, eventTypes, Collections.emptyList()),
                    arguments(true, true, 0L, false, eventTypes, List.of(topic0)),
                    arguments(true, true, 0L, true, eventTypes, List.of(topic1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getTopics(boolean isConferences, boolean isMeetups, Long organizerId, Boolean defaultTopic,
                       List<EventType> eventTypes, List<Topic> expected) {
            Mockito.when(eventTypeDao.getEventTypes()).thenReturn(eventTypes);

            assertEquals(expected, topicService.getTopics(isConferences, isMeetups, organizerId, defaultTopic));
            Mockito.verify(eventTypeDao, VerificationModeFactory.times(1)).getEventTypes();
            Mockito.verifyNoMoreInteractions(eventTypeDao);

            Mockito.reset(eventTypeDao);
        }
    }
}
