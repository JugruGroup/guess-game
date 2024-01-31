package guess.service;

import guess.dao.EventTypeDao;
import guess.dao.OlapDao;
import guess.dao.OlapDaoImpl;
import guess.dao.TopicDao;
import guess.domain.Conference;
import guess.domain.Identifier;
import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.statistics.olap.CubeType;
import guess.domain.statistics.olap.DimensionType;
import guess.domain.statistics.olap.MeasureType;
import guess.domain.statistics.olap.dimension.City;
import guess.domain.statistics.olap.metrics.OlapEntityMetrics;
import guess.domain.statistics.olap.metrics.OlapEntitySubMetrics;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.statistics.olap.parameters.OlapCityParametersDto;
import guess.dto.statistics.olap.parameters.OlapEventTypeParametersDto;
import guess.dto.statistics.olap.parameters.OlapParametersDto;
import guess.dto.statistics.olap.parameters.OlapSpeakerParametersDto;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("OlapServiceImpl class tests")
@ExtendWith(SpringExtension.class)
class OlapServiceImplTest {
    private static Topic topic0;
    private static Topic topic1;
    private static EventType eventType0;
    private static EventType eventType1;
    private static EventType eventType2;
    private static Speaker speaker0;
    private static Speaker speaker1;
    private static Company company0;
    private static Company company1;

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        TopicDao topicDao() {
            TopicDao topicDao = Mockito.mock(TopicDao.class);
            Mockito.when(topicDao.getTopics()).thenReturn(List.of(topic0, topic1));

            return topicDao;
        }

        @Bean
        EventTypeDao eventTypeDao() {
            EventTypeDao eventTypeDao = Mockito.mock(EventTypeDao.class);
            Mockito.when(eventTypeDao.getEventTypes()).thenReturn(List.of(eventType0, eventType1, eventType2));

            return eventTypeDao;
        }

        @Bean
        OlapDao olapDao() {
            return new OlapDaoImpl(topicDao(), eventTypeDao());
        }

