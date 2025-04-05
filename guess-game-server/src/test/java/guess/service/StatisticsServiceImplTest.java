package guess.service;

import guess.dao.EventDao;
import guess.dao.EventTypeDao;
import guess.dao.PlaceDao;
import guess.domain.Conference;
import guess.domain.source.*;
import guess.domain.statistics.EventTypeEventMetrics;
import guess.domain.statistics.Metrics;
import guess.domain.statistics.company.CompanyMetrics;
import guess.domain.statistics.company.CompanyStatistics;
import guess.domain.statistics.event.EventMetrics;
import guess.domain.statistics.event.EventStatistics;
import guess.domain.statistics.eventplace.EventPlaceMetrics;
import guess.domain.statistics.eventplace.EventPlaceStatistics;
import guess.domain.statistics.eventtype.EventTypeMetrics;
import guess.domain.statistics.eventtype.EventTypeStatistics;
import guess.domain.statistics.speaker.SpeakerMetrics;
import guess.domain.statistics.speaker.SpeakerStatistics;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("StatisticsServiceImpl class tests")
@ExtendWith(SpringExtension.class)
class StatisticsServiceImplTest {
    private static final LocalDate NOW_DATE;

    private static final LocalDate EVENT_START_DATE0;
    private static final LocalDate EVENT_END_DATE0;

    private static final LocalDate EVENT_START_DATE1;
    private static final LocalDate EVENT_END_DATE1;

    private static final LocalDate EVENT_START_DATE2;
    private static final LocalDate EVENT_END_DATE2;

    private static final LocalDate EVENT_START_DATE3;
    private static final LocalDate EVENT_END_DATE3;

    private static final LocalDate EVENT_START_DATE4;
    private static final LocalDate EVENT_END_DATE4;

    private static final LocalDate EVENT_START_DATE5;
    private static final LocalDate EVENT_END_DATE5;

    private static Place place0;
    private static Place place1;
    private static Place place2;
    private static EventType eventType0;
    private static EventType eventType1;
    private static EventType eventType2;
    private static EventType eventType3;
    private static EventType eventType4;
    private static Event event0;
    private static Event event1;
    private static Event event2;
    private static Event event3;
    private static Event event4;
    private static Event event5;
    private static Company company0;
    private static Company company1;
    private static Company company2;
    private static Speaker speaker0;
    private static Speaker speaker1;
    private static Speaker speaker2;

    static {
        NOW_DATE = LocalDate.now();

        EVENT_START_DATE0 = NOW_DATE.minus(3, ChronoUnit.DAYS);
        EVENT_END_DATE0 = EVENT_START_DATE0.plus(1, ChronoUnit.DAYS);

        EVENT_START_DATE1 = NOW_DATE.plus(3, ChronoUnit.DAYS);
        EVENT_END_DATE1 = EVENT_START_DATE1;

        EVENT_START_DATE2 = NOW_DATE.plus(7, ChronoUnit.DAYS);
        EVENT_END_DATE2 = EVENT_START_DATE2;

        EVENT_START_DATE3 = EVENT_START_DATE2.minus(1, ChronoUnit.DAYS);
        EVENT_END_DATE3 = EVENT_START_DATE3;

        EVENT_START_DATE4 = EVENT_START_DATE2.plus(1, ChronoUnit.DAYS);
        EVENT_END_DATE4 = EVENT_START_DATE4;

        EVENT_START_DATE5 = NOW_DATE.minus(4, ChronoUnit.YEARS);
        EVENT_END_DATE5 = EVENT_START_DATE5;
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        EventTypeDao eventTypeDao() {
            return Mockito.mock(EventTypeDao.class);
        }

        @Bean
        EventDao eventDao() {
            return Mockito.mock(EventDao.class);
        }

        @Bean
        PlaceDao placeDao() {
            return Mockito.mock(PlaceDao.class);
        }

        @Bean
        StatisticsService statisticsService() {
            return new StatisticsServiceImpl(eventTypeDao(), eventDao(), placeDao());
        }
    }

    @Autowired
    private EventTypeDao eventTypeDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private PlaceDao placeDao;

    @Autowired
    private StatisticsService statisticsService;

