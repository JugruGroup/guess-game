package guess.service;

import guess.dao.EventDao;
import guess.dao.EventTypeDao;
import guess.domain.Conference;
import guess.domain.auxiliary.EventDateMinStartTime;
import guess.domain.auxiliary.EventMinStartTimeEndDayTime;
import guess.domain.source.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("EventServiceImpl class tests")
@ExtendWith(SpringExtension.class)
class EventServiceImplTest {
    private static final LocalDate NOW_DATE;

    private static final LocalDate EVENT_START_DATE0;
    private static final LocalDate EVENT_END_DATE0;

    private static final LocalDate EVENT_START_DATE1;
    private static final LocalDate EVENT_END_DATE1;

    private static final LocalDate EVENT_START_DATE2;
    private static final LocalDate EVENT_END_DATE2;

    private static final LocalDate EVENT_START_DATE4;

    private static final LocalDate EVENT_END_DATE5;

    private static final LocalDate EVENT_START_DATE6;
    private static final LocalDate EVENT_END_DATE6;

    private static final LocalTime TALK_TRACK_TIME1;
    private static final LocalTime TALK_TRACK_TIME2;

    private static EventType eventType0;
    private static EventType eventType1;
    private static EventType eventType2;
    private static Event event0;
    private static Event event1;
    private static Event event2;
    private static Event event3;
    private static Event event4;
    private static Event event5;
    private static Event event6;

    static {
        NOW_DATE = LocalDate.now();

        EVENT_START_DATE0 = NOW_DATE.minus(3, ChronoUnit.DAYS);
        EVENT_END_DATE0 = EVENT_START_DATE0.plus(1, ChronoUnit.DAYS);

        EVENT_START_DATE1 = NOW_DATE.plus(3, ChronoUnit.DAYS);
        EVENT_END_DATE1 = EVENT_START_DATE1;

        EVENT_START_DATE2 = NOW_DATE.plus(7, ChronoUnit.DAYS);
        EVENT_END_DATE2 = EVENT_START_DATE2;

        EVENT_START_DATE4 = NOW_DATE.plus(8, ChronoUnit.DAYS);

        EVENT_END_DATE5 = EVENT_START_DATE4;

        EVENT_START_DATE6 = NOW_DATE.plus(10, ChronoUnit.DAYS);
        EVENT_END_DATE6 = EVENT_START_DATE6.minus(1, ChronoUnit.DAYS);

        TALK_TRACK_TIME1 = LocalTime.of(9, 0);
        TALK_TRACK_TIME2 = LocalTime.of(11, 30);
    }