        @Bean
        OlapService olapService() {
            return new OlapServiceImpl(olapDao());
        }
    }

    @Autowired
    private OlapDao olapDao;

    @Autowired
    private OlapService olapService;

    @BeforeAll
    static void init() {
        topic0 = new Topic();
        topic0.setId(0);
        topic0.setDefaultTopic(true);
        topic0.setOrderNumber(0);

        topic1 = new Topic();
        topic1.setId(1);
        topic1.setOrderNumber(1);

        Organizer organizer0 = new Organizer();
        organizer0.setId(0L);

        Organizer organizer1 = new Organizer();
        organizer1.setId(1L);

        eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setConference(Conference.JPOINT);
        eventType0.setOrganizerId(0L);
        eventType0.setOrganizer(organizer0);
        eventType0.setTopic(topic0);

        eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizerId(0L);
        eventType1.setOrganizer(organizer0);
        eventType1.setTopic(topic1);

        eventType2 = new EventType();
        eventType2.setId(2);
        eventType2.setOrganizerId(1L);
        eventType2.setOrganizer(organizer1);

        Place place0 = new Place(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City0")), Collections.emptyList(), null);
        Place place1 = new Place(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City1")), Collections.emptyList(), null);
        Place place2 = new Place(2, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City2")), Collections.emptyList(), null);

        EventDays eventDays0 = new EventDays(
                LocalDate.of(2020, 9, 5),
                LocalDate.of(2020, 9, 6),
                place0
        );

        EventDays eventDays1 = new EventDays(
                LocalDate.of(2021, 10, 7),
                LocalDate.of(2021, 10, 9),
                place1
        );

        EventDays eventDays2 = new EventDays(
                LocalDate.of(2021, 5, 1),
                LocalDate.of(2021, 5, 2),
                place2
        );

        EventDays eventDays3 = new EventDays(
                LocalDate.of(2021, 6, 10),
                LocalDate.of(2021, 6, 12),
                place2
        );

        Event event0 = new Event();
        event0.setId(0);
        event0.setEventTypeId(eventType0.getId());
        event0.setEventType(eventType0);
        eventType0.setEvents(List.of(event0));
        event0.setDays(List.of(eventDays0));

        Event event1 = new Event();
        event1.setId(1);
        event1.setEventTypeId(eventType1.getId());
        event1.setEventType(eventType1);
        eventType1.setEvents(List.of(event1));
        event1.setDays(List.of(eventDays1));

        Event event2 = new Event();
        event2.setId(2);
        event2.setEventTypeId(eventType2.getId());
        event2.setEventType(eventType2);
        eventType2.setEvents(List.of(event2));
        event2.setDays(List.of(eventDays2));

        Event event3 = new Event();
        event3.setId(3);
        event3.setDays(List.of(eventDays3));

        company0 = new Company();
        company0.setId(0);

        company1 = new Company();
        company1.setId(1);

        speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setCompanyIds(List.of(0L));
        speaker0.setCompanies(List.of(company0));
        speaker0.setJavaChampion(true);

        speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setCompanyIds(List.of(1L));
        speaker1.setCompanies(List.of(company1));
        speaker1.setMvp(true);

        Speaker speaker2 = new Speaker();
        speaker2.setId(2);
        speaker2.setCompanyIds(List.of(1L));
        speaker2.setCompanies(List.of(company1));

        Talk talk0 = new Talk();
        talk0.setId(0);
        talk0.setSpeakerIds(List.of(0L));
        talk0.setSpeakers(List.of(speaker0));
        event0.setTalkIds(List.of(0L));
        event0.setTalks(List.of(talk0));

        Talk talk1 = new Talk();
        talk1.setId(1);
        talk1.setSpeakerIds(List.of(1L));
        talk1.setSpeakers(List.of(speaker1));
        event1.setTalkIds(List.of(1L));
        event1.setTalks(List.of(talk1));

        Talk talk2 = new Talk();
        talk2.setId(2);
        talk2.setSpeakerIds(List.of(0L, 1L));
        talk2.setSpeakers(List.of(speaker0, speaker1));
        event2.setTalkIds(List.of(2L));
        event2.setTalks(List.of(talk2));
    }

    @Test
    void getMeasureTypes() {
        OlapDao olapDao = Mockito.mock(OlapDao.class);
        OlapService olapService = new OlapServiceImpl(olapDao);

        olapService.getMeasureTypes(CubeType.EVENT_TYPES);

        Mockito.verify(olapDao, VerificationModeFactory.times(1)).getMeasureTypes(CubeType.EVENT_TYPES);
        Mockito.verifyNoMoreInteractions(olapDao);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getOlapStatistics method with parameters tests")
    class GetOlapStatisticsTest {
        private Stream<Arguments> data() {
            // Year statistics
            List<Integer> yearDimensionValues0 = List.of(2020, 2021);

            City city0 = new City(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City0")));
            City city1 = new City(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City1")));
            City city2 = new City(2, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City2")));
            List<City> cityDimensionValues0 = List.of(city0, city1, city2);

            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList1 = List.of(
                    new OlapEntitySubMetrics<>(eventType0, Map.of(city0, List.of(1L, 0L)))
            );
            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList2 = List.of(
                    new OlapEntitySubMetrics<>(eventType1, Map.of(city1, List.of(0L, 1L))),
                    new OlapEntitySubMetrics<>(eventType2, Map.of(city2, List.of(0L, 1L)))
            );
            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList3 = List.of(
                    new OlapEntitySubMetrics<>(eventType0, Map.of(city0, List.of(1L, 0L))),
                    new OlapEntitySubMetrics<>(eventType1, Map.of(city1, List.of(0L, 1L))),
                    new OlapEntitySubMetrics<>(eventType2, Map.of(city2, List.of(0L, 1L)))
            );
            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList4 = List.of(
                    new OlapEntitySubMetrics<>(eventType0, Map.of(city0, List.of(1L, 0L))),
                    new OlapEntitySubMetrics<>(eventType1, Map.of(city1, List.of(0L, 1L)))
            );

            List<OlapEntitySubMetrics<Speaker, EventType>> yearEventTypeSpeakerSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Speaker, EventType>> yearEventTypeSpeakerSubMetricsList1 = List.of(
                    new OlapEntitySubMetrics<>(speaker0, Map.of(eventType0, List.of(1L, 0L), eventType2, List.of(0L, 1L))),
                    new OlapEntitySubMetrics<>(speaker1, Map.of(eventType1, List.of(0L, 1L), eventType2, List.of(0L, 1L)))
            );
            List<OlapEntitySubMetrics<Speaker, EventType>> yearEventTypeSpeakerSubMetricsList2 = List.of(
                    new OlapEntitySubMetrics<>(speaker0, Map.of(eventType0, List.of(1L, 0L), eventType2, List.of(0L, 1L)))
            );

            List<OlapEntitySubMetrics<Company, EventType>> yearEventTypeCompanySubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Company, EventType>> yearEventTypeCompanySubMetricsList1 = List.of(
                    new OlapEntitySubMetrics<>(company0, Map.of(eventType0, List.of(1L, 0L), eventType2, List.of(0L, 1L))),
                    new OlapEntitySubMetrics<>(company1, Map.of(eventType1, List.of(0L, 1L), eventType2, List.of(0L, 1L)))
            );
            List<OlapEntitySubMetrics<Company, EventType>> yearEventTypeCompanySubMetricsList2 = List.of(
                    new OlapEntitySubMetrics<>(company0, Map.of(eventType0, List.of(1L, 0L), eventType2, List.of(0L, 1L)))
            );

            List<EventType> eventTypeDimensionValues0 = List.of(eventType0, eventType1, eventType2);

            List<OlapEntityMetrics<EventType>> yearMetricsList0 = Collections.emptyList();
            OlapEntityMetrics<Void> yearTotals0 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Speaker>> yearMetricsList1 = Collections.emptyList();
            OlapEntityMetrics<Void> yearTotals1 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Company>> yearMetricsList2 = Collections.emptyList();
            OlapEntityMetrics<Void> yearTotals2 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<EventType>> yearMetricsList3 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> yearTotals3 = new OlapEntityMetrics<>(null, List.of(1L, 0L), List.of(1L, 1L), 1L);

            List<OlapEntityMetrics<EventType>> yearMetricsList4 = List.of(
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType2, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> yearTotals4 = new OlapEntityMetrics<>(null, List.of(0L, 2L), List.of(0L, 2L), 2L);

            List<OlapEntityMetrics<EventType>> yearMetricsList5 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType2, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> yearTotals5 = new OlapEntityMetrics<>(null, List.of(1L, 2L), List.of(1L, 3L), 3L);

            List<OlapEntityMetrics<EventType>> yearMetricsList6 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> yearTotals6 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            List<OlapEntityMetrics<Speaker>> yearMetricsList7 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(1L, 1L), List.of(1L, 2L), 2L),
                    new OlapEntityMetrics<>(speaker1, List.of(0L, 2L), List.of(0L, 2L), 2L)
            );
            OlapEntityMetrics<Void> yearTotals7 = new OlapEntityMetrics<>(null, List.of(1L, 2L), List.of(1L, 3L), 3L);

            List<OlapEntityMetrics<Speaker>> yearMetricsList8 = Collections.emptyList();
            OlapEntityMetrics<Void> yearTotals8 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Speaker>> yearMetricsList9 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(1L, 1L), List.of(1L, 2L), 2L)
            );
            OlapEntityMetrics<Void> yearTotals9 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            List<OlapEntityMetrics<Company>> yearMetricsList10 = List.of(
                    new OlapEntityMetrics<>(company0, List.of(1L, 1L), List.of(1L, 2L), 2L),
                    new OlapEntityMetrics<>(company1, List.of(0L, 2L), List.of(0L, 2L), 2L)
            );
            OlapEntityMetrics<Void> yearTotals10 = new OlapEntityMetrics<>(null, List.of(1L, 2L), List.of(1L, 3L), 3L);

            List<OlapEntityMetrics<Company>> yearMetricsList11 = Collections.emptyList();
            OlapEntityMetrics<Void> yearTotals11 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Company>> yearMetricsList12 = List.of(
                    new OlapEntityMetrics<>(company0, List.of(1L, 1L), List.of(1L, 2L), 2L)
            );
            OlapEntityMetrics<Void> yearTotals12 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList0, yearMetricsList0, yearTotals0);
            OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics1 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList1, yearMetricsList3, yearTotals3);
            OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics2 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList2, yearMetricsList4, yearTotals4);
            OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics3 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList3, yearMetricsList5, yearTotals5);
            OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics4 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList4, yearMetricsList6, yearTotals6);

            OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeSpeakerSubMetricsList0, yearMetricsList1, yearTotals1);
            OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics1 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeSpeakerSubMetricsList1, yearMetricsList7, yearTotals7);
            OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics2 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeSpeakerSubMetricsList0, yearMetricsList8, yearTotals8);
            OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics3 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeSpeakerSubMetricsList2, yearMetricsList9, yearTotals9);

            OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeCompanySubMetricsList0, yearMetricsList2, yearTotals2);
            OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics1 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeCompanySubMetricsList1, yearMetricsList10, yearTotals10);
            OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics2 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeCompanySubMetricsList0, yearMetricsList11, yearTotals11);
            OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics3 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeCompanySubMetricsList2, yearMetricsList12, yearTotals12);

            // Topic statistics
            List<Topic> topicDimensionValues0 = List.of(topic0, topic1);
            List<Void> voidDimensionValues0 = Collections.emptyList();
            List<OlapEntitySubMetrics<EventType, Void>> topicVoidEventTypeSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Speaker, Void>> topicVoidSpeakerSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Company, Void>> topicVoidCompanySubMetricsList0 = Collections.emptyList();

            List<OlapEntityMetrics<EventType>> topicMetricsList0 = Collections.emptyList();
            OlapEntityMetrics<Void> topicTotals0 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Speaker>> topicMetricsList1 = Collections.emptyList();
            OlapEntityMetrics<Void> topicTotals1 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Company>> topicMetricsList2 = Collections.emptyList();
            OlapEntityMetrics<Void> topicTotals2 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<EventType>> topicMetricsList3 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> topicTotals3 = new OlapEntityMetrics<>(null, List.of(1L, 0L), List.of(1L, 1L), 1L);

            List<OlapEntityMetrics<EventType>> topicMetricsList4 = List.of(
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType2, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> topicTotals4 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            List<OlapEntityMetrics<EventType>> topicMetricsList5 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType2, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> topicTotals5 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            List<OlapEntityMetrics<EventType>> topicMetricsList6 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> topicTotals6 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            List<OlapEntityMetrics<Speaker>> topicMetricsList7 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(2L, 0L), List.of(2L, 2L), 2L),
                    new OlapEntityMetrics<>(speaker1, List.of(1L, 1L), List.of(1L, 2L), 2L)
            );
            OlapEntityMetrics<Void> topicTotals7 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            List<OlapEntityMetrics<Speaker>> topicMetricsList8 = Collections.emptyList();
            OlapEntityMetrics<Void> topicTotals8 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Speaker>> topicMetricsList9 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );
            OlapEntityMetrics<Void> topicTotals9 = new OlapEntityMetrics<>(null, List.of(2L, 0L), List.of(2L, 2L), 2L);

            List<OlapEntityMetrics<Company>> topicMetricsList10 = List.of(
                    new OlapEntityMetrics<>(company0, List.of(2L, 0L), List.of(2L, 2L), 2L),
                    new OlapEntityMetrics<>(company1, List.of(1L, 1L), List.of(1L, 2L), 2L)
            );
            OlapEntityMetrics<Void> topicTotals10 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            List<OlapEntityMetrics<Company>> topicMetricsList11 = Collections.emptyList();
            OlapEntityMetrics<Void> topicTotals11 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Company>> topicMetricsList12 = List.of(
                    new OlapEntityMetrics<>(company0, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );
            OlapEntityMetrics<Void> topicTotals12 = new OlapEntityMetrics<>(null, List.of(2L, 0L), List.of(2L, 2L), 2L);

            OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics0 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidEventTypeSubMetricsList0, topicMetricsList0, topicTotals0);
            OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics1 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidEventTypeSubMetricsList0, topicMetricsList3, topicTotals3);
            OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics2 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidEventTypeSubMetricsList0, topicMetricsList4, topicTotals4);
            OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics3 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidEventTypeSubMetricsList0, topicMetricsList5, topicTotals5);
            OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics4 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidEventTypeSubMetricsList0, topicMetricsList6, topicTotals6);

            OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics0 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidSpeakerSubMetricsList0, topicMetricsList1, topicTotals1);
            OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics1 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidSpeakerSubMetricsList0, topicMetricsList7, topicTotals7);
            OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics2 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidSpeakerSubMetricsList0, topicMetricsList8, topicTotals8);
            OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics3 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidSpeakerSubMetricsList0, topicMetricsList9, topicTotals9);

            OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics0 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidCompanySubMetricsList0, topicMetricsList2, topicTotals2);
            OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics1 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidCompanySubMetricsList0, topicMetricsList10, topicTotals10);
            OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics2 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidCompanySubMetricsList0, topicMetricsList11, topicTotals11);
            OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics3 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidCompanySubMetricsList0, topicMetricsList12, topicTotals12);

            OlapStatistics expected0 = new OlapStatistics(yearEventTypeStatistics0, null, null, topicEventTypeStatistics0, null, null);
            OlapStatistics expected1 = new OlapStatistics(null, yearSpeakerStatistics0, null, null, topicSpeakerStatistics0, null);
            OlapStatistics expected2 = new OlapStatistics(null, null, yearCompanyStatistics0, null, null, topicCompanyStatistics0);
            OlapStatistics expected3 = new OlapStatistics(yearEventTypeStatistics1, null, null, topicEventTypeStatistics1, null, null);
            OlapStatistics expected4 = new OlapStatistics(yearEventTypeStatistics2, null, null, topicEventTypeStatistics2, null, null);
            OlapStatistics expected5 = new OlapStatistics(yearEventTypeStatistics3, null, null, topicEventTypeStatistics3, null, null);
            OlapStatistics expected6 = new OlapStatistics(yearEventTypeStatistics4, null, null, topicEventTypeStatistics4, null, null);
            OlapStatistics expected7 = new OlapStatistics(null, yearSpeakerStatistics1, null, null, topicSpeakerStatistics1, null);
            OlapStatistics expected8 = new OlapStatistics(null, yearSpeakerStatistics2, null, null, topicSpeakerStatistics2, null);
            OlapStatistics expected9 = new OlapStatistics(null, yearSpeakerStatistics3, null, null, topicSpeakerStatistics3, null);
            OlapStatistics expected10 = new OlapStatistics(null, null, yearCompanyStatistics1, null, null, topicCompanyStatistics1);
            OlapStatistics expected11 = new OlapStatistics(null, null, yearCompanyStatistics2, null, null, topicCompanyStatistics2);
            OlapStatistics expected12 = new OlapStatistics(null, null, yearCompanyStatistics3, null, null, topicCompanyStatistics3);

            OlapParametersDto op0 = new OlapParametersDto();
            op0.setCubeType(CubeType.EVENT_TYPES);

            OlapParametersDto op1 = new OlapParametersDto();
            op1.setCubeType(CubeType.SPEAKERS);
            op1.setMeasureType(MeasureType.EVENTS_QUANTITY);

            OlapParametersDto op2 = new OlapParametersDto();
            op2.setCubeType(CubeType.COMPANIES);
            op2.setMeasureType(MeasureType.EVENTS_QUANTITY);

            // Event types
            OlapParametersDto op3 = new OlapParametersDto();
            op3.setCubeType(CubeType.EVENT_TYPES);
            op3.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op3.setConferences(true);

            OlapParametersDto op4 = new OlapParametersDto();
            op4.setCubeType(CubeType.EVENT_TYPES);
            op4.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op4.setMeetups(true);

            OlapParametersDto op5 = new OlapParametersDto();
            op5.setCubeType(CubeType.EVENT_TYPES);
            op5.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op5.setConferences(true);
            op5.setMeetups(true);

            OlapParametersDto op6 = new OlapParametersDto();
            op6.setCubeType(CubeType.EVENT_TYPES);
            op6.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op6.setConferences(true);
            op6.setMeetups(true);
            op6.setOrganizerId(42L);

            OlapParametersDto op7 = new OlapParametersDto();
            op7.setCubeType(CubeType.EVENT_TYPES);
            op7.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op7.setConferences(true);
            op7.setMeetups(true);
            op7.setOrganizerId(0L);

            OlapParametersDto op8 = new OlapParametersDto();
            op8.setCubeType(CubeType.EVENT_TYPES);
            op8.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op8.setConferences(true);
            op8.setMeetups(true);
            op8.setEventTypeIds(Collections.emptyList());

            OlapParametersDto op9 = new OlapParametersDto();
            op9.setCubeType(CubeType.EVENT_TYPES);
            op9.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op9.setConferences(true);
            op9.setMeetups(true);
            op9.setEventTypeIds(List.of(42L));

            OlapParametersDto op10 = new OlapParametersDto();
            op10.setCubeType(CubeType.EVENT_TYPES);
            op10.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op10.setConferences(true);
            op10.setMeetups(true);
            op10.setEventTypeIds(List.of(0L));

            // Speakers
            OlapParametersDto op11 = new OlapParametersDto();
            op11.setCubeType(CubeType.SPEAKERS);
            op11.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op11.setConferences(true);
            op11.setMeetups(true);

            OlapParametersDto op12 = new OlapParametersDto();
            op12.setCubeType(CubeType.SPEAKERS);
            op12.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op12.setConferences(true);
            op12.setMeetups(true);
            op12.setSpeakerIds(Collections.emptyList());

            OlapParametersDto op13 = new OlapParametersDto();
            op13.setCubeType(CubeType.SPEAKERS);
            op13.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op13.setConferences(true);
            op13.setMeetups(true);
            op13.setSpeakerIds(List.of(42L));

            OlapParametersDto op14 = new OlapParametersDto();
            op14.setCubeType(CubeType.SPEAKERS);
            op14.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op14.setConferences(true);
            op14.setMeetups(true);
            op14.setSpeakerIds(List.of(0L));

            // Companies
            OlapParametersDto op15 = new OlapParametersDto();
            op15.setCubeType(CubeType.COMPANIES);
            op15.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op15.setConferences(true);
            op15.setMeetups(true);

            OlapParametersDto op16 = new OlapParametersDto();
            op16.setCubeType(CubeType.COMPANIES);
            op16.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op16.setConferences(true);
            op16.setMeetups(true);
            op16.setCompanyIds(Collections.emptyList());

            OlapParametersDto op17 = new OlapParametersDto();
            op17.setCubeType(CubeType.COMPANIES);
            op17.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op17.setConferences(true);
            op17.setMeetups(true);
            op17.setCompanyIds(List.of(42L));

            OlapParametersDto op18 = new OlapParametersDto();
            op18.setCubeType(CubeType.COMPANIES);
            op18.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op18.setConferences(true);
            op18.setMeetups(true);
            op18.setCompanyIds(List.of(0L));

            return Stream.of(
                    arguments(op0, expected0),
                    arguments(op1, expected1),
                    arguments(op2, expected2),

                    arguments(op3, expected3),
                    arguments(op4, expected4),
                    arguments(op5, expected5),
                    arguments(op6, expected0),
                    arguments(op7, expected6),
                    arguments(op8, expected5),
                    arguments(op9, expected0),
                    arguments(op10, expected3),

                    arguments(op11, expected7),
                    arguments(op12, expected7),
                    arguments(op13, expected8),
                    arguments(op14, expected9),

                    arguments(op15, expected10),
                    arguments(op16, expected10),
                    arguments(op17, expected11),
                    arguments(op18, expected12)
            );
        }

        private <T extends Identifier> void sortDimensionValues2(OlapEntityStatistics<?, T, ?> olapEntityStatistics) {
            List<T> sortedDimensionValues2 = olapEntityStatistics.getDimensionValues2().stream()
                    .sorted(Comparator.comparing(Identifier::getId))
                    .toList();
            olapEntityStatistics.setDimensionValues2(sortedDimensionValues2);
        }

        private <T extends Identifier, S> void sortSubMetricsList(OlapEntityStatistics<?, S, T> olapEntityStatistics) {
            List<OlapEntitySubMetrics<T, S>> sortedSubMetricsList = olapEntityStatistics.getSubMetricsList().stream()
                    .sorted(Comparator.comparing(m -> m.entity().getId()))
                    .toList();
            olapEntityStatistics.setSubMetricsList(sortedSubMetricsList);
        }

        private <T extends Identifier> void sortMetricsList(OlapEntityStatistics<?, ?, T> olapEntityStatistics) {
            List<OlapEntityMetrics<T>> sortedMetricsList = olapEntityStatistics.getMetricsList().stream()
                    .sorted(Comparator.comparing(m -> m.entity().getId()))
                    .toList();
            olapEntityStatistics.setMetricsList(sortedMetricsList);
        }

        @ParameterizedTest
        @MethodSource("data")
        void getOlapStatistics(OlapParametersDto op, OlapStatistics expected) {
            OlapStatistics actual = olapService.getOlapStatistics(op);

            if (actual.yearEventTypeStatistics() != null) {
                sortDimensionValues2(actual.yearEventTypeStatistics());
                sortSubMetricsList(actual.yearEventTypeStatistics());
                sortMetricsList(actual.yearEventTypeStatistics());
            }

            if (actual.topicEventTypeStatistics() != null) {
                sortMetricsList(actual.topicEventTypeStatistics());
            }

            if (actual.yearSpeakerStatistics() != null) {
                sortDimensionValues2(actual.yearSpeakerStatistics());
                sortSubMetricsList(actual.yearSpeakerStatistics());
                sortMetricsList(actual.yearSpeakerStatistics());
            }

            if (actual.topicSpeakerStatistics() != null) {
                sortMetricsList(actual.topicSpeakerStatistics());
            }

            if (actual.yearCompanyStatistics() != null) {
                sortDimensionValues2(actual.yearCompanyStatistics());
                sortSubMetricsList(actual.yearCompanyStatistics());
                sortMetricsList(actual.yearCompanyStatistics());
            }

            if (actual.topicCompanyStatistics() != null) {
                sortMetricsList(actual.topicCompanyStatistics());
            }

            assertEquals(expected, actual);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getOlapEventTypeStatistics method with parameters tests")
    class GetOlapEventTypeStatisticsTest {
        private Stream<Arguments> data() {
            List<Integer> dimensionValues0 = List.of(2020, 2021);
            List<Void> voidDimensionValues0 = Collections.emptyList();
            List<OlapEntitySubMetrics<EventType, Void>> yearVoidEventTypeSubMetricsList0 = Collections.emptyList();

            List<OlapEntityMetrics<EventType>> metricsList0 = Collections.emptyList();
            OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<EventType>> metricsList1 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals1 = new OlapEntityMetrics<>(null, List.of(1L, 0L), List.of(1L, 1L), 1L);

            List<OlapEntityMetrics<EventType>> metricsList2 = List.of(
                    new OlapEntityMetrics<>(eventType2, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals2 = new OlapEntityMetrics<>(null, List.of(0L, 1L), List.of(0L, 1L), 1L);

            List<OlapEntityMetrics<EventType>> metricsList3 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals3 = new OlapEntityMetrics<>(null, List.of(1L, 0L), List.of(1L, 1L), 1L);

            OlapEventTypeParametersDto op0 = new OlapEventTypeParametersDto();
            op0.setCubeType(CubeType.EVENT_TYPES);

            OlapEventTypeParametersDto op1 = new OlapEventTypeParametersDto();
            op1.setCubeType(CubeType.SPEAKERS);
            op1.setMeasureType(MeasureType.EVENTS_QUANTITY);

            OlapEventTypeParametersDto op2 = new OlapEventTypeParametersDto();
            op2.setCubeType(CubeType.SPEAKERS);
            op2.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op2.setConferences(true);

            OlapEventTypeParametersDto op3 = new OlapEventTypeParametersDto();
            op3.setCubeType(CubeType.SPEAKERS);
            op3.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op3.setConferences(true);
            op3.setSpeakerId(42L);

            OlapEventTypeParametersDto op4 = new OlapEventTypeParametersDto();
            op4.setCubeType(CubeType.SPEAKERS);
            op4.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op4.setConferences(true);
            op4.setSpeakerId(0L);

            OlapEventTypeParametersDto op5 = new OlapEventTypeParametersDto();
            op5.setCubeType(CubeType.SPEAKERS);
            op5.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op5.setMeetups(true);
            op5.setSpeakerId(42L);

            OlapEventTypeParametersDto op6 = new OlapEventTypeParametersDto();
            op6.setCubeType(CubeType.SPEAKERS);
            op6.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op6.setMeetups(true);
            op6.setSpeakerId(0L);

            OlapEventTypeParametersDto op7 = new OlapEventTypeParametersDto();
            op7.setCubeType(CubeType.SPEAKERS);
            op7.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op7.setConferences(true);
            op7.setMeetups(true);
            op7.setOrganizerId(42L);
            op7.setSpeakerId(0L);

            OlapEventTypeParametersDto op8 = new OlapEventTypeParametersDto();
            op8.setCubeType(CubeType.SPEAKERS);
            op8.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op8.setConferences(true);
            op8.setMeetups(true);
            op8.setOrganizerId(0L);
            op8.setSpeakerId(0L);

            OlapEventTypeParametersDto op9 = new OlapEventTypeParametersDto();
            op9.setCubeType(CubeType.SPEAKERS);
            op9.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op9.setConferences(true);
            op9.setMeetups(true);
            op9.setOrganizerId(0L);
            op9.setEventTypeIds(Collections.emptyList());
            op9.setSpeakerId(0L);

            OlapEventTypeParametersDto op10 = new OlapEventTypeParametersDto();
            op10.setCubeType(CubeType.SPEAKERS);
            op10.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op10.setConferences(true);
            op10.setMeetups(true);
            op10.setEventTypeIds(List.of(42L));
            op10.setSpeakerId(0L);

            OlapEventTypeParametersDto op11 = new OlapEventTypeParametersDto();
            op11.setCubeType(CubeType.SPEAKERS);
            op11.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op11.setConferences(true);
            op11.setMeetups(true);
            op11.setEventTypeIds(List.of(0L));
            op11.setSpeakerId(0L);

            OlapEventTypeParametersDto op12 = new OlapEventTypeParametersDto();
            op12.setCubeType(CubeType.COMPANIES);
            op12.setMeasureType(MeasureType.EVENTS_QUANTITY);

            OlapEventTypeParametersDto op13 = new OlapEventTypeParametersDto();
            op13.setCubeType(CubeType.COMPANIES);
            op13.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op13.setConferences(true);

            OlapEventTypeParametersDto op14 = new OlapEventTypeParametersDto();
            op14.setCubeType(CubeType.COMPANIES);
            op14.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op14.setConferences(true);
            op14.setCompanyId(42L);

            OlapEventTypeParametersDto op15 = new OlapEventTypeParametersDto();
            op15.setCubeType(CubeType.COMPANIES);
            op15.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op15.setConferences(true);
            op15.setCompanyId(0L);

            OlapEventTypeParametersDto op16 = new OlapEventTypeParametersDto();
            op16.setCubeType(CubeType.COMPANIES);
            op16.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op16.setMeetups(true);
            op16.setCompanyId(42L);

            OlapEventTypeParametersDto op17 = new OlapEventTypeParametersDto();
            op17.setCubeType(CubeType.COMPANIES);
            op17.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op17.setMeetups(true);
            op17.setCompanyId(0L);

            OlapEventTypeParametersDto op18 = new OlapEventTypeParametersDto();
            op18.setCubeType(CubeType.COMPANIES);
            op18.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op18.setConferences(true);
            op18.setMeetups(true);
            op18.setOrganizerId(42L);
            op18.setCompanyId(0L);

            OlapEventTypeParametersDto op19 = new OlapEventTypeParametersDto();
            op19.setCubeType(CubeType.COMPANIES);
            op19.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op19.setConferences(true);
            op19.setMeetups(true);
            op19.setOrganizerId(0L);
            op19.setCompanyId(0L);

            OlapEventTypeParametersDto op20 = new OlapEventTypeParametersDto();
            op20.setCubeType(CubeType.COMPANIES);
            op20.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op20.setConferences(true);
            op20.setMeetups(true);
            op20.setOrganizerId(0L);
            op20.setEventTypeIds(Collections.emptyList());
            op20.setCompanyId(0L);

            OlapEventTypeParametersDto op21 = new OlapEventTypeParametersDto();
            op21.setCubeType(CubeType.COMPANIES);
            op21.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op21.setConferences(true);
            op21.setMeetups(true);
            op21.setEventTypeIds(List.of(42L));
            op21.setCompanyId(0L);

            OlapEventTypeParametersDto op22 = new OlapEventTypeParametersDto();
            op22.setCubeType(CubeType.COMPANIES);
            op22.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op22.setConferences(true);
            op22.setMeetups(true);
            op22.setEventTypeIds(List.of(0L));
            op22.setCompanyId(0L);

            OlapEntityStatistics<Integer, Void, EventType> expected0 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidEventTypeSubMetricsList0, metricsList0, totals0);
            OlapEntityStatistics<Integer, Void, EventType> expected1 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidEventTypeSubMetricsList0, metricsList1, totals1);
            OlapEntityStatistics<Integer, Void, EventType> expected2 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidEventTypeSubMetricsList0, metricsList2, totals2);
            OlapEntityStatistics<Integer, Void, EventType> expected3 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidEventTypeSubMetricsList0, metricsList3, totals3);

            return Stream.of(
                    arguments(op0, IllegalArgumentException.class, null),

                    arguments(op1, null, expected0),
                    arguments(op2, null, expected0),
                    arguments(op3, null, expected0),
                    arguments(op4, null, expected1),
                    arguments(op5, null, expected0),
                    arguments(op6, null, expected2),
                    arguments(op7, null, expected0),
                    arguments(op8, null, expected3),
                    arguments(op9, null, expected3),
                    arguments(op10, null, expected0),
                    arguments(op11, null, expected3),

                    arguments(op12, null, expected0),
                    arguments(op13, null, expected0),
                    arguments(op14, null, expected0),
                    arguments(op15, null, expected1),
                    arguments(op16, null, expected0),
                    arguments(op17, null, expected2),
                    arguments(op18, null, expected0),
                    arguments(op19, null, expected3),
                    arguments(op20, null, expected3),
                    arguments(op21, null, expected0),
                    arguments(op22, null, expected3)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getOlapEventTypeStatistics(OlapEventTypeParametersDto op, Class<? extends Throwable> expectedException,
                                        OlapEntityStatistics<Integer, Void, EventType> expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, olapService.getOlapEventTypeStatistics(op));
            } else {
                assertThrows(expectedException, () -> olapService.getOlapEventTypeStatistics(op));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getOlapSpeakerStatistics method with parameters tests")
    class GetOlapSpeakerStatisticsTest {
        private Stream<Arguments> data() {
            List<Integer> dimensionValues0 = List.of(2020, 2021);
            List<Void> voidDimensionValues0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Speaker, Void>> yearVoidSpeakerSubMetricsList0 = Collections.emptyList();

            List<OlapEntityMetrics<Speaker>> metricsList0 = Collections.emptyList();
            OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<Speaker>> metricsList1 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals1 = new OlapEntityMetrics<>(null, List.of(1L, 0L), List.of(1L, 1L), 1L);

            List<OlapEntityMetrics<Speaker>> metricsList2 = List.of(
                    new OlapEntityMetrics<>(speaker1, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals2 = new OlapEntityMetrics<>(null, List.of(0L, 1L), List.of(0L, 1L), 1L);

            OlapSpeakerParametersDto op0 = new OlapSpeakerParametersDto();
            op0.setCubeType(CubeType.SPEAKERS);
            op0.setMeasureType(MeasureType.EVENTS_QUANTITY);

            OlapSpeakerParametersDto op1 = new OlapSpeakerParametersDto();
            op1.setCubeType(CubeType.SPEAKERS);
            op1.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op1.setEventTypeId(42L);

            OlapSpeakerParametersDto op2 = new OlapSpeakerParametersDto();
            op2.setCubeType(CubeType.SPEAKERS);
            op2.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op2.setEventTypeId(0L);

            OlapSpeakerParametersDto op3 = new OlapSpeakerParametersDto();
            op3.setCubeType(CubeType.SPEAKERS);
            op3.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op3.setCompanyId(42L);

            OlapSpeakerParametersDto op4 = new OlapSpeakerParametersDto();
            op4.setCubeType(CubeType.SPEAKERS);
            op4.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op4.setCompanyId(0L);

            OlapSpeakerParametersDto op5 = new OlapSpeakerParametersDto();
            op5.setCubeType(CubeType.SPEAKERS);
            op5.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op5.setCompanyId(0L);
            op5.setEventTypeId(42L);

            OlapSpeakerParametersDto op6 = new OlapSpeakerParametersDto();
            op6.setCubeType(CubeType.SPEAKERS);
            op6.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op6.setCompanyId(42L);
            op6.setEventTypeId(0L);

            OlapSpeakerParametersDto op7 = new OlapSpeakerParametersDto();
            op7.setCubeType(CubeType.SPEAKERS);
            op7.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op7.setCompanyId(0L);
            op7.setEventTypeId(0L);

            OlapSpeakerParametersDto op8 = new OlapSpeakerParametersDto();
            op8.setCubeType(CubeType.SPEAKERS);
            op8.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op8.setCompanyId(1L);
            op8.setEventTypeId(1L);

            OlapEntityStatistics<Integer, Void, Speaker> expected0 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidSpeakerSubMetricsList0, metricsList0, totals0);
            OlapEntityStatistics<Integer, Void, Speaker> expected1 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidSpeakerSubMetricsList0, metricsList1, totals1);
            OlapEntityStatistics<Integer, Void, Speaker> expected2 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidSpeakerSubMetricsList0, metricsList2, totals2);

            return Stream.of(
                    arguments(op0, expected0),
                    arguments(op1, expected0),
                    arguments(op2, expected0),
                    arguments(op3, expected0),
                    arguments(op4, expected0),
                    arguments(op5, expected0),
                    arguments(op6, expected0),
                    arguments(op7, expected1),
                    arguments(op8, expected2)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getOlapSpeakerStatistics(OlapSpeakerParametersDto op, OlapEntityStatistics<Integer, Void, Speaker> expected) {
            assertEquals(expected, olapService.getOlapSpeakerStatistics(op));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getOlapCityStatistics method with parameters tests")
    class GetOlapCityStatisticsTest {
        private Stream<Arguments> data() {
            List<Integer> dimensionValues0 = List.of(2020, 2021);
            List<Void> voidDimensionValues0 = Collections.emptyList();
            List<OlapEntitySubMetrics<City, Void>> yearVoidCitySubMetricsList0 = Collections.emptyList();

            City city0 = new City(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City0")));
            City city1 = new City(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City1")));

            List<OlapEntityMetrics<City>> metricsList0 = Collections.emptyList();
            OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(0L, 0L), List.of(0L, 0L), 0L);

            List<OlapEntityMetrics<City>> metricsList1 = List.of(
                    new OlapEntityMetrics<>(city0, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals1 = new OlapEntityMetrics<>(null, List.of(1L, 0L), List.of(1L, 1L), 1L);

            List<OlapEntityMetrics<City>> metricsList2 = List.of(
                    new OlapEntityMetrics<>(city1, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            OlapEntityMetrics<Void> totals2 = new OlapEntityMetrics<>(null, List.of(0L, 1L), List.of(0L, 1L), 1L);

            OlapCityParametersDto op0 = new OlapCityParametersDto();
            op0.setCubeType(CubeType.EVENT_TYPES);
            op0.setMeasureType(MeasureType.EVENTS_QUANTITY);

            OlapCityParametersDto op1 = new OlapCityParametersDto();
            op1.setCubeType(CubeType.EVENT_TYPES);
            op1.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op1.setEventTypeId(42L);

            OlapCityParametersDto op2 = new OlapCityParametersDto();
            op2.setCubeType(CubeType.EVENT_TYPES);
            op2.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op2.setEventTypeId(0L);

            OlapCityParametersDto op3 = new OlapCityParametersDto();
            op3.setCubeType(CubeType.EVENT_TYPES);
            op3.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op3.setEventTypeId(1L);

            OlapEntityStatistics<Integer, Void, City> expected0 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidCitySubMetricsList0, metricsList0, totals0);
            OlapEntityStatistics<Integer, Void, City> expected1 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidCitySubMetricsList0, metricsList1, totals1);
            OlapEntityStatistics<Integer, Void, City> expected2 = new OlapEntityStatistics<>(dimensionValues0, voidDimensionValues0, yearVoidCitySubMetricsList0, metricsList2, totals2);

            return Stream.of(
                    arguments(op0, expected0),
                    arguments(op1, expected0),
                    arguments(op2, expected1),
                    arguments(op3, expected2)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getOlapCityStatistics(OlapCityParametersDto op, OlapEntityStatistics<Integer, Void, City> expected) {
            assertEquals(expected, olapService.getOlapCityStatistics(op));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getOlapEntityStatistics method with parameters tests")
    class GetOlapEntityStatisticsTest {
        private Stream<Arguments> data() {
            OlapParametersDto op0 = new OlapParametersDto();
            op0.setCubeType(CubeType.EVENT_TYPES);
            op0.setMeasureType(MeasureType.EVENTS_QUANTITY);
            op0.setConferences(true);
            op0.setMeetups(true);

            OlapParametersDto op1 = new OlapParametersDto();
            op1.setCubeType(CubeType.EVENT_TYPES);
            op1.setMeasureType(MeasureType.DURATION);
            op1.setConferences(true);
            op1.setMeetups(false);

            List<Integer> yearDimensionValues0 = List.of(2020, 2021);

            City city0 = new City(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City0")));
            City city1 = new City(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City1")));
            City city2 = new City(2, List.of(new LocaleItem(Language.ENGLISH.getCode(), "City2")));
            List<City> cityDimensionValues0 = List.of(city0, city1, city2);

            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList0 = List.of(
                    new OlapEntitySubMetrics<>(eventType0, Map.of(city0, List.of(1L, 0L))),
                    new OlapEntitySubMetrics<>(eventType1, Map.of(city1, List.of(0L, 1L))),
                    new OlapEntitySubMetrics<>(eventType2, Map.of(city2, List.of(0L, 1L)))
            );
            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList1 = List.of(
                    new OlapEntitySubMetrics<>(eventType0, Map.of(city0, List.of(2L, 0L)))
            );

            List<OlapEntityMetrics<EventType>> metricsList0 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(1L, 0L), List.of(1L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType2, List.of(0L, 1L), List.of(0L, 1L), 1L)
            );
            List<OlapEntityMetrics<EventType>> metricsList1 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );

            OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(1L, 2L), List.of(1L, 3L), 3L);
            OlapEntityMetrics<Void> totals1 = new OlapEntityMetrics<>(null, List.of(2L, 0L), List.of(2L, 2L), 2L);

            OlapEntityStatistics<Integer, City, EventType> expected0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList0, metricsList0, totals0);
            OlapEntityStatistics<Integer, City, EventType> expected1 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList1, metricsList1, totals1);

            return Stream.of(
                    arguments(op0, expected0),
                    arguments(op1, expected1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getOlapEntityStatistics(OlapParametersDto op, OlapEntityStatistics<Integer, City, EventType> expected) {
            OlapServiceImpl olapServiceImpl = new OlapServiceImpl(olapDao);
            Predicate<EventType> eventTypePredicate = et ->
                    ((op.isConferences() && et.isEventTypeConference()) || (op.isMeetups() && !et.isEventTypeConference())) &&
                            ((op.getOrganizerId() == null) || (et.getOrganizer().getId() == op.getOrganizerId())) &&
                            ((op.getEventTypeIds() == null) || op.getEventTypeIds().isEmpty() || op.getEventTypeIds().contains(et.getId()));
            OlapEntityStatistics<Integer, City, EventType> actual = olapServiceImpl.getOlapEntityStatistics(
                    op.getCubeType(), op.getMeasureType(), DimensionType.EVENT_TYPE, eventTypePredicate,
                    DimensionType.YEAR, DimensionType.CITY, DimensionType.EVENT_TYPE, eventTypePredicate);

            List<City> sortedDimensionValues2 = actual.getDimensionValues2().stream()
                    .sorted(Comparator.comparing(Identifier::getId))
                    .toList();
            actual.setDimensionValues2(sortedDimensionValues2);

            List<OlapEntitySubMetrics<EventType, City>> sortedSubMetricsList = actual.getSubMetricsList().stream()
                    .sorted(Comparator.comparing(m -> m.entity().getId()))
                    .toList();
            actual.setSubMetricsList(sortedSubMetricsList);

            List<OlapEntityMetrics<EventType>> sortedMetricsList = actual.getMetricsList().stream()
                    .sorted(Comparator.comparing(m -> m.entity().getId()))
                    .toList();
            actual.setMetricsList(sortedMetricsList);

            assertEquals(expected, actual);
        }
    }
}