    @BeforeAll
    static void init() {
        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        Organizer organizer1 = new Organizer();
        organizer1.setId(1);

        Topic topic0 = new Topic();
        topic0.setId(0);

        ZoneId zoneId0 = ZoneId.of("Europe/Moscow");

        eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setConference(Conference.JPOINT);
        eventType0.setOrganizer(organizer0);
        eventType0.setTimeZoneId(zoneId0);
        eventType0.setTopic(topic0);

        eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizer(organizer1);
        eventType1.setTimeZoneId(zoneId0);
        eventType1.setTopic(topic0);

        eventType2 = new EventType();
        eventType2.setId(2);
        eventType2.setConference(Conference.JOKER);
        eventType2.setOrganizer(organizer1);
        eventType2.setTimeZoneId(zoneId0);

        eventType3 = new EventType();
        eventType3.setId(3);
        eventType3.setOrganizer(organizer1);
        eventType3.setTimeZoneId(zoneId0);

        eventType4 = new EventType();
        eventType4.setId(4);
        eventType4.setOrganizer(organizer1);
        eventType4.setTimeZoneId(zoneId0);

        company0 = new Company();
        company0.setId(0);

        company1 = new Company();
        company1.setId(1);

        company2 = new Company();
        company2.setId(2);

        speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setJavaChampion(true);
        speaker0.setCompanies(List.of(company0));

        speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setMvp(true);
        speaker1.setCompanies(List.of(company1));

        speaker2 = new Speaker();
        speaker2.setId(2);
        speaker2.setCompanies(List.of(company2));

        Talk talk0 = new Talk();
        talk0.setId(0);
        talk0.setSpeakers(List.of(speaker0));
        talk0.setTalkDay(1L);

        Talk talk1 = new Talk();
        talk1.setId(1);
        talk1.setSpeakers(List.of(speaker1));
        talk1.setTalkDay(1L);

        Talk talk2 = new Talk();
        talk2.setId(2);
        talk2.setSpeakers(List.of(speaker2));
        talk2.setTalkDay(1L);

        place0 = new Place();
        place0.setId(0);

        place1 = new Place();
        place1.setId(1);

        place2 = new Place();
        place2.setId(2);

        event0 = new Event();
        event0.setId(0);
        event0.setEventType(eventType0);
        event0.setDays(List.of(new EventDays(
                EVENT_START_DATE0,
                EVENT_END_DATE0,
                place0
        )));
        event0.setTalks(List.of(talk0));

        event1 = new Event();
        event1.setId(1);
        event1.setEventType(eventType1);
        event1.setDays(List.of(new EventDays(
                EVENT_START_DATE1,
                EVENT_END_DATE1,
                place1
        )));
        event1.setTalks(List.of(talk1));

        event2 = new Event();
        event2.setId(2);
        event2.setEventType(eventType2);
        event2.setDays(List.of(new EventDays(
                EVENT_START_DATE2,
                EVENT_END_DATE2,
                place2
        )));
        event2.setTalks(List.of(talk2));

        event3 = new Event();
        event3.setId(3);
        event3.setEventType(eventType2);
        event3.setDays(List.of(new EventDays(
                EVENT_START_DATE3,
                EVENT_END_DATE3,
                place0
        )));

        event4 = new Event();
        event4.setId(4);
        event4.setEventType(eventType2);
        event4.setDays(List.of(new EventDays(
                EVENT_START_DATE4,
                EVENT_END_DATE4,
                place1
        )));

        event5 = new Event();
        event5.setId(5);
        event5.setEventType(eventType3);
        event5.setDays(List.of(new EventDays(
                EVENT_START_DATE5,
                EVENT_END_DATE5,
                place2
        )));

        eventType0.setEvents(List.of(event0));
        eventType1.setEvents(List.of(event1));
        eventType2.setEvents(List.of(event2, event3, event4));
        eventType3.setEvents(List.of(event5));
    }

