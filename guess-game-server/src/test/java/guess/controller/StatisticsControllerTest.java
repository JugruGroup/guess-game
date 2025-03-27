package guess.controller;

import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.statistics.EventTypeEventMetrics;
import guess.domain.statistics.Metrics;
import guess.domain.statistics.company.CompanyMetrics;
import guess.domain.statistics.company.CompanyStatistics;
import guess.domain.statistics.event.EventMetrics;
import guess.domain.statistics.event.EventStatistics;
import guess.domain.statistics.eventtype.EventTypeMetrics;
import guess.domain.statistics.eventtype.EventTypeStatistics;
import guess.domain.statistics.olap.CubeType;
import guess.domain.statistics.olap.metrics.OlapEntityMetrics;
import guess.domain.statistics.olap.metrics.OlapEntitySubMetrics;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.domain.statistics.speaker.SpeakerMetrics;
import guess.domain.statistics.speaker.SpeakerStatistics;
import guess.dto.statistics.olap.parameters.OlapCityParametersDto;
import guess.dto.statistics.olap.parameters.OlapEventTypeParametersDto;
import guess.dto.statistics.olap.parameters.OlapParametersDto;
import guess.dto.statistics.olap.parameters.OlapSpeakerParametersDto;
import guess.service.OlapService;
import guess.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("StatisticsController class tests")
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @MockitoBean
    private OlapService olapService;

    @Test
    void getEventTypeStatistics() throws Exception {
        boolean conferences = true;
        boolean meetups = false;

        Organizer organizer0 = new Organizer(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        Organizer organizer1 = new Organizer(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer1);

        EventType eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizer(organizer0);

        EventTypeMetrics eventTypeMetrics0 = new EventTypeMetrics(
                eventType0,
                4, 1,
                new EventTypeEventMetrics(
                        LocalDate.of(2016, 11, 1), LocalDate.of(2016, 11, 1),
                        4, 20, 10, new Metrics(21, 3, 0))
        );

        EventTypeMetrics eventTypeMetrics1 = new EventTypeMetrics(
                eventType1,
                2, 4,
                new EventTypeEventMetrics(
                        LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2),
                        10, 40, 25, new Metrics(61, 5, 1))
        );

        EventTypeMetrics eventTypeMetricsTotals = new EventTypeMetrics(
                new EventType(),
                4, 5,
                new EventTypeEventMetrics(LocalDate.of(2016, 11, 1), LocalDate.of(2018, 1, 2),
                        14, 60, 30, new Metrics(82, 7, 1))
        );

        EventTypeStatistics eventTypeStatistics = new EventTypeStatistics(
                List.of(eventTypeMetrics0, eventTypeMetrics1),
                eventTypeMetricsTotals);

        given(statisticsService.getEventTypeStatistics(conferences, meetups, null, null)).willReturn(eventTypeStatistics);

        mvc.perform(get("/api/statistics/event-type-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", Boolean.toString(conferences))
                        .param("meetups", Boolean.toString(meetups))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTypeMetricsList", hasSize(2)))
                .andExpect(jsonPath("$.eventTypeMetricsList[0].id", is(1)))
                .andExpect(jsonPath("$.eventTypeMetricsList[1].id", is(0)))
                .andExpect(jsonPath("$.totals.age", is(4)));
        Mockito.verify(statisticsService, VerificationModeFactory.times(1)).getEventTypeStatistics(conferences, meetups, null, null);
    }

    @Test
    void getEventStatistics() throws Exception {
        boolean conferences = true;
        boolean meetups = false;
        long organizerId = 0L;
        long eventTypeId = 0L;

        Event event0 = new Event();
        event0.setId(0);

        Event event1 = new Event();
        event1.setId(1);

        EventMetrics eventMetrics0 = new EventMetrics(
                event0,
                new EventTypeEventMetrics(
                        LocalDate.of(2016, 11, 1), LocalDate.of(2016, 11, 1),
                        4, 20, 10, new Metrics(21, 3, 0))
        );

        EventMetrics eventMetrics1 = new EventMetrics(
                event1,
                new EventTypeEventMetrics(
                        LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 2),
                        10, 40, 25, new Metrics(61, 5, 1))
        );

        EventMetrics eventMetricsTotals = new EventMetrics(
                new Event(),
                new EventTypeEventMetrics(
                        LocalDate.of(2016, 11, 1), LocalDate.of(2018, 1, 2),
                        14, 60, 30, new Metrics(81, 5, 1))
        );

        EventStatistics eventStatistics = new EventStatistics(
                List.of(eventMetrics0, eventMetrics1),
                eventMetricsTotals);

        given(statisticsService.getEventStatistics(conferences, meetups, organizerId, eventTypeId)).willReturn(eventStatistics);

        mvc.perform(get("/api/statistics/event-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", Boolean.toString(conferences))
                        .param("meetups", Boolean.toString(meetups))
                        .param("organizerId", Long.toString(organizerId))
                        .param("eventTypeId", Long.toString(eventTypeId))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventMetricsList", hasSize(2)))
                .andExpect(jsonPath("$.eventMetricsList[0].id", is(0)))
                .andExpect(jsonPath("$.eventMetricsList[1].id", is(1)))
                .andExpect(jsonPath("$.totals.duration", is(14)));
        Mockito.verify(statisticsService, VerificationModeFactory.times(1)).getEventStatistics(conferences, meetups, organizerId, eventTypeId);
    }

    @Test
    void getSpeakerStatistics() throws Exception {
        boolean conferences = true;
        boolean meetups = false;
        Long organizerId = null;
        Long eventTypeId = 0L;

        Speaker speaker0 = new Speaker();
        speaker0.setId(0);

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);

        SpeakerMetrics speakerMetrics0 = new SpeakerMetrics(
                speaker0, 21, 15, 10, 1, 0);

        SpeakerMetrics speakerMetrics1 = new SpeakerMetrics(
                speaker1, 61, 20, 15, 0, 1);

        SpeakerMetrics speakerMetricsTotals = new SpeakerMetrics(
                new Speaker(),
                82, 35, 25, 1, 1);

        SpeakerStatistics speakerStatistics = new SpeakerStatistics(
                List.of(speakerMetrics0, speakerMetrics1),
                speakerMetricsTotals);

        given(statisticsService.getSpeakerStatistics(conferences, meetups, organizerId, eventTypeId)).willReturn(speakerStatistics);

        mvc.perform(get("/api/statistics/speaker-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", Boolean.toString(conferences))
                        .param("meetups", Boolean.toString(meetups))
                        .param("eventTypeId", Long.toString(eventTypeId))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speakerMetricsList", hasSize(2)))
                .andExpect(jsonPath("$.speakerMetricsList[0].id", is(1)))
                .andExpect(jsonPath("$.speakerMetricsList[1].id", is(0)))
                .andExpect(jsonPath("$.totals.talksQuantity", is(82)));
        Mockito.verify(statisticsService, VerificationModeFactory.times(1)).getSpeakerStatistics(conferences, meetups, organizerId, eventTypeId);
    }

    @Test
    void getCompanyStatistics() throws Exception {
        boolean conferences = true;
        boolean meetups = false;
        Long organizerId = null;
        Long eventTypeId = 0L;

        Company company0 = new Company();
        company0.setId(0);

        Company company1 = new Company();
        company1.setId(1);

        CompanyMetrics companyMetrics0 = new CompanyMetrics(
                company0, 20, 21, 15, 10, 1, 0);

        CompanyMetrics companyMetrics1 = new CompanyMetrics(
                company1, 42, 61, 20, 15, 0, 1);

        CompanyMetrics companyMetricsTotals = new CompanyMetrics(
                new Company(),
                60, 82, 35, 25, 1, 1);

        CompanyStatistics companyStatistics = new CompanyStatistics(
                List.of(companyMetrics0, companyMetrics1),
                companyMetricsTotals);

        given(statisticsService.getCompanyStatistics(conferences, meetups, organizerId, eventTypeId)).willReturn(companyStatistics);

        mvc.perform(get("/api/statistics/company-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("conferences", Boolean.toString(conferences))
                        .param("meetups", Boolean.toString(meetups))
                        .param("eventTypeId", Long.toString(eventTypeId))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyMetricsList", hasSize(2)))
                .andExpect(jsonPath("$.companyMetricsList[0].id", is(1)))
                .andExpect(jsonPath("$.companyMetricsList[1].id", is(0)))
                .andExpect(jsonPath("$.totals.speakersQuantity", is(60)));
        Mockito.verify(statisticsService, VerificationModeFactory.times(1)).getCompanyStatistics(conferences, meetups, organizerId, eventTypeId);
    }

    @Test
    void getCubeTypes() throws Exception {
        mvc.perform(get("/api/statistics/cube-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("EVENT_TYPES")))
                .andExpect(jsonPath("$[1]", is("SPEAKERS")))
                .andExpect(jsonPath("$[2]", is("COMPANIES")));
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getMeasureTypes method with parameters tests")
    class GetMeasureTypesTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(""),
                    arguments("EVENT_TYPES")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getMeasureTypes(String cubeType) throws Exception {
            if (cubeType.isEmpty()) {
                mvc.perform(get("/api/statistics/measure-types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("cubeType", cubeType))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));
                Mockito.verifyNoMoreInteractions(olapService);
            } else {
                mvc.perform(get("/api/statistics/measure-types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("cubeType", cubeType))
                        .andExpect(status().isOk());
                Mockito.verify(olapService, VerificationModeFactory.times(1)).getMeasureTypes(CubeType.valueOf(cubeType));
                Mockito.verifyNoMoreInteractions(olapService);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getOlapStatistics method with parameters tests")
    class GetOlapStatisticsTest {
        private Stream<Arguments> data() {
            List<Integer> yearDimensionValues0 = List.of(2020, 2021);
            List<City> cityDimensionValues0 = Collections.emptyList();
            List<EventType> eventTypeDimensionValues0 = Collections.emptyList();
            List<Void> voidDimensionValues0 = Collections.emptyList();

            Topic topic0 = new Topic();
            topic0.setId(0);

            Topic topic1 = new Topic();
            topic1.setId(1);

            List<Topic> topicDimensionValues0 = List.of(topic0, topic1);

            Organizer organizer0 = new Organizer(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            Organizer organizer1 = new Organizer(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setOrganizer(organizer1);

            EventType eventType1 = new EventType();
            eventType1.setId(1);
            eventType1.setOrganizer(organizer0);

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

            Company company0 = new Company();
            company0.setId(0);
            company0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

            Company company1 = new Company();
            company1.setId(1);
            company1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

            List<OlapEntitySubMetrics<EventType, City>> yearCityEventTypeSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Speaker, EventType>> yearEventTypeSpeakerSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Company, EventType>> yearEventTypeCompanySubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<EventType, Void>> topicVoidEventTypeSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Speaker, Void>> topicVoidSpeakerSubMetricsList0 = Collections.emptyList();
            List<OlapEntitySubMetrics<Company, Void>> topicVoidCompanySubMetricsList0 = Collections.emptyList();

            List<OlapEntityMetrics<EventType>> yearMetricsList0 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> yearTotals0 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            List<OlapEntityMetrics<Speaker>> yearMetricsList1 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(speaker1, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );
            OlapEntityMetrics<Void> yearTotals1 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            List<OlapEntityMetrics<Company>> yearMetricsList2 = List.of(
                    new OlapEntityMetrics<>(company0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(company1, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );
            OlapEntityMetrics<Void> yearTotals2 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            List<OlapEntityMetrics<EventType>> topicMetricsList0 = List.of(
                    new OlapEntityMetrics<>(eventType0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(eventType1, List.of(1L, 0L), List.of(1L, 1L), 1L)
            );
            OlapEntityMetrics<Void> topicTotals0 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

            List<OlapEntityMetrics<Speaker>> topicMetricsList1 = List.of(
                    new OlapEntityMetrics<>(speaker0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(speaker1, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );
            OlapEntityMetrics<Void> topicTotals1 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            List<OlapEntityMetrics<Company>> topicMetricsList2 = List.of(
                    new OlapEntityMetrics<>(company0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                    new OlapEntityMetrics<>(company1, List.of(2L, 0L), List.of(2L, 2L), 2L)
            );
            OlapEntityMetrics<Void> topicTotals2 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

            OlapEntityStatistics<Integer, City, EventType> yearEventTypeStatistics0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, cityDimensionValues0, yearCityEventTypeSubMetricsList0, yearMetricsList0, yearTotals0);
            OlapEntityStatistics<Integer, EventType, Speaker> yearSpeakerStatistics0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeSpeakerSubMetricsList0, yearMetricsList1, yearTotals1);
            OlapEntityStatistics<Integer, EventType, Company> yearCompanyStatistics0 = new OlapEntityStatistics<>(
                    yearDimensionValues0, eventTypeDimensionValues0, yearEventTypeCompanySubMetricsList0, yearMetricsList2, yearTotals2);
            OlapEntityStatistics<Topic, Void, EventType> topicEventTypeStatistics0 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidEventTypeSubMetricsList0, topicMetricsList0, topicTotals0);
            OlapEntityStatistics<Topic, Void, Speaker> topicSpeakerStatistics0 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidSpeakerSubMetricsList0, topicMetricsList1, topicTotals1);
            OlapEntityStatistics<Topic, Void, Company> topicCompanyStatistics0 = new OlapEntityStatistics<>(
                    topicDimensionValues0, voidDimensionValues0, topicVoidCompanySubMetricsList0, topicMetricsList2, topicTotals2);

            OlapStatistics olapStatistics0 = new OlapStatistics(
                    yearEventTypeStatistics0, null, null, topicEventTypeStatistics0, null, null);
            OlapStatistics olapStatistics1 = new OlapStatistics(
                    null, yearSpeakerStatistics0, null, null, topicSpeakerStatistics0, null);
            OlapStatistics olapStatistics2 = new OlapStatistics(
                    null, null, yearCompanyStatistics0, null, null, topicCompanyStatistics0);

            return Stream.of(
                    arguments(CubeType.EVENT_TYPES, olapStatistics0),
                    arguments(CubeType.SPEAKERS, olapStatistics1),
                    arguments(CubeType.COMPANIES, olapStatistics2)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getOlapStatistics(CubeType cubeType, OlapStatistics olapStatistics) throws Exception {
            given(olapService.getOlapStatistics(Mockito.any())).willReturn(olapStatistics);

            switch (cubeType) {
                case EVENT_TYPES -> mvc.perform(post("/api/statistics/olap-statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(JsonUtil.toJson(new OlapParametersDto()))
                                .param("language", "en"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.eventTypeStatistics.metricsList", hasSize(2)))
                        .andExpect(jsonPath("$.eventTypeStatistics.metricsList[0].id", is(1)))
                        .andExpect(jsonPath("$.eventTypeStatistics.metricsList[1].id", is(0)));
                case SPEAKERS -> mvc.perform(post("/api/statistics/olap-statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(JsonUtil.toJson(new OlapParametersDto()))
                                .param("language", "en"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.speakerStatistics.metricsList", hasSize(2)))
                        .andExpect(jsonPath("$.speakerStatistics.metricsList[0].id", is(1)))
                        .andExpect(jsonPath("$.speakerStatistics.metricsList[1].id", is(0)));
                case COMPANIES -> mvc.perform(post("/api/statistics/olap-statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(JsonUtil.toJson(new OlapParametersDto()))
                                .param("language", "en"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.companyStatistics.metricsList", hasSize(2)))
                        .andExpect(jsonPath("$.companyStatistics.metricsList[0].id", is(1)))
                        .andExpect(jsonPath("$.companyStatistics.metricsList[1].id", is(0)));
            }

            Mockito.verify(olapService, VerificationModeFactory.times(1)).getOlapStatistics(Mockito.any());
        }
    }

    @Test
    void getOlapEventTypeStatistics() throws Exception {
        Organizer organizer0 = new Organizer(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        Organizer organizer1 = new Organizer(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setOrganizer(organizer1);

        EventType eventType1 = new EventType();
        eventType1.setId(1);
        eventType1.setOrganizer(organizer0);

        List<Integer> yearDimensionValues0 = List.of(2020, 2021);
        List<Void> voidDimensionValues0 = Collections.emptyList();
        List<OlapEntitySubMetrics<EventType, Void>> yearVoidEventTypeSubMetricsList0 = Collections.emptyList();

        List<OlapEntityMetrics<EventType>> metricsList0 = List.of(
                new OlapEntityMetrics<>(eventType0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                new OlapEntityMetrics<>(eventType1, List.of(1L, 0L), List.of(1L, 1L), 1L)
        );
        OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(1L, 1L), List.of(1L, 2L), 2L);

        OlapEntityStatistics<Integer, Void, EventType> eventTypeStatistics0 =
                new OlapEntityStatistics<>(yearDimensionValues0, voidDimensionValues0, yearVoidEventTypeSubMetricsList0, metricsList0, totals0);

        given(olapService.getOlapEventTypeStatistics(Mockito.any())).willReturn(eventTypeStatistics0);

        mvc.perform(post("/api/statistics/olap-event-type-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(new OlapEventTypeParametersDto()))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricsList", hasSize(2)))
                .andExpect(jsonPath("$.metricsList[0].id", is(1)))
                .andExpect(jsonPath("$.metricsList[1].id", is(0)));

        Mockito.verify(olapService, VerificationModeFactory.times(1)).getOlapEventTypeStatistics(Mockito.any());
    }

    @Test
    void getOlapSpeakerStatistics() throws Exception {
        Speaker speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        List<Integer> yearDimensionValues0 = List.of(2020, 2021);
        List<Void> voidDimensionValues0 = Collections.emptyList();
        List<OlapEntitySubMetrics<Speaker, Void>> yearVoidSpeakerSubMetricsList0 = Collections.emptyList();

        List<OlapEntityMetrics<Speaker>> metricsList0 = List.of(
                new OlapEntityMetrics<>(speaker0, List.of(0L, 1L), List.of(0L, 1L), 1L),
                new OlapEntityMetrics<>(speaker1, List.of(2L, 0L), List.of(2L, 2L), 2L)
        );
        OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

        OlapEntityStatistics<Integer, Void, Speaker> speakerStatistics0 =
                new OlapEntityStatistics<>(yearDimensionValues0, voidDimensionValues0, yearVoidSpeakerSubMetricsList0, metricsList0, totals0);

        given(olapService.getOlapSpeakerStatistics(Mockito.any())).willReturn(speakerStatistics0);

        mvc.perform(post("/api/statistics/olap-speaker-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(new OlapSpeakerParametersDto()))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricsList", hasSize(2)))
                .andExpect(jsonPath("$.metricsList[0].id", is(1)))
                .andExpect(jsonPath("$.metricsList[1].id", is(0)));

        Mockito.verify(olapService, VerificationModeFactory.times(1)).getOlapSpeakerStatistics(Mockito.any());
    }

    @Test
    void getOlapCityStatistics() throws Exception {
        City city0 = new City(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        City city1 = new City(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        List<Integer> yearDimensionValues0 = List.of(2020, 2021);
        List<Void> voidDimensionValues0 = Collections.emptyList();
        List<OlapEntitySubMetrics<City, Void>> yearVoidCitySubMetricsList0 = Collections.emptyList();

        List<OlapEntityMetrics<City>> metricsList0 = List.of(
                new OlapEntityMetrics<>(city1, List.of(2L, 0L), List.of(2L, 2L), 2L),
                new OlapEntityMetrics<>(city0, List.of(0L, 1L), List.of(0L, 1L), 1L)
        );
        OlapEntityMetrics<Void> totals0 = new OlapEntityMetrics<>(null, List.of(2L, 1L), List.of(2L, 3L), 3L);

        OlapEntityStatistics<Integer, Void, City> cityStatistics0 =
                new OlapEntityStatistics<>(yearDimensionValues0, voidDimensionValues0, yearVoidCitySubMetricsList0, metricsList0, totals0);

        given(olapService.getOlapCityStatistics(Mockito.any())).willReturn(cityStatistics0);

        mvc.perform(post("/api/statistics/olap-city-statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(new OlapCityParametersDto()))
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricsList", hasSize(2)))
                .andExpect(jsonPath("$.metricsList[0].id", is(0)))
                .andExpect(jsonPath("$.metricsList[1].id", is(1)));

        Mockito.verify(olapService, VerificationModeFactory.times(1)).getOlapCityStatistics(Mockito.any());
    }
}