    @BeforeAll
    static void init() {
        Organizer organizer0 = new Organizer();
        organizer0.setId(0);

        Organizer organizer1 = new Organizer();
        organizer1.setId(1);

        eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setConference(Conference.JPOINT);
        eventType0.setOrganizer(organizer0);

        eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizer(organizer1);

        eventType2 = new EventType();
        eventType2.setId(2);
        eventType2.setConference(Conference.JOKER);
        eventType2.setOrganizer(organizer1);

        EventType eventType3 = new EventType();
        eventType3.setId(3);
        eventType3.setConference(Conference.HEISENBUG);

        EventType eventType4 = new EventType();
        eventType4.setId(4);
        eventType4.setConference(Conference.DOT_NEXT);

        EventType eventType5 = new EventType();
        eventType5.setId(2);
        eventType5.setConference(Conference.HOLY_JS);

        EventType eventType6 = new EventType();
        eventType6.setId(6);
        eventType6.setConference(Conference.CPP_RUSSIA);

        Talk talk0 = new Talk();
        talk0.setId(0);

        Talk talk1 = new Talk();
        talk1.setId(1);
        talk1.setTalkDay(1L);
        talk1.setStartTime(TALK_TRACK_TIME1);

        Talk talk2 = new Talk();
        talk2.setId(2);
        talk2.setTalkDay(1L);
        talk2.setStartTime(TALK_TRACK_TIME2);

        event0 = new Event();
        event0.setId(0);
        event0.setEventType(eventType0);
        event0.setDays(List.of(new EventDays(
                EVENT_START_DATE0,
                EVENT_END_DATE0,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event0.setTalks(List.of(talk0));

        event1 = new Event();
        event1.setId(1);
        event1.setEventType(eventType1);
        event1.setDays(List.of(new EventDays(
                EVENT_START_DATE1,
                EVENT_END_DATE1,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event1.setTalks(List.of(talk1));

        event2 = new Event();
        event2.setId(2);
        event2.setEventType(eventType2);
        event2.setDays(List.of(new EventDays(
                EVENT_START_DATE2,
                EVENT_END_DATE2,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event2.setTalks(List.of(talk2));

        event3 = new Event();
        event3.setId(3);
        event3.setEventType(eventType3);
        event3.setDays(List.of(new EventDays(
                null,
                null,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event3.setTalks(List.of(talk2));

        event4 = new Event();
        event4.setId(4);
        event4.setEventType(eventType4);
        event4.setDays(List.of(new EventDays(
                EVENT_START_DATE4,
                null,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event4.setTalks(List.of(talk2));

        event5 = new Event();
        event5.setId(5);
        event5.setEventType(eventType5);
        event5.setDays(List.of(new EventDays(
                null,
                EVENT_END_DATE5,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event5.setTalks(List.of(talk2));

        event6 = new Event();
        event6.setId(6);
        event6.setEventType(eventType6);
        event6.setDays(List.of(new EventDays(
                EVENT_START_DATE6,
                EVENT_END_DATE6,
                new Place(
                        0,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null
                )
        )));
        event6.setTalks(List.of(talk2));

        eventType0.setEvents(List.of(event0));
        eventType1.setEvents(List.of(event1));
        eventType2.setEvents(List.of(event2));
    }

    @Test
    void getEventById() {
        EventDao eventDao = Mockito.mock(EventDao.class);
        EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
        EventService eventService = new EventServiceImpl(eventDao, eventTypeDao);

        eventService.getEventById(0);
        Mockito.verify(eventDao, VerificationModeFactory.times(1)).getEventById(0);
        Mockito.verifyNoMoreInteractions(eventDao);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEvents method tests")
    class GetEventsTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(false, false, null, null, Collections.emptyList()),
                    arguments(false, false, 0L, null, Collections.emptyList()),
                    arguments(false, false, 1L, null, Collections.emptyList()),
                    arguments(false, true, null, null, List.of(event1)),
                    arguments(false, true, 0L, null, Collections.emptyList()),
                    arguments(false, true, 1L, null, List.of(event1)),
                    arguments(true, false, null, null, List.of(event0, event2)),
                    arguments(true, false, 0L, null, List.of(event0)),
                    arguments(true, false, 1L, null, List.of(event2)),
                    arguments(true, true, null, null, List.of(event0, event1, event2)),
                    arguments(true, true, 0L, null, List.of(event0)),
                    arguments(true, true, 1L, null, List.of(event1, event2)),

                    arguments(false, false, null, 0L, Collections.emptyList()),
                    arguments(false, false, 0L, 0L, Collections.emptyList()),
                    arguments(false, false, 1L, 0L, Collections.emptyList()),
                    arguments(false, true, null, 0L, Collections.emptyList()),
                    arguments(false, true, 0L, 0L, Collections.emptyList()),
                    arguments(false, true, 1L, 0L, Collections.emptyList()),
                    arguments(true, false, null, 0L, List.of(event0)),
                    arguments(true, false, 0L, 0L, List.of(event0)),
                    arguments(true, false, 1L, 0L, Collections.emptyList()),
                    arguments(true, true, null, 0L, List.of(event0)),
                    arguments(true, true, 0L, 0L, List.of(event0)),
                    arguments(true, true, 1L, 0L, Collections.emptyList()),

                    arguments(false, false, null, 1L, Collections.emptyList()),
                    arguments(false, false, 0L, 1L, Collections.emptyList()),
                    arguments(false, false, 1L, 1L, Collections.emptyList()),
                    arguments(false, true, null, 1L, List.of(event1)),
                    arguments(false, true, 0L, 1L, Collections.emptyList()),
                    arguments(false, true, 1L, 1L, List.of(event1)),
                    arguments(true, false, null, 1L, Collections.emptyList()),
                    arguments(true, false, 0L, 1L, Collections.emptyList()),
                    arguments(true, false, 1L, 1L, Collections.emptyList()),
                    arguments(true, true, null, 1L, List.of(event1)),
                    arguments(true, true, 0L, 1L, Collections.emptyList()),
                    arguments(true, true, 1L, 1L, List.of(event1)),

                    arguments(false, false, null, 2L, Collections.emptyList()),
                    arguments(false, false, 0L, 2L, Collections.emptyList()),
                    arguments(false, false, 1L, 2L, Collections.emptyList()),
                    arguments(false, true, null, 2L, Collections.emptyList()),
                    arguments(false, true, 0L, 2L, Collections.emptyList()),
                    arguments(false, true, 1L, 2L, Collections.emptyList()),
                    arguments(true, false, null, 2L, List.of(event2)),
                    arguments(true, false, 0L, 2L, Collections.emptyList()),
                    arguments(true, false, 1L, 2L, List.of(event2)),
                    arguments(true, true, null, 2L, List.of(event2)),
                    arguments(true, true, 0L, 2L, Collections.emptyList()),
                    arguments(true, true, 1L, 2L, List.of(event2)),

                    arguments(false, false, null, 3L, Collections.emptyList()),
                    arguments(false, false, 0L, 3L, Collections.emptyList()),
                    arguments(false, false, 1L, 3L, Collections.emptyList()),
                    arguments(false, true, null, 3L, Collections.emptyList()),
                    arguments(false, true, 0L, 3L, Collections.emptyList()),
                    arguments(false, true, 1L, 3L, Collections.emptyList()),
                    arguments(true, false, null, 3L, Collections.emptyList()),
                    arguments(true, false, 0L, 3L, Collections.emptyList()),
                    arguments(true, false, 1L, 3L, Collections.emptyList()),
                    arguments(true, true, null, 3L, Collections.emptyList()),
                    arguments(true, true, 0L, 3L, Collections.emptyList()),
                    arguments(true, true, 1L, 3L, Collections.emptyList())
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEvents(boolean isConferences, boolean isMeetups, Long organizerId, Long eventTypeId, List<Event> expected) {
            EventDao eventDao = Mockito.mock(EventDao.class);
            EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
            EventService eventService = new EventServiceImpl(eventDao, eventTypeDao);

            Mockito.when(eventTypeDao.getEventTypes()).thenReturn(List.of(eventType0, eventType1, eventType2));

            assertEquals(expected, eventService.getEvents(isConferences, isMeetups, organizerId, eventTypeId));
        }
    }

    @Test
    void getDefaultEvent() {
        EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class);
        final boolean IS_CONFERENCES = Boolean.TRUE;
        final boolean IS_MEETUPS = Boolean.FALSE;

        Mockito.doCallRealMethod().when(eventService).getDefaultEvent(IS_CONFERENCES, IS_MEETUPS);

        eventService.getDefaultEvent(IS_CONFERENCES, IS_MEETUPS);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEvent(IS_CONFERENCES, IS_MEETUPS);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEvent(Mockito.eq(IS_CONFERENCES), Mockito.eq(IS_MEETUPS), Mockito.any(LocalDateTime.class));
        Mockito.verifyNoMoreInteractions(eventService);
    }

    @Test
    void getDefaultEventPart() {
        EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class);
        final boolean IS_CONFERENCES = Boolean.TRUE;
        final boolean IS_MEETUPS = Boolean.FALSE;

        Mockito.doCallRealMethod().when(eventService).getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS);

        eventService.getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEventPart(Mockito.eq(IS_CONFERENCES), Mockito.eq(IS_MEETUPS), Mockito.any(LocalDateTime.class));
        Mockito.verifyNoMoreInteractions(eventService);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventsFromDateTime method tests")
    class GetEventsFromDateTimeTest {
        private Stream<Arguments> data() {
            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setConference(Conference.JPOINT);

            EventType eventType1 = new EventType();
            eventType1.setId(1);

            Event event0 = new Event();
            event0.setId(0);
            event0.setEventType(eventType0);

            Event event1 = new Event();
            event1.setId(1);
            event1.setEventType(eventType1);

            return Stream.of(
                    arguments(false, false, null, List.of(event0, event1), Collections.emptyList()),
                    arguments(false, true, null, List.of(event0, event1), List.of(event1)),
                    arguments(true, false, null, List.of(event0, event1), List.of(event0)),
                    arguments(true, true, null, List.of(event0, event1), List.of(event0, event1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventsFromDateTime(boolean isConferences, boolean isMeetups, LocalDateTime dateTime,
                                   List<Event> eventsFromDateTime, List<Event> expected) {
            EventDao eventDao = Mockito.mock(EventDao.class);
            EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
            EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class, Mockito.withSettings().useConstructor(eventDao, eventTypeDao));

            Mockito.when(eventDao.getEventsFromDateTime(Mockito.any())).thenReturn(eventsFromDateTime);

            Mockito.doCallRealMethod().when(eventService).getEventsFromDateTime(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());

            assertEquals(expected, eventService.getEventsFromDateTime(isConferences, isMeetups, dateTime));
        }
    }

    @Test
    void testGetDefaultEventPart() {
        EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class);
        final boolean IS_CONFERENCES = Boolean.TRUE;
        final boolean IS_MEETUPS = Boolean.FALSE;
        final LocalDateTime DATE_TIME = LocalDateTime.now();

        Mockito.doCallRealMethod().when(eventService).getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS, DATE_TIME);
        Mockito.when(eventService.getDefaultEvent(IS_CONFERENCES, IS_MEETUPS, DATE_TIME)).thenReturn(new Event());

        eventService.getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS, DATE_TIME);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEventPart(IS_CONFERENCES, IS_MEETUPS, DATE_TIME);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getDefaultEvent(IS_CONFERENCES, IS_MEETUPS, DATE_TIME);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).getEventPartFromEvent(Mockito.any(Event.class), Mockito.eq(DATE_TIME));
        Mockito.verifyNoMoreInteractions(eventService);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventPartFromEvent method tests")
    class GetEventPartFromEventTest {
        private Stream<Arguments> data() {
            final LocalDate DATE0 = LocalDate.of(2022, 6, 23);
            final LocalDate DATE1 = LocalDate.of(2022, 6, 24);
            final LocalDateTime DATE_TIME0 = LocalDateTime.of(2022, 7, 1, 0, 0, 0);
            final LocalDateTime DATE_TIME1 = LocalDateTime.of(2022, 6, 1, 0, 0, 0);

            Place place0 = new Place();

            EventDays eventDays0 = new EventDays(
                    DATE0,
                    DATE1,
                    place0
            );

            ZoneId zoneId0 = ZoneId.of("Europe/Moscow");

            Event event0 = new Event();
            event0.setId(0);

            Event event1 = new Event();
            event1.setId(1);
            event1.setDays(Collections.emptyList());

            Event event2 = new Event();
            event2.setId(2);
            event2.setDays(List.of(eventDays0));
            event2.setTimeZoneId(zoneId0);

            EventPart eventPart0 = new EventPart(
                    2,
                    new EventType(),
                    DATE0,
                    DATE1
            );

            return Stream.of(
                    arguments(null, null, null),
                    arguments(null, DATE_TIME0, null),
                    arguments(event0, null, null),
                    arguments(event0, DATE_TIME0, null),
                    arguments(event1, null, null),
                    arguments(event1, DATE_TIME0, null),
                    arguments(event2, DATE_TIME0, null),
                    arguments(event2, DATE_TIME1, eventPart0)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventPartFromEvent(Event event, LocalDateTime dateTime, EventPart expected) {
            EventDao eventDao = Mockito.mock(EventDao.class);
            EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
            EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class, Mockito.withSettings().useConstructor(eventDao, eventTypeDao));

            Mockito.doCallRealMethod().when(eventService).getEventPartFromEvent(Mockito.nullable(Event.class), Mockito.any(LocalDateTime.class));

            assertEquals(expected, eventService.getEventPartFromEvent(event, dateTime));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getDefaultEvent method tests")
    class GetDefaultEventTest {
        private Stream<Arguments> data() {
            final boolean IS_CONFERENCES = Boolean.TRUE;
            final boolean IS_MEETUPS = Boolean.TRUE;
            final LocalDateTime DATE_TIME = LocalDateTime.of(2021, 2, 7, 10, 0);

            Event event0 = new Event();
            event0.setId(0);

            Event event1 = new Event();
            event1.setId(1);

            EventDateMinStartTime eventDateMinStartTime0 = new EventDateMinStartTime(
                    event0,
                    LocalDate.of(2021, 2, 7),
                    LocalTime.of(10, 0)
            );

            EventMinStartTimeEndDayTime eventMinStartTimeEndDayTime0 = new EventMinStartTimeEndDayTime(
                    event0,
                    LocalDateTime.of(2021, 2, 7, 10, 0),
                    LocalDateTime.of(2021, 2, 8, 0, 0)
            );

            EventMinStartTimeEndDayTime eventMinStartTimeEndDayTime1 = new EventMinStartTimeEndDayTime(
                    event1,
                    LocalDateTime.of(2021, 2, 7, 12, 0),
                    LocalDateTime.of(2021, 2, 8, 0, 0)
            );

            return Stream.of(
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME, Collections.emptyList(), null, null, null),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME, List.of(event0), Collections.emptyList(), null, null),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME, List.of(event0), List.of(eventDateMinStartTime0), Collections.emptyList(), null),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME.plus(1, ChronoUnit.DAYS),
                            List.of(event0), List.of(eventDateMinStartTime0), List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1), null),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME.minus(1, ChronoUnit.DAYS),
                            List.of(event0), List.of(eventDateMinStartTime0), List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1), event0),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME,
                            List.of(event0), List.of(eventDateMinStartTime0), List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1), event0),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME.plus(1, ChronoUnit.HOURS),
                            List.of(event0), List.of(eventDateMinStartTime0), List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1), event0),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME.plus(2, ChronoUnit.HOURS),
                            List.of(event0), List.of(eventDateMinStartTime0), List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1), event1),
                    arguments(IS_CONFERENCES, IS_MEETUPS, DATE_TIME.plus(3, ChronoUnit.HOURS),
                            List.of(event0), List.of(eventDateMinStartTime0), List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1), event1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getDefaultEvent(boolean isConferences, boolean isMeetups, LocalDateTime dateTime, List<Event> eventsFromDateTime,
                             List<EventDateMinStartTime> eventDateMinStartTimeList,
                             List<EventMinStartTimeEndDayTime> eventMinStartTimeEndDayTimeList, Event expected) {
            EventDao eventDao = Mockito.mock(EventDao.class);
            EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
            EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class, Mockito.withSettings().useConstructor(eventDao, eventTypeDao));

            Mockito.doCallRealMethod().when(eventService).getDefaultEvent(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any());
            Mockito.when(eventService.getEventsFromDateTime(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any())).thenReturn(eventsFromDateTime);
            Mockito.when(eventService.getEventDateMinTrackTimeList(Mockito.any())).thenReturn(eventDateMinStartTimeList);
            Mockito.when(eventService.getEventMinTrackTimeEndDayTimeList(Mockito.any())).thenReturn(eventMinStartTimeEndDayTimeList);

            assertEquals(expected, eventService.getDefaultEvent(isConferences, isMeetups, dateTime));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventDateMinTrackTimeList method tests")
    class GetEventDateMinTrackTimeListTest {
        private Stream<Arguments> data() {
            EventDateMinStartTime eventDateMinStartTime0 = new EventDateMinStartTime(
                    event0,
                    EVENT_START_DATE0,
                    LocalTime.of(0, 0)
            );

            EventDateMinStartTime eventDateMinStartTime1 = new EventDateMinStartTime(
                    event0,
                    EVENT_START_DATE0.plus(1, ChronoUnit.DAYS),
                    LocalTime.of(0, 0)
            );

            EventDateMinStartTime eventDateMinStartTime2 = new EventDateMinStartTime(
                    event1,
                    EVENT_START_DATE1,
                    TALK_TRACK_TIME1
            );

            EventDateMinStartTime eventDateMinStartTime3 = new EventDateMinStartTime(
                    event2,
                    EVENT_START_DATE2,
                    TALK_TRACK_TIME2
            );

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(event0), List.of(eventDateMinStartTime0, eventDateMinStartTime1)),
                    arguments(List.of(event0, event1), List.of(eventDateMinStartTime0, eventDateMinStartTime1, eventDateMinStartTime2)),
                    arguments(List.of(event0, event1, event2), List.of(eventDateMinStartTime0, eventDateMinStartTime1, eventDateMinStartTime2, eventDateMinStartTime3)),
                    arguments(List.of(event3, event4, event5, event6), Collections.emptyList()),
                    arguments(List.of(event0, event1, event2, event3, event4, event5, event6), List.of(eventDateMinStartTime0, eventDateMinStartTime1, eventDateMinStartTime2, eventDateMinStartTime3))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getConferenceDateMinTrackTimeList(List<Event> events, List<EventDateMinStartTime> expected) {
            EventDao eventDao = Mockito.mock(EventDao.class);
            EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
            EventServiceImpl eventService = new EventServiceImpl(eventDao, eventTypeDao);

            assertEquals(expected, eventService.getEventDateMinTrackTimeList(events));
        }
    }

    @Test
    void getEventMinTrackTimeEndDayTimeList() {
        Event event0 = new Event();
        event0.setId(0);
        event0.setTimeZoneId(ZoneId.of("Europe/Moscow"));

        Event event1 = new Event();
        event1.setId(1);
        event1.setTimeZoneId(ZoneId.of("Asia/Novosibirsk"));

        EventDateMinStartTime eventDateMinStartTime0 = new EventDateMinStartTime(
                event0,
                LocalDate.of(2021, 2, 4),
                LocalTime.of(15, 0)
        );

        EventDateMinStartTime eventDateMinStartTime1 = new EventDateMinStartTime(
                event1,
                LocalDate.of(2021, 2, 4),
                LocalTime.of(15, 0)
        );

        EventDateMinStartTime eventDateMinStartTime2 = new EventDateMinStartTime(
                event0,
                LocalDate.of(2021, 2, 4),
                LocalTime.of(0, 0)
        );

        EventMinStartTimeEndDayTime eventMinStartTimeEndDayTime0 = new EventMinStartTimeEndDayTime(
                event0,
                LocalDateTime.of(2021, 2, 4, 12, 0),
                LocalDateTime.of(2021, 2, 4, 21, 0)
        );

        EventMinStartTimeEndDayTime eventMinStartTimeEndDayTime1 = new EventMinStartTimeEndDayTime(
                event1,
                LocalDateTime.of(2021, 2, 4, 8, 0),
                LocalDateTime.of(2021, 2, 4, 17, 0)
        );

        EventMinStartTimeEndDayTime eventMinStartTimeEndDayTime2 = new EventMinStartTimeEndDayTime(
                event0,
                LocalDateTime.of(2021, 2, 3, 21, 0),
                LocalDateTime.of(2021, 2, 4, 21, 0)
        );

        EventDao eventDao = Mockito.mock(EventDao.class);
        EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
        EventServiceImpl eventService = new EventServiceImpl(eventDao, eventTypeDao);

        assertEquals(
                List.of(eventMinStartTimeEndDayTime0, eventMinStartTimeEndDayTime1, eventMinStartTimeEndDayTime2),
                eventService.getEventMinTrackTimeEndDayTimeList(List.of(eventDateMinStartTime0, eventDateMinStartTime1, eventDateMinStartTime2)));
    }

    @Test
    void getEventByTalk() {
        Talk talk0 = new Talk();
        talk0.setId(0);

        EventDao eventDao = Mockito.mock(EventDao.class);
        EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
        EventService eventService = new EventServiceImpl(eventDao, eventTypeDao);

        eventService.getEventByTalk(talk0);
        Mockito.verify(eventDao, VerificationModeFactory.times(1)).getEventByTalk(talk0);
        Mockito.verifyNoMoreInteractions(eventDao);
    }

    @Test
    void convertEventToEventParts() {
        EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class);

        Mockito.doCallRealMethod().when(eventService).convertEventToEventParts(Mockito.any(Event.class));

        Event event0 = new Event();
        event0.setId(0);
        event0.setDays(Collections.emptyList());

        eventService.convertEventToEventParts(event0);
        Mockito.verify(eventService, VerificationModeFactory.times(1)).convertEventToEventParts(Mockito.any(Event.class));
        Mockito.verifyNoMoreInteractions(eventService);
    }

    @Test
    void convertEventsToEventParts() {
        EventServiceImpl eventService = Mockito.mock(EventServiceImpl.class);

        Mockito.doCallRealMethod().when(eventService).convertEventsToEventParts(Mockito.anyList());

        Event event0 = new Event();
        event0.setId(0);

        Event event1 = new Event();
        event1.setId(1);

        eventService.convertEventsToEventParts(List.of(event0, event1));
        Mockito.verify(eventService, VerificationModeFactory.times(1)).convertEventsToEventParts(Mockito.anyList());
        Mockito.verify(eventService, VerificationModeFactory.times(2)).convertEventToEventParts(Mockito.any(Event.class));
        Mockito.verifyNoMoreInteractions(eventService);
    }
}