    @BeforeEach
    void setUp() {
        Mockito.when(eventTypeDao.getEventTypes()).thenReturn(List.of(eventType0, eventType1, eventType2, eventType3, eventType4));
        Mockito.when(eventDao.getEvents()).thenReturn(List.of(event0, event1, event2, event3, event4, event5));
        Mockito.when(placeDao.getPlaces()).thenReturn(List.of(place0, place1, place2));
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(eventTypeDao);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getStatisticsEventTypes method with parameters tests")
    class GetStatisticsEventTypesTest {
        private Stream<Arguments> data() {
            List<EventType> eventTypes = List.of(eventType0, eventType1, eventType2);

            return Stream.of(
                    arguments(false, false, null, null, null, eventTypes, Collections.emptyList()),
                    arguments(true, false, null, null, null, eventTypes, List.of(eventType0, eventType2)),
                    arguments(false, true, null, null, null, eventTypes, List.of(eventType1)),
                    arguments(true, true, null, null, null, eventTypes, List.of(eventType0, eventType1, eventType2)),
                    arguments(false, false, 0L, null, null, eventTypes, Collections.emptyList()),
                    arguments(true, false, 0L, null, null, eventTypes, List.of(eventType0)),
                    arguments(false, true, 0L, null, null, eventTypes, Collections.emptyList()),
                    arguments(true, true, 0L, null, null, eventTypes, List.of(eventType0)),
                    arguments(false, false, 1L, null, null, eventTypes, Collections.emptyList()),
                    arguments(true, false, 1L, null, null, eventTypes, List.of(eventType2)),
                    arguments(false, true, 1L, null, null, eventTypes, List.of(eventType1)),
                    arguments(true, true, 1L, null, null, eventTypes, List.of(eventType1, eventType2)),

                    arguments(false, false, null, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(true, false, null, null, 0L, eventTypes, List.of(eventType0)),
                    arguments(false, true, null, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(true, true, null, null, 0L, eventTypes, List.of(eventType0)),
                    arguments(false, false, 0L, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(true, false, 0L, null, 0L, eventTypes, List.of(eventType0)),
                    arguments(false, true, 0L, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(true, true, 0L, null, 0L, eventTypes, List.of(eventType0)),
                    arguments(false, false, 1L, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(true, false, 1L, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(false, true, 1L, null, 0L, eventTypes, Collections.emptyList()),
                    arguments(true, true, 1L, null, 0L, eventTypes, Collections.emptyList()),

                    arguments(false, false, null, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(true, false, null, null, 2L, eventTypes, List.of(eventType2)),
                    arguments(false, true, null, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(true, true, null, null, 2L, eventTypes, List.of(eventType2)),
                    arguments(false, false, 0L, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(true, false, 0L, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(false, true, 0L, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(true, true, 0L, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(false, false, 1L, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(true, false, 1L, null, 2L, eventTypes, List.of(eventType2)),
                    arguments(false, true, 1L, null, 2L, eventTypes, Collections.emptyList()),
                    arguments(true, true, 1L, null, 2L, eventTypes, List.of(eventType2)),

                    arguments(true, true, null, 0L, null, eventTypes, List.of(eventType0, eventType1)),
                    arguments(true, true, null, 1L, null, eventTypes, Collections.emptyList())
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getStatisticsEventTypes(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId, Long eventTypeId,
                                     List<EventType> eventTypes, List<EventType> expected) {
            Mockito.when(eventTypeDao.getEventTypes()).thenReturn(eventTypes);

            assertEquals(expected, statisticsService.getStatisticsEventTypes(isConferences, isMeetups, organizerId, topicId, eventTypeId));
            Mockito.verify(eventTypeDao, VerificationModeFactory.times(1)).getEventTypes();
            Mockito.verifyNoMoreInteractions(eventTypeDao);
        }
    }

    private EventTypeStatistics createEventTypeStatistics(List<EventTypeMetrics> eventTypeMetricsList,
                                                          EventType eventType, LocalDate startDate, LocalDate endDate,
                                                          long age, long duration, long eventsQuantity, long speakersQuantity,
                                                          long companiesQuantity, long talksQuantity, long javaChampionsQuantity,
                                                          long mvpsQuantity) {
        return new EventTypeStatistics(
                eventTypeMetricsList,
                new EventTypeMetrics(
                        eventType,
                        age,
                        eventsQuantity,
                        new EventTypeEventMetrics(
                                startDate,
                                endDate,
                                duration,
                                speakersQuantity,
                                companiesQuantity,
                                new Metrics(
                                        talksQuantity,
                                        javaChampionsQuantity,
                                        mvpsQuantity
                                )
                        )
                )
        );
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventTypeStatistics method with parameters tests")
    class GetEventTypeStatisticsTest {
        private Stream<Arguments> data() {
            EventTypeMetrics eventTypeMetrics0 = new EventTypeMetrics(
                    eventType0,
                    ChronoUnit.YEARS.between(EVENT_START_DATE0, NOW_DATE),
                    1,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE0,
                            EVENT_END_DATE0,
                            2,
                            1,
                            1,
                            new Metrics(
                                    1,
                                    1,
                                    0
                            )
                    )
            );
            EventTypeMetrics eventTypeMetrics1 = new EventTypeMetrics(
                    eventType1,
                    ChronoUnit.YEARS.between(EVENT_START_DATE1, NOW_DATE),
                    1,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE1,
                            EVENT_END_DATE1,
                            1,
                            1,
                            1,
                            new Metrics(
                                    1,
                                    0,
                                    1
                            )
                    )
            );
            EventTypeMetrics eventTypeMetrics2 = new EventTypeMetrics(
                    eventType2,
                    ChronoUnit.YEARS.between(EVENT_START_DATE3, NOW_DATE),
                    3,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE3,
                            EVENT_END_DATE4,
                            3,
                            1,
                            1,
                            new Metrics(
                                    1,
                                    0,
                                    0
                            )
                    )
            );
            EventTypeMetrics eventTypeMetrics3 = new EventTypeMetrics(
                    eventType3,
                    ChronoUnit.YEARS.between(EVENT_START_DATE5, NOW_DATE),
                    1,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE5,
                            EVENT_END_DATE5,
                            1,
                            0,
                            0,
                            new Metrics(
                                    0,
                                    0,
                                    0
                            )
                    )
            );
            EventTypeMetrics eventTypeMetrics4 = new EventTypeMetrics(
                    eventType4,
                    0,
                    0,
                    new EventTypeEventMetrics(
                            null,
                            null,
                            0,
                            0,
                            0,
                            new Metrics(
                                    0,
                                    0,
                                    0
                            )
                    )
            );

            EventTypeStatistics expected0 = createEventTypeStatistics(
                    Collections.emptyList(),
                    new EventType(),
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0, 0, 0
            );

            EventTypeStatistics expected1 = createEventTypeStatistics(
                    List.of(eventTypeMetrics1, eventTypeMetrics3, eventTypeMetrics4),
                    new EventType(),
                    EVENT_START_DATE5,
                    EVENT_END_DATE1,
                    ChronoUnit.YEARS.between(EVENT_START_DATE5, NOW_DATE),
                    2,
                    2,
                    1,
                    1,
                    1, 0, 1
            );

            EventTypeStatistics expected2 = createEventTypeStatistics(
                    List.of(eventTypeMetrics0, eventTypeMetrics2),
                    new EventType(),
                    EVENT_START_DATE0,
                    EVENT_END_DATE4,
                    ChronoUnit.YEARS.between(EVENT_START_DATE0, NOW_DATE),
                    5,
                    4,
                    2,
                    2,
                    2, 1, 0
            );

            EventTypeStatistics expected3 = createEventTypeStatistics(
                    List.of(eventTypeMetrics0, eventTypeMetrics1, eventTypeMetrics2, eventTypeMetrics3, eventTypeMetrics4),
                    new EventType(),
                    EVENT_START_DATE5,
                    EVENT_END_DATE4,
                    ChronoUnit.YEARS.between(EVENT_START_DATE5, NOW_DATE),
                    7,
                    6,
                    3,
                    3,
                    3, 1, 1
            );

            return Stream.of(
                    arguments(false, false, null, null, expected0),
                    arguments(false, true, null, null, expected1),
                    arguments(true, false, null, null, expected2),
                    arguments(true, true, null, null, expected3)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventTypeStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long topicId,
                                    EventTypeStatistics expected) {
            assertEquals(expected, statisticsService.getEventTypeStatistics(isConferences, isMeetups, organizerId, topicId));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventTypeAge method with parameters tests")
    class GetEventTypeAgeTest {
        private Stream<Arguments> data() {
            var zoneId = ZoneId.of("Europe/Moscow");
            var currentLocalDateTime = ZonedDateTime.of(
                            2021, 11, 15, 0, 0, 0, 0,
                            zoneId)
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime();

            LocalDate localDate0 = currentLocalDateTime
                    .plus(1, ChronoUnit.YEARS)
                    .plus(1, ChronoUnit.DAYS)
                    .toLocalDate();
            LocalDate localDate1 = currentLocalDateTime
                    .plus(2, ChronoUnit.YEARS)
                    .plus(1, ChronoUnit.DAYS)
                    .toLocalDate();
            LocalDate localDate2 = currentLocalDateTime
                    .minus(1, ChronoUnit.YEARS)
                    .minus(1, ChronoUnit.DAYS)
                    .toLocalDate();
            LocalDate localDate3 = currentLocalDateTime
                    .minus(2, ChronoUnit.YEARS)
                    .minus(1, ChronoUnit.DAYS)
                    .toLocalDate();

            return Stream.of(
                    arguments(null, null, null, 0),
                    arguments(null, zoneId, null, 0),
                    arguments(null, null, currentLocalDateTime, 0),
                    arguments(null, zoneId, currentLocalDateTime, 0),
                    arguments(localDate0, zoneId, currentLocalDateTime, 0),
                    arguments(localDate1, zoneId, currentLocalDateTime, 0),
                    arguments(localDate2, zoneId, currentLocalDateTime, 1),
                    arguments(localDate3, zoneId, currentLocalDateTime, 2)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventTypeAge(LocalDate eventTypeStartDate, ZoneId zoneId, LocalDateTime currentLocalDateTime, long expected) {
            assertEquals(expected, StatisticsServiceImpl.getEventTypeAge(eventTypeStartDate, zoneId, currentLocalDateTime));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getFilteredEvents method with parameters tests")
    class GetFilteredEventsTest {
        private Stream<Arguments> data() {
            List<Event> expected0 = List.of(event0, event2, event3, event4);
            List<Event> expected1 = List.of(event0);
            List<Event> expected2 = Collections.emptyList();
            List<Event> expected3 = List.of(event2, event3, event4);
            List<Event> expected4 = List.of(event1, event5);
            List<Event> expected5 = List.of(event0, event1, event2, event3, event4, event5);

            return Stream.of(
                    arguments(true, false, null, null, expected0),
                    arguments(true, false, null, 0L, expected1),
                    arguments(true, false, null, 1L, expected2),
                    arguments(true, false, null, 2L, expected3),
                    arguments(true, false, null, 3L, expected2),

                    arguments(true, false, 0L, null, expected1),
                    arguments(true, false, 1L, null, expected3),

                    arguments(true, false, 0L, 0L, expected1),
                    arguments(true, false, 1L, 0L, expected2),

                    arguments(true, false, 0L, 1L, expected2),
                    arguments(true, false, 1L, 1L, expected2),

                    arguments(true, false, 0L, 2L, expected2),
                    arguments(true, false, 1L, 2L, expected3),

                    arguments(true, false, 0L, 3L, expected2),
                    arguments(true, false, 1L, 3L, expected2),

                    arguments(false, false, null, null, expected2),
                    arguments(false, true, null, null, expected4),
                    arguments(true, true, null, null, expected5)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getFilteredEvents(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId,
                               List<Event> expected) {
            assertEquals(expected, statisticsService.getFilteredEvents(isConferences, isMeetups, organizerId, eventTypeId));
        }
    }

    private EventStatistics createEventStatistics(List<EventMetrics> eventMetricsList, Event event, LocalDate startDate,
                                                  LocalDate endDate, long duration, long talksQuantity, long speakersQuantity,
                                                  long companiesQuantity, long javaChampionsQuantity, long mvpsQuantity) {
        return new EventStatistics(
                eventMetricsList,
                new EventMetrics(
                        event,
                        new EventTypeEventMetrics(
                                startDate,
                                endDate,
                                duration,
                                speakersQuantity,
                                companiesQuantity,
                                new Metrics(
                                        talksQuantity,
                                        javaChampionsQuantity,
                                        mvpsQuantity
                                )
                        )
                )
        );
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventStatistics method with parameters tests")
    class GetEventStatisticsTest {
        private Stream<Arguments> data() {
            EventMetrics eventMetrics0 = new EventMetrics(
                    event0,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE0,
                            EVENT_END_DATE0,
                            2,
                            1,
                            1,
                            new Metrics(
                                    1,
                                    1,
                                    0
                            )
                    )
            );
            EventMetrics eventMetrics2 = new EventMetrics(
                    event2,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE2,
                            EVENT_END_DATE2,
                            1,
                            1,
                            1,
                            new Metrics(
                                    1,
                                    0,
                                    0
                            )
                    )
            );
            EventMetrics eventMetrics3 = new EventMetrics(
                    event3,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE3,
                            EVENT_END_DATE3,
                            1,
                            0,
                            0,
                            new Metrics(
                                    0,
                                    0,
                                    0
                            )
                    )
            );
            EventMetrics eventMetrics4 = new EventMetrics(
                    event4,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE4,
                            EVENT_END_DATE4,
                            1,
                            0,
                            0,
                            new Metrics(
                                    0,
                                    0,
                                    0
                            )
                    )
            );
            EventMetrics eventMetrics5 = new EventMetrics(
                    event1,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE1,
                            EVENT_END_DATE1,
                            1,
                            1,
                            1,
                            new Metrics(
                                    1,
                                    0,
                                    1
                            )
                    )
            );
            EventMetrics eventMetrics6 = new EventMetrics(
                    event5,
                    new EventTypeEventMetrics(
                            EVENT_START_DATE5,
                            EVENT_END_DATE5,
                            1,
                            0,
                            0,
                            new Metrics(
                                    0,
                                    0,
                                    0
                            )
                    )
            );

            EventStatistics expected0 = createEventStatistics(
                    List.of(eventMetrics0, eventMetrics2, eventMetrics3, eventMetrics4),
                    new Event(),
                    EVENT_START_DATE0,
                    EVENT_END_DATE4,
                    5,
                    2,
                    2,
                    2,
                    1,
                    0
            );

            EventStatistics expected1 = createEventStatistics(
                    List.of(eventMetrics0),
                    new Event(),
                    EVENT_START_DATE0,
                    EVENT_END_DATE0,
                    2,
                    1,
                    1,
                    1,
                    1,
                    0
            );

            EventStatistics expected2 = createEventStatistics(
                    Collections.emptyList(),
                    new Event(),
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );

            EventStatistics expected3 = createEventStatistics(
                    List.of(eventMetrics2, eventMetrics3, eventMetrics4),
                    new Event(),
                    EVENT_START_DATE3,
                    EVENT_END_DATE4,
                    3,
                    1,
                    1,
                    1,
                    0,
                    0
            );

            EventStatistics expected4 = createEventStatistics(
                    List.of(eventMetrics5, eventMetrics6),
                    new Event(),
                    EVENT_START_DATE5,
                    EVENT_END_DATE1,
                    2,
                    1,
                    1,
                    1,
                    0,
                    1
            );

            EventStatistics expected5 = createEventStatistics(
                    List.of(eventMetrics0, eventMetrics5, eventMetrics2, eventMetrics3, eventMetrics4, eventMetrics6),
                    new Event(),
                    EVENT_START_DATE5,
                    EVENT_END_DATE4,
                    7,
                    3,
                    3,
                    3,
                    1,
                    1
            );

            return Stream.of(
                    arguments(true, false, null, null, expected0),
                    arguments(true, false, null, 0L, expected1),
                    arguments(true, false, null, 1L, expected2),
                    arguments(true, false, null, 2L, expected3),
                    arguments(true, false, null, 3L, expected2),

                    arguments(true, false, 0L, null, expected1),
                    arguments(true, false, 1L, null, expected3),

                    arguments(true, false, 0L, 0L, expected1),
                    arguments(true, false, 1L, 0L, expected2),

                    arguments(true, false, 0L, 1L, expected2),
                    arguments(true, false, 1L, 1L, expected2),

                    arguments(true, false, 0L, 2L, expected2),
                    arguments(true, false, 1L, 2L, expected3),

                    arguments(true, false, 0L, 3L, expected2),
                    arguments(true, false, 1L, 3L, expected2),

                    arguments(false, false, null, null, expected2),
                    arguments(false, true, null, null, expected4),
                    arguments(true, true, null, null, expected5)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId, EventStatistics expected) {
            assertEquals(expected, statisticsService.getEventStatistics(isConferences, isMeetups, organizerId, eventTypeId));
        }
    }

    private EventPlaceStatistics createEventPlaceStatistics(List<EventPlaceMetrics> eventPlaceMetricsList, Place place,
                                                            long eventsQuantity, long eventTypesQuantity,
                                                            LocalDate startDate, LocalDate endDate,
                                                            long duration, long speakersQuantity,
                                                            long companiesQuantity,
                                                            long talksQuantity, long javaChampionsQuantity,
                                                            long mvpsQuantity) {
        return new EventPlaceStatistics(
                eventPlaceMetricsList,
                new EventPlaceMetrics(
                        place,
                        eventsQuantity,
                        eventTypesQuantity,
                        new EventTypeEventMetrics(
                                startDate,
                                endDate,
                                duration,
                                speakersQuantity,
                                companiesQuantity,
                                new Metrics(
                                        talksQuantity,
                                        javaChampionsQuantity,
                                        mvpsQuantity
                                )
                        )
                )
        );
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventPlaceStatistics method with parameters tests")
    class GetEventPlaceStatisticsTest {
        private Stream<Arguments> data() {
            EventPlaceStatistics expected0 = createEventPlaceStatistics(
                    List.of(
                            new EventPlaceMetrics(
                                    place0,
                                    2,
                                    2,
                                    new EventTypeEventMetrics(
                                            Collections.min(List.of(EVENT_START_DATE0, EVENT_START_DATE3)),
                                            Collections.max(List.of(EVENT_END_DATE0, EVENT_END_DATE3)),
                                            3,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    1,
                                                    0
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place1,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE4,
                                            EVENT_END_DATE4,
                                            1,
                                            0,
                                            0,
                                            new Metrics(
                                                    0,
                                                    0,
                                                    0
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place2,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE2,
                                            EVENT_END_DATE2,
                                            1,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    0,
                                                    0
                                            )
                                    )
                            )
                    ),
                    new Place(),
                    4,
                    2,
                    Collections.min(List.of(EVENT_START_DATE0, EVENT_START_DATE2, EVENT_START_DATE3, EVENT_START_DATE4)),
                    Collections.max(List.of(EVENT_END_DATE0, EVENT_END_DATE2, EVENT_END_DATE3, EVENT_END_DATE4)),
                    5,
                    2,
                    2,
                    2,
                    1,
                    0
            );
            EventPlaceStatistics expected1 = createEventPlaceStatistics(
                    List.of(
                            new EventPlaceMetrics(
                                    place0,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE0,
                                            EVENT_END_DATE0,
                                            2,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    1,
                                                    0
                                            )
                                    )
                            )
                    ),
                    new Place(),
                    1,
                    1,
                    EVENT_START_DATE0,
                    EVENT_END_DATE0,
                    2,
                    1,
                    1,
                    1,
                    1,
                    0
            );
            EventPlaceStatistics expected2 = createEventPlaceStatistics(
                    List.of(),
                    new Place(),
                    0,
                    0,
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );
            EventPlaceStatistics expected3 = createEventPlaceStatistics(
                    List.of(
                            new EventPlaceMetrics(
                                    place0,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE3,
                                            EVENT_END_DATE3,
                                            1,
                                            0,
                                            0,
                                            new Metrics(
                                                    0,
                                                    0,
                                                    0
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place1,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE4,
                                            EVENT_END_DATE4,
                                            1,
                                            0,
                                            0,
                                            new Metrics(
                                                    0,
                                                    0,
                                                    0
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place2,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE2,
                                            EVENT_END_DATE2,
                                            1,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    0,
                                                    0
                                            )
                                    )
                            )
                    ),
                    new Place(),
                    3,
                    1,
                    Collections.min(List.of(EVENT_START_DATE2, EVENT_START_DATE3, EVENT_START_DATE4)),
                    Collections.max(List.of(EVENT_END_DATE2, EVENT_END_DATE3, EVENT_END_DATE4)),
                    3,
                    1,
                    1,
                    1,
                    0,
                    0
            );
            EventPlaceStatistics expected4 = createEventPlaceStatistics(
                    List.of(
                            new EventPlaceMetrics(
                                    place1,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE1,
                                            EVENT_END_DATE1,
                                            1,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    0,
                                                    1
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place2,
                                    1,
                                    1,
                                    new EventTypeEventMetrics(
                                            EVENT_START_DATE5,
                                            EVENT_END_DATE5,
                                            1,
                                            0,
                                            0,
                                            new Metrics(
                                                    0,
                                                    0,
                                                    0
                                            )
                                    )
                            )
                    ),
                    new Place(),
                    2,
                    2,
                    Collections.min(List.of(EVENT_START_DATE1, EVENT_START_DATE5)),
                    Collections.max(List.of(EVENT_END_DATE1, EVENT_END_DATE5)),
                    2,
                    1,
                    1,
                    1,
                    0,
                    1
            );
            EventPlaceStatistics expected5 = createEventPlaceStatistics(
                    List.of(
                            new EventPlaceMetrics(
                                    place0,
                                    2,
                                    2,
                                    new EventTypeEventMetrics(
                                            Collections.min(List.of(EVENT_START_DATE0, EVENT_START_DATE3)),
                                            Collections.max(List.of(EVENT_END_DATE0, EVENT_END_DATE3)),
                                            3,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    1,
                                                    0
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place1,
                                    2,
                                    2,
                                    new EventTypeEventMetrics(
                                            Collections.min(List.of(EVENT_START_DATE1, EVENT_START_DATE4)),
                                            Collections.max(List.of(EVENT_END_DATE1, EVENT_END_DATE4)),
                                            2,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    0,
                                                    1
                                            )
                                    )
                            ),
                            new EventPlaceMetrics(
                                    place2,
                                    2,
                                    2,
                                    new EventTypeEventMetrics(
                                            Collections.min(List.of(EVENT_START_DATE2, EVENT_START_DATE5)),
                                            Collections.max(List.of(EVENT_END_DATE2, EVENT_END_DATE5)),
                                            2,
                                            1,
                                            1,
                                            new Metrics(
                                                    1,
                                                    0,
                                                    0
                                            )
                                    )
                            )
                    ),
                    new Place(),
                    6,
                    4,
                    Collections.min(List.of(EVENT_START_DATE0, EVENT_START_DATE1, EVENT_START_DATE2, EVENT_START_DATE3, EVENT_START_DATE4, EVENT_START_DATE5)),
                    Collections.max(List.of(EVENT_END_DATE0, EVENT_END_DATE1, EVENT_END_DATE2, EVENT_END_DATE3, EVENT_END_DATE4, EVENT_END_DATE5)),
                    7,
                    3,
                    3,
                    3,
                    1,
                    1
            );

            return Stream.of(
                    arguments(true, false, null, null, expected0),
                    arguments(true, false, null, 0L, expected1),
                    arguments(true, false, null, 1L, expected2),
                    arguments(true, false, null, 2L, expected3),
                    arguments(true, false, null, 3L, expected2),

                    arguments(true, false, 0L, null, expected1),
                    arguments(true, false, 1L, null, expected3),

                    arguments(true, false, 0L, 0L, expected1),
                    arguments(true, false, 1L, 0L, expected2),

                    arguments(true, false, 0L, 1L, expected2),
                    arguments(true, false, 1L, 1L, expected2),

                    arguments(true, false, 0L, 2L, expected2),
                    arguments(true, false, 1L, 2L, expected3),

                    arguments(true, false, 0L, 3L, expected2),
                    arguments(true, false, 1L, 3L, expected2),

                    arguments(false, false, null, null, expected2),
                    arguments(false, true, null, null, expected4),
                    arguments(true, true, null, null, expected5)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventPlaceStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId,
                                     EventPlaceStatistics expected) {
            assertEquals(expected, statisticsService.getEventPlaceStatistics(isConferences, isMeetups, organizerId, eventTypeId));
        }
    }

    private SpeakerStatistics createSpeakerStatistics(List<SpeakerMetrics> speakerMetricsList, Speaker speaker,
                                                      long talksQuantity, long eventsQuantity, long eventTypesQuantity,
                                                      long javaChampionsQuantity, long mvpsQuantity) {
        return new SpeakerStatistics(
                speakerMetricsList,
                new SpeakerMetrics(
                        speaker,
                        talksQuantity,
                        eventsQuantity,
                        eventTypesQuantity,
                        javaChampionsQuantity,
                        mvpsQuantity)
        );
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerStatistics method with parameters tests")
    class GetSpeakerStatisticsTest {
        private Stream<Arguments> data() {
            SpeakerMetrics speakerMetrics0 = new SpeakerMetrics(
                    speaker0,
                    1,
                    1,
                    1,
                    1,
                    0);
            SpeakerMetrics speakerMetrics1 = new SpeakerMetrics(
                    speaker1,
                    1,
                    1,
                    1,
                    0,
                    1);
            SpeakerMetrics speakerMetrics2 = new SpeakerMetrics(
                    speaker2,
                    1,
                    1,
                    1,
                    0,
                    0);

            return Stream.of(
                    arguments(false, false, null, null, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, null, createSpeakerStatistics(
                            List.of(speakerMetrics1),
                            new Speaker(),
                            1, 2, 3, 0, 1
                    )),
                    arguments(true, false, null, null, createSpeakerStatistics(
                            List.of(speakerMetrics0, speakerMetrics2),
                            new Speaker(),
                            2, 4, 2, 1, 0
                    )),
                    arguments(true, true, null, null, createSpeakerStatistics(
                            List.of(speakerMetrics0, speakerMetrics1, speakerMetrics2),
                            new Speaker(),
                            3, 6, 5, 1, 1
                    )),

                    arguments(false, false, null, 0L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 0L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(true, false, null, 0L, createSpeakerStatistics(
                            List.of(speakerMetrics0),
                            new Speaker(),
                            1, 1, 1, 1, 0
                    )),
                    arguments(true, true, null, 0L, createSpeakerStatistics(
                            List.of(speakerMetrics0),
                            new Speaker(),
                            1, 1, 1, 1, 0
                    )),

                    arguments(false, false, null, 1L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 1L, createSpeakerStatistics(
                            List.of(speakerMetrics1),
                            new Speaker(),
                            1, 1, 1, 0, 1
                    )),
                    arguments(true, false, null, 1L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(true, true, null, 1L, createSpeakerStatistics(
                            List.of(speakerMetrics1),
                            new Speaker(),
                            1, 1, 1, 0, 1
                    )),

                    arguments(false, false, null, 2L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 2L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(true, false, null, 2L, createSpeakerStatistics(
                            List.of(speakerMetrics2),
                            new Speaker(),
                            1, 3, 1, 0, 0
                    )),
                    arguments(true, true, null, 2L, createSpeakerStatistics(
                            List.of(speakerMetrics2),
                            new Speaker(),
                            1, 3, 1, 0, 0
                    )),

                    arguments(false, false, null, 42L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 42L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(true, false, null, 42L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    )),
                    arguments(true, true, null, 42L, createSpeakerStatistics(
                            Collections.emptyList(),
                            new Speaker(),
                            0, 0, 0, 0, 0
                    ))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId, SpeakerStatistics expected) {
            assertEquals(expected, statisticsService.getSpeakerStatistics(isConferences, isMeetups, organizerId, eventTypeId));
        }
    }

    private CompanyStatistics createCompanyStatistics(List<CompanyMetrics> companyMetricsList, Company company,
                                                      long speakersQuantity, long talksQuantity, long eventsQuantity,
                                                      long eventTypesQuantity, long javaChampionsQuantity, long mvpsQuantity) {
        return new CompanyStatistics(
                companyMetricsList,
                new CompanyMetrics(
                        company,
                        speakersQuantity,
                        talksQuantity,
                        eventsQuantity,
                        eventTypesQuantity,
                        javaChampionsQuantity,
                        mvpsQuantity)
        );
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getCompanyStatistics method with parameters tests")
    class GetCompanyStatisticsTest {
        private Stream<Arguments> data() {
            CompanyMetrics companyMetrics0 = new CompanyMetrics(
                    company0,
                    1,
                    1,
                    1,
                    1,
                    1,
                    0);
            CompanyMetrics companyMetrics1 = new CompanyMetrics(
                    company1,
                    1,
                    1,
                    1,
                    1,
                    0,
                    1);
            CompanyMetrics companyMetrics2 = new CompanyMetrics(
                    company2,
                    1,
                    1,
                    1,
                    1,
                    0,
                    0);

            return Stream.of(
                    arguments(false, false, null, null, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, null, createCompanyStatistics(
                            List.of(companyMetrics1),
                            new Company(),
                            1, 1, 1, 1, 0, 1
                    )),
                    arguments(true, false, null, null, createCompanyStatistics(
                            List.of(companyMetrics0, companyMetrics2),
                            new Company(),
                            2, 2, 2, 2, 1, 0
                    )),
                    arguments(true, true, null, null, createCompanyStatistics(
                            List.of(companyMetrics0, companyMetrics1, companyMetrics2),
                            new Company(),
                            3, 3, 3, 3, 1, 1
                    )),

                    arguments(false, false, null, 0L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 0L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(true, false, null, 0L, createCompanyStatistics(
                            List.of(companyMetrics0),
                            new Company(),
                            1, 1, 1, 1, 1, 0
                    )),
                    arguments(true, true, null, 0L, createCompanyStatistics(
                            List.of(companyMetrics0),
                            new Company(),
                            1, 1, 1, 1, 1, 0
                    )),

                    arguments(false, false, null, 1L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 1L, createCompanyStatistics(
                            List.of(companyMetrics1),
                            new Company(),
                            1, 1, 1, 1, 0, 1
                    )),
                    arguments(true, false, null, 1L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(true, true, null, 1L, createCompanyStatistics(
                            List.of(companyMetrics1),
                            new Company(),
                            1, 1, 1, 1, 0, 1
                    )),

                    arguments(false, false, null, 2L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 2L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(true, false, null, 2L, createCompanyStatistics(
                            List.of(companyMetrics2),
                            new Company(),
                            1, 1, 1, 1, 0, 0
                    )),
                    arguments(true, true, null, 2L, createCompanyStatistics(
                            List.of(companyMetrics2),
                            new Company(),
                            1, 1, 1, 1, 0, 0
                    )),

                    arguments(false, false, null, 3L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(false, true, null, 3L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(true, false, null, 3L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    )),
                    arguments(true, true, null, 3L, createCompanyStatistics(
                            Collections.emptyList(),
                            new Company(),
                            0, 0, 0, 0, 0, 0
                    ))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getCompanyStatistics(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId, CompanyStatistics expected) {
            assertEquals(expected, statisticsService.getCompanyStatistics(isConferences, isMeetups, organizerId, eventTypeId));
        }
    }
}
