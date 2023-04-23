package guess.util.load;

import guess.domain.Conference;
import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.source.image.UrlFilename;
import guess.domain.source.load.*;
import guess.util.ImageUtils;
import guess.util.LocalizationUtils;
import guess.util.yaml.YamlUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("ConferenceDataLoaderExecutor class tests")
class ConferenceDataLoaderExecutorTest {
    @Test
    void loadSpaceTags() throws IOException, NoSuchFieldException {
        try (MockedStatic<CmsDataLoaderFactory> mockedStatic = Mockito.mockStatic(CmsDataLoaderFactory.class)) {
            final String CODE1 = "code1";
            final String CODE2 = "code2";
            final String CODE3 = "code3";
            final String CODE4 = "code4";

            CmsDataLoader cmsDataLoader = Mockito.mock(CmsDataLoader.class);
            Mockito.when(cmsDataLoader.getTags(Mockito.nullable(String.class)))
                    .thenReturn(Map.of(
                            ContentfulDataLoader.ConferenceSpaceInfo.COMMON_SPACE_INFO.toString(),
                            List.of(CODE1, CODE2, CODE3, CODE4)));

            mockedStatic.when(() -> CmsDataLoaderFactory.createDataLoader(Mockito.any(CmsType.class)))
                    .thenReturn(cmsDataLoader);

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.loadTags(CmsType.CONTENTFUL, null));
        }
    }

    @Test
    void loadEventTypes() throws IOException, NoSuchFieldException {
        try (MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class);
             MockedStatic<CmsDataLoaderFactory> cmsDataLoaderFactoryMockedStatic = Mockito.mockStatic(CmsDataLoaderFactory.class);
             MockedStatic<ConferenceDataLoaderExecutor> conferenceDataLoaderMockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
            yamlUtilsMockedStatic.when(YamlUtils::readSourceInformation)
                    .thenReturn(new SourceInformation(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            new SourceInformation.SpeakerInformation(
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList()
                            ),
                            Collections.emptyList()
                    ));

            CmsDataLoader cmsDataLoader = Mockito.mock(CmsDataLoader.class);
            Mockito.when(cmsDataLoader.getEventTypes()).thenReturn(Collections.emptyList());
            cmsDataLoaderFactoryMockedStatic.when(() -> CmsDataLoaderFactory.createDataLoader(Mockito.any(CmsType.class)))
                    .thenReturn(cmsDataLoader);

            conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.loadEventTypes(Mockito.any()))
                    .thenCallRealMethod();
            conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.getConferences(Mockito.anyList()))
                    .thenReturn(Collections.emptyList());
            conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.getResourceEventTypeMap(Mockito.anyList()))
                    .thenReturn(Collections.emptyMap());
            conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.getLastId(Mockito.anyList()))
                    .thenReturn(42L);
            conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.getEventTypeLoadResult(Mockito.anyList(), Mockito.anyMap(), Mockito.any()))
                    .thenReturn(new LoadResult<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.loadEventTypes(CmsType.CONTENTFUL));
        }
    }

    @Test
    void getConferences() {
        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setConference(Conference.JPOINT);

        EventType eventType1 = new EventType();
        eventType1.setId(1);

        EventType eventType2 = new EventType();
        eventType2.setId(2);
        eventType2.setConference(Conference.JOKER);

        assertEquals(List.of(eventType0, eventType2), ConferenceDataLoaderExecutor.getConferences(List.of(eventType0, eventType1, eventType2)));
    }

    @Test
    void getResourceEventTypeMap() {
        EventType eventType0 = new EventType();
        eventType0.setId(0);
        eventType0.setConference(Conference.JPOINT);

        EventType eventType1 = new EventType();
        eventType1.setId(1);

        EventType eventType2 = new EventType();
        eventType2.setId(2);
        eventType2.setConference(Conference.JOKER);

        Map<Conference, EventType> expected = new HashMap<>();
        expected.put(Conference.JPOINT, eventType0);
        expected.put(null, eventType1);
        expected.put(Conference.JOKER, eventType2);

        assertEquals(expected, ConferenceDataLoaderExecutor.getResourceEventTypeMap(List.of(eventType0, eventType1, eventType2)));
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getLastId method tests")
    class GetLastIdTest {
        private Stream<Arguments> data() {
            EventType eventType0 = new EventType();
            eventType0.setId(0);

            EventType eventType1 = new EventType();
            eventType1.setId(1);

            EventType eventType2 = new EventType();
            eventType2.setId(2);

            return Stream.of(
                    arguments(Collections.emptyList(), -1),
                    arguments(List.of(eventType0), 0),
                    arguments(List.of(eventType0, eventType1), 1),
                    arguments(List.of(eventType0, eventType1, eventType2), 2),
                    arguments(List.of(eventType1, eventType0), 1),
                    arguments(List.of(eventType1, eventType0, eventType2), 2)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getLastId(List<EventType> entities, long expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.getLastId(entities));
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getFirstId method tests")
    class GetFirstIdTest {
        private Stream<Arguments> data() {
            EventType eventType0 = new EventType();
            eventType0.setId(42);

            EventType eventType1 = new EventType();
            eventType1.setId(43);

            EventType eventType2 = new EventType();
            eventType2.setId(44);

            return Stream.of(
                    arguments(Collections.emptyList(), 0),
                    arguments(List.of(eventType0), 42),
                    arguments(List.of(eventType0, eventType1), 42),
                    arguments(List.of(eventType0, eventType1, eventType2), 42),
                    arguments(List.of(eventType1, eventType0), 42),
                    arguments(List.of(eventType1, eventType0, eventType2), 42),
                    arguments(List.of(eventType2), 44),
                    arguments(List.of(eventType1, eventType2), 43)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getFirstId(List<EventType> entities, long expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.getFirstId(entities));
        }
    }

    @Test
    void getEventTypeLoadResult() {
        try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
            mockedStatic.when(() -> ConferenceDataLoaderExecutor.getEventTypeLoadResult(Mockito.anyList(), Mockito.anyMap(), Mockito.any()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> ConferenceDataLoaderExecutor.needUpdate(Mockito.any(EventType.class), Mockito.any(EventType.class)))
                    .thenAnswer(
                            (Answer<Boolean>) invocation -> {
                                Object[] args = invocation.getArguments();
                                EventType a = (EventType) args[0];
                                EventType b = (EventType) args[1];

                                return (Conference.JPOINT.equals(a.getConference()) && Conference.JPOINT.equals(b.getConference()));
                            }
                    );

            EventType eventType0 = new EventType();
            eventType0.setId(0);

            EventType eventType1 = new EventType();
            eventType1.setId(1);
            eventType1.setConference(Conference.JOKER);

            EventType eventType2 = new EventType();
            eventType2.setId(2);
            eventType2.setConference(Conference.JPOINT);

            EventType eventType3 = new EventType();
            eventType3.setId(3);
            eventType3.setConference(Conference.DOT_NEXT);

            Map<Conference, EventType> eventTypeMap = new HashMap<>(Map.of(Conference.JPOINT, eventType2,
                    Conference.DOT_NEXT, eventType3));

            LoadResult<List<EventType>> expected = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(eventType0, eventType1),
                    List.of(eventType2));

            assertEquals(expected, ConferenceDataLoaderExecutor.getEventTypeLoadResult(
                    List.of(eventType0, eventType1, eventType2, eventType3),
                    eventTypeMap,
                    new AtomicLong(-1)));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("saveEventTypes method tests")
    class SaveEventTypesTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList())),
                    arguments(new LoadResult<>(
                            Collections.emptyList(),
                            List.of(new EventType()),
                            Collections.emptyList())),
                    arguments(new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(new EventType()))),
                    arguments(new LoadResult<>(
                            Collections.emptyList(),
                            List.of(new EventType()),
                            List.of(new EventType())))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void saveEventTypes(LoadResult<List<EventType>> loadResult) {
            try (MockedStatic<ConferenceDataLoaderExecutor> conferenceDataLoaderMockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class);
                 MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class)) {
                conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.saveEventTypes(Mockito.any()))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveEventTypes(loadResult));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("loadTalksSpeakersEvent method tests")
    class LoadTalksSpeakersEventTest {
        private Stream<Arguments> data() {
            final Conference JPOINT_CONFERENCE = Conference.JPOINT;
            final LocalDate EVENT_DATE = LocalDate.of(2020, 6, 29);
            final String EVENT_CODE = "2020-jpoint";

            Place place0 = new Place();

            Organizer organizer0 = new Organizer();

            Topic topic0 = new Topic();

            Event event0 = new Event();
            event0.setId(0);
            event0.setDays(List.of(new EventDays(
                    EVENT_DATE,
                    EVENT_DATE,
                    place0
            )));

            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setConference(JPOINT_CONFERENCE);
            eventType0.setOrganizer(organizer0);
            eventType0.setEvents(List.of(event0));

            Talk talk0 = new Talk();
            talk0.setId(0);

            Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company0")));

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            speaker0.setCompanies(List.of(company0));

            return Stream.of(
                    arguments(JPOINT_CONFERENCE, EVENT_DATE, EVENT_CODE, LoadSettings.defaultSettings(),
                            new SourceInformation(
                                    List.of(place0),
                                    List.of(organizer0),
                                    List.of(topic0),
                                    List.of(eventType0),
                                    Collections.emptyList(),
                                    new SourceInformation.SpeakerInformation(
                                            Collections.emptyList(),
                                            Collections.emptyList(),
                                            Collections.emptyList(),
                                            List.of(speaker0)
                                    ),
                                    Collections.emptyList()),
                            event0,
                            List.of(talk0),
                            List.of(speaker0),
                            List.of(company0),
                            Map.of("name0", company0)),
                    arguments(JPOINT_CONFERENCE, LocalDate.of(2020, 6, 30), null, LoadSettings.defaultSettings(),
                            new SourceInformation(
                                    List.of(place0),
                                    List.of(organizer0),
                                    List.of(topic0),
                                    List.of(eventType0),
                                    Collections.emptyList(),
                                    new SourceInformation.SpeakerInformation(
                                            Collections.emptyList(),
                                            Collections.emptyList(),
                                            Collections.emptyList(),
                                            List.of(speaker0)
                                    ),
                                    Collections.emptyList()),
                            event0,
                            List.of(talk0),
                            List.of(speaker0),
                            List.of(company0),
                            Map.of("name0", company0))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void loadTalksSpeakersEvent(Conference conference, LocalDate startDate, String conferenceCode,
                                    LoadSettings loadSettings, SourceInformation sourceInformation, Event contentfulEvent,
                                    List<Talk> contentfulTalks, List<Speaker> talkSpeakers, List<Company> speakerCompanies,
                                    Map<String, Company> resourceLowerNameCompanyMap) throws IOException, NoSuchFieldException {
            try (MockedStatic<CmsDataLoaderFactory> cmsDataLoaderFactoryMockedStatic = Mockito.mockStatic(CmsDataLoaderFactory.class);
                 MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class);
                 MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
                 MockedStatic<ConferenceDataLoaderExecutor> conferenceDataLoaderExecutorMockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {

                CmsDataLoader cmsDataLoader = Mockito.mock(CmsDataLoader.class);
                Mockito.when(cmsDataLoader.getEvent(Mockito.any(Conference.class), Mockito.any(LocalDate.class), Mockito.nullable(String.class), Mockito.nullable(Event.class)))
                        .thenReturn(contentfulEvent);
                Mockito.when(cmsDataLoader.getTalks(Mockito.any(Conference.class), Mockito.any(LocalDate.class), Mockito.anyString(), Mockito.anyBoolean()))
                        .thenReturn(contentfulTalks);
                cmsDataLoaderFactoryMockedStatic.when(() -> CmsDataLoaderFactory.createDataLoader(Mockito.any(LocalDate.class)))
                        .thenReturn(cmsDataLoader);

                yamlUtilsMockedStatic.when(YamlUtils::readSourceInformation)
                        .thenReturn(sourceInformation);
                localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.nullable(List.class), Mockito.any(Language.class)))
                        .thenReturn("");
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.loadTalksSpeakersEvent(
                                Mockito.any(Conference.class), Mockito.any(LocalDate.class), Mockito.nullable(String.class), Mockito.any(LoadSettings.class)))
                        .thenCallRealMethod();
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.deleteInvalidTalks(Mockito.anyList(), Mockito.anySet()))
                        .thenAnswer(
                                (Answer<List<Talk>>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return (List<Talk>) args[0];
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.deleteOpeningAndClosingTalks(Mockito.anyList()))
                        .thenAnswer(
                                (Answer<List<Talk>>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return (List<Talk>) args[0];
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.deleteTalkDuplicates(Mockito.anyList()))
                        .thenAnswer(
                                (Answer<List<Talk>>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return (List<Talk>) args[0];
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getTalkSpeakers(Mockito.anyList()))
                        .thenReturn(talkSpeakers);
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getSpeakerCompanies(Mockito.anyList()))
                        .thenReturn(speakerCompanies);
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getResourceLowerNameCompanyMap(Mockito.anyList()))
                        .thenReturn(resourceLowerNameCompanyMap);
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getLastId(Mockito.anyList()))
                        .thenReturn(42L);
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getCompanyLoadResult(
                                Mockito.anyList(), Mockito.anyMap(), Mockito.any()))
                        .thenReturn(new LoadResult<>(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList()));
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getResourceNameCompanySpeakerMap(Mockito.anyList()))
                        .thenReturn(Collections.emptyMap());
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getResourceNameSpeakersMap(Mockito.anyList()))
                        .thenReturn(Collections.emptyMap());
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getSpeakerLoadResult(
                                Mockito.anyList(), Mockito.any(SpeakerLoadMaps.class), Mockito.any(), Mockito.anyString()))
                        .thenReturn(new SpeakerLoadResult(
                                new LoadResult<>(
                                        Collections.emptyList(),
                                        Collections.emptyList(),
                                        Collections.emptyList()),
                                new LoadResult<>(
                                        Collections.emptyList(),
                                        Collections.emptyList(),
                                        Collections.emptyList())));
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getTalkLoadResult(
                                Mockito.anyList(), Mockito.any(Event.class), Mockito.anyList(), Mockito.any()))
                        .thenReturn(new LoadResult<>(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList()));
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.fixVenueAddress(Mockito.any(Place.class)))
                        .thenReturn(Collections.emptyList());
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourcePlace(
                                Mockito.any(Place.class), Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap()))
                        .thenAnswer(
                                (Answer<Place>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return (Place) args[0];
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getPlaceLoadResult(
                                Mockito.anyList(), Mockito.anyList(), Mockito.any()))
                        .thenReturn(new LoadResult<>(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList()));
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getEventLoadResult(Mockito.any(Event.class), Mockito.any(Event.class)))
                        .thenReturn(new LoadResult<>(
                                null,
                                null,
                                null));

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.loadTalksSpeakersEvent(conference, startDate, conferenceCode, loadSettings));
            }
        }
    }

    @Test
    void loadTalksSpeakersEventWithoutInvalidTalksSetAndKnownSpeakerIdsMap() {
        try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
            mockedStatic.when(() -> ConferenceDataLoaderExecutor.loadTalksSpeakersEvent(
                            Mockito.any(Conference.class), Mockito.any(LocalDate.class), Mockito.anyString()))
                    .thenCallRealMethod();

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.loadTalksSpeakersEvent(
                    Conference.JPOINT,
                    LocalDate.of(2020, 6, 29),
                    "2020-jpoint"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("deleteInvalidTalks method tests")
    class DeleteInvalidTalksTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);
            talk0.setName(List.of(new LocaleItem("en", "Name0")));

            Talk talk1 = new Talk();
            talk1.setId(1);
            talk1.setName(List.of(new LocaleItem("en", "Name1")));

            Talk talk2 = new Talk();
            talk2.setId(2);
            talk2.setName(List.of(new LocaleItem("en", "Name2"), new LocaleItem("ru", "Имя2")));

            return Stream.of(
                    arguments(List.of(talk0, talk1, talk2), Collections.emptySet(), List.of(talk0, talk1, talk2)),
                    arguments(List.of(talk0, talk1, talk2), Set.of("Unknown"), List.of(talk0, talk1, talk2)),
                    arguments(List.of(talk0, talk1, talk2), Set.of("Name0"), List.of(talk1, talk2)),
                    arguments(List.of(talk0, talk1, talk2), Set.of("Имя2"), List.of(talk0, talk1)),
                    arguments(List.of(talk0, talk1, talk2), Set.of("Name0", "Имя2"), List.of(talk1)),
                    arguments(List.of(talk0, talk1, talk2), Set.of("Name0", "Name1", "Имя2"), List.of())
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void deleteInvalidTalks(List<Talk> talks, Set<String> invalidTalksSet, List<Talk> expected) {
            try (MockedStatic<LocalizationUtils> mockedStatic = Mockito.mockStatic(LocalizationUtils.class)) {
                mockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<String>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    List<LocaleItem> localeItems = (List<LocaleItem>) args[0];
                                    Language language = (Language) args[1];

                                    if ((localeItems != null) && !localeItems.isEmpty()) {
                                        if (Language.ENGLISH.equals(language)) {
                                            return localeItems.get(0).getText();
                                        } else if (Language.RUSSIAN.equals(language)) {
                                            if (localeItems.size() > 1) {
                                                return localeItems.get(1).getText();
                                            } else {
                                                return localeItems.get(0).getText();
                                            }
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        return null;
                                    }
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.deleteInvalidTalks(talks, invalidTalksSet));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("deleteOpeningAndClosingTalks method tests")
    class DeleteOpeningAndClosingTalksTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);
            talk0.setName(List.of(new LocaleItem("en", "Conference opening")));

            Talk talk1 = new Talk();
            talk1.setId(1);
            talk1.setName(List.of(new LocaleItem("en", "Conference closing")));

            Talk talk2 = new Talk();
            talk2.setId(2);
            talk2.setName(List.of(new LocaleItem("en", "School opening")));

            Talk talk3 = new Talk();
            talk3.setId(3);
            talk3.setName(List.of(new LocaleItem("en", "School closing")));

            Talk talk4 = new Talk();
            talk4.setId(4);
            talk4.setName(List.of(new LocaleItem("ru", "Открытие")));

            Talk talk5 = new Talk();
            talk5.setId(5);
            talk5.setName(List.of(new LocaleItem("ru", "Закрытие")));

            Talk talk6 = new Talk();
            talk6.setId(6);
            talk6.setName(List.of(new LocaleItem("ru", "Открытие конференции")));

            Talk talk7 = new Talk();
            talk7.setId(7);
            talk7.setName(List.of(new LocaleItem("ru", "Закрытие конференции")));

            Talk talk8 = new Talk();
            talk8.setId(8);
            talk8.setName(List.of(new LocaleItem("en", "Opening")));

            Talk talk9 = new Talk();
            talk9.setId(9);
            talk9.setName(List.of(new LocaleItem("en", "name8")));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(talk0), Collections.emptyList()),
                    arguments(List.of(talk1), Collections.emptyList()),
                    arguments(List.of(talk2), Collections.emptyList()),
                    arguments(List.of(talk3), Collections.emptyList()),
                    arguments(List.of(talk4), Collections.emptyList()),
                    arguments(List.of(talk5), Collections.emptyList()),
                    arguments(List.of(talk6), Collections.emptyList()),
                    arguments(List.of(talk7), Collections.emptyList()),
                    arguments(List.of(talk8), Collections.emptyList()),
                    arguments(List.of(talk0, talk1), Collections.emptyList()),
                    arguments(List.of(talk0, talk1, talk2), Collections.emptyList()),
                    arguments(List.of(talk0, talk1, talk2, talk3), Collections.emptyList()),
                    arguments(List.of(talk9), List.of(talk9)),
                    arguments(List.of(talk0, talk9), List.of(talk9)),
                    arguments(List.of(talk0, talk1, talk9), List.of(talk9)),
                    arguments(List.of(talk0, talk1, talk2, talk9), List.of(talk9)),
                    arguments(List.of(talk0, talk1, talk2, talk3, talk9), List.of(talk9))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void deleteOpeningAndClosingTalks(List<Talk> talks, List<Talk> expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.deleteOpeningAndClosingTalks(talks));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("deleteTalkDuplicates method tests")
    class DeleteTalkDuplicatesTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);
            talk0.setName(List.of(new LocaleItem("ru", "name0")));
            talk0.setTalkDay(1L);
            talk0.setTrack(1L);
            talk0.setStartTime(LocalTime.of(10, 0));

            Talk talk1 = new Talk();
            talk1.setId(1);
            talk1.setName(List.of(new LocaleItem("ru", "name0")));
            talk1.setTalkDay(2L);

            Talk talk2 = new Talk();
            talk2.setId(2);
            talk2.setName(List.of(new LocaleItem("ru", "name0")));
            talk2.setTalkDay(1L);
            talk2.setTrack(2L);

            Talk talk3 = new Talk();
            talk3.setId(3);
            talk3.setName(List.of(new LocaleItem("ru", "name0")));
            talk3.setTalkDay(1L);
            talk3.setTrack(1L);
            talk3.setStartTime(LocalTime.of(10, 30));

            Talk talk4 = new Talk();
            talk4.setId(4);
            talk4.setName(List.of(new LocaleItem("ru", "name0")));
            talk4.setTalkDay(1L);
            talk4.setTrack(1L);
            talk4.setStartTime(LocalTime.of(11, 0));

            Talk talk5 = new Talk();
            talk5.setId(5);
            talk5.setName(List.of(new LocaleItem("ru", "name5")));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(talk0), List.of(talk0)),
                    arguments(List.of(talk1, talk0), List.of(talk0)),
                    arguments(List.of(talk0, talk1), List.of(talk0)),
                    arguments(List.of(talk1, talk2, talk0), List.of(talk0)),
                    arguments(List.of(talk1, talk0, talk2), List.of(talk0)),
                    arguments(List.of(talk1, talk2, talk3, talk0), List.of(talk0)),
                    arguments(List.of(talk0, talk5), List.of(talk0, talk5)),
                    arguments(List.of(talk1, talk2, talk3, talk0, talk4), List.of(talk0)),
                    arguments(List.of(talk1, talk2, talk3, talk0, talk5), List.of(talk0, talk5))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void deleteTalkDuplicates(List<Talk> talks, List<Talk> expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.deleteTalkDuplicates(talks));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("checkTalkSpeakersExistence method tests")
    class CheckTalkSpeakersExistenceTest {
        private Stream<Arguments> data() {
            Speaker speaker0 = new Speaker();
            speaker0.setId(0);

            Talk talk0 = new Talk();
            talk0.setId(0);
            talk0.setName(List.of(new LocaleItem("en", "Name0")));
            talk0.setSpeakers(List.of(speaker0));

            Talk talk1 = new Talk();
            talk1.setId(1);
            talk1.setName(List.of(new LocaleItem("en", "Name1")));

            return Stream.of(
                    arguments(Collections.emptyList(), null),
                    arguments(List.of(talk0), null),
                    arguments(List.of(talk1), IllegalStateException.class),
                    arguments(List.of(talk0, talk1), IllegalStateException.class)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void checkTalkSpeakersExistence(List<Talk> talks, Class<? extends Throwable> expectedException) {
            if (expectedException == null) {
                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.checkTalkSpeakersExistence(talks));
            } else {
                assertThrows(expectedException, () -> ConferenceDataLoaderExecutor.checkTalkSpeakersExistence(talks));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getInvalidCompanyNames method tests")
    class GetInvalidCompanyNamesTest {
        private Stream<Arguments> data() {
            final String COMPANY_NAME0 = "EPAM Systems";
            final String COMPANY_NAME1 = "CROC";

            final String SYNONYM0 = "EPAM";
            final String SYNONYM1 = "KROK";
            final String SYNONYM2 = "INVALID0";
            final String SYNONYM3 = "INVALID1";
            final String SYNONYM4 = "INVALID3";

            CompanySynonyms companySynonyms0 = new CompanySynonyms();
            companySynonyms0.setName(COMPANY_NAME0);
            companySynonyms0.setSynonyms(List.of(SYNONYM0));

            CompanySynonyms companySynonyms1 = new CompanySynonyms();
            companySynonyms1.setName(COMPANY_NAME1);
            companySynonyms1.setSynonyms(List.of(SYNONYM1));

            CompanySynonyms companySynonyms2 = new CompanySynonyms();
            companySynonyms2.setName(null);
            companySynonyms2.setSynonyms(List.of(SYNONYM2, SYNONYM3));

            CompanySynonyms companySynonyms3 = new CompanySynonyms();
            companySynonyms3.setName("");
            companySynonyms3.setSynonyms(List.of(SYNONYM4));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptySet()),
                    arguments(List.of(companySynonyms0, companySynonyms1), Collections.emptySet()),
                    arguments(List.of(companySynonyms2), Set.of(SYNONYM2, SYNONYM3)),
                    arguments(List.of(companySynonyms3), Set.of(SYNONYM4)),
                    arguments(List.of(companySynonyms2, companySynonyms3), Set.of(SYNONYM2, SYNONYM3, SYNONYM4)),
                    arguments(List.of(companySynonyms0, companySynonyms1, companySynonyms2, companySynonyms3), Set.of(SYNONYM2, SYNONYM3, SYNONYM4))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getInvalidCompanyNames(List<CompanySynonyms> companySynonymsList, Set<String> expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.getInvalidCompanyNames(companySynonymsList));
        }
    }

    @Test
    void deleteInvalidSpeakerCompanies() {
        final String NAME0 = "Name0";
        final String NAME1 = "Invalid";

        Company company0 = new Company();
        company0.setId(0L);
        company0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), NAME0)));

        Company company1 = new Company();
        company1.setId(1L);
        company1.setName(Collections.emptyList());

        Company company2 = new Company();
        company2.setId(2L);

        Company company3 = new Company();
        company3.setId(3L);
        company3.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), NAME1)));

        Speaker speaker0 = new Speaker();
        speaker0.setId(0L);
        speaker0.setCompanyIds(new ArrayList<>());
        speaker0.setCompanies(new ArrayList<>());

        Speaker speaker1 = new Speaker();
        speaker1.setId(1L);
        speaker1.setCompanyIds(new ArrayList<>(List.of(0L)));
        speaker1.setCompanies(new ArrayList<>(List.of(company0)));

        Speaker speaker2 = new Speaker();
        speaker2.setId(2L);
        speaker2.setCompanyIds(new ArrayList<>(List.of(0L, 1L, 2L)));
        speaker2.setCompanies(new ArrayList<>(List.of(company0, company1, company2)));

        Speaker speaker3 = new Speaker();
        speaker3.setId(3L);
        speaker3.setCompanyIds(new ArrayList<>(List.of(1L, 2L)));
        speaker3.setCompanies(new ArrayList<>(List.of(company1, company2)));

        Speaker speaker4 = new Speaker();
        speaker4.setId(4L);
        speaker4.setCompanyIds(new ArrayList<>(List.of(3L)));
        speaker4.setCompanies(new ArrayList<>(List.of(company3)));

        List<Speaker> speakers = List.of(speaker0, speaker1, speaker2, speaker3, speaker4);

        Set<String> invalidCompanyNames = Set.of(NAME1);

        Predicate<Company> invalidCompanyPredicate = c -> {
            if ((c.getName() == null) || c.getName().isEmpty()) {
                return true;
            } else {
                return c.getName().stream()
                        .map(LocaleItem::getText)
                        .anyMatch(invalidCompanyNames::contains);
            }
        };
        List<Company> oldCompanies = speakers.stream()
                .flatMap(s -> s.getCompanies().stream())
                .toList();
        long oldTotalCompanyCount = oldCompanies.size();
        long oldInvalidCompanyCount = oldCompanies.stream()
                .filter(invalidCompanyPredicate)
                .count();
        long oldValidCompanyCount = oldTotalCompanyCount - oldInvalidCompanyCount;

        assertTrue(oldTotalCompanyCount > 0);
        assertTrue(oldInvalidCompanyCount > 0);

        ConferenceDataLoaderExecutor.deleteInvalidSpeakerCompanies(speakers, invalidCompanyNames);

        List<Company> newCompanies = speakers.stream()
                .flatMap(s -> s.getCompanies().stream())
                .toList();
        long newTotalCompanyCount = newCompanies.size();
        long newInvalidCompanyCount = newCompanies.stream()
                .filter(invalidCompanyPredicate)
                .count();
        long newValidCompanyCount = newTotalCompanyCount - newInvalidCompanyCount;

        assertTrue(newTotalCompanyCount > 0);
        assertEquals(0, newInvalidCompanyCount);
        assertEquals(oldValidCompanyCount, newValidCompanyCount);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("splitCompanyGroupNames method tests")
    class SplitCompanyGroupNamesTest {
        private Stream<Arguments> data() {
            final String NAME0 = "Name0";
            final String NAME1 = "Name1";
            final String NAME2 = "Name0, Name1";

            Company company0 = new Company();
            company0.setId(0);
            company0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), NAME0)));

            Company company1 = new Company();
            company1.setId(1);
            company1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), NAME1)));

            Company company2 = new Company();
            company2.setId(2);
            company2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), NAME2)));

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setCompanies(List.of(company0));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setCompanies(List.of(company1));

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);
            speaker2.setCompanies(List.of(company2));

            CompanyGroup companyGroup0 = new CompanyGroup();
            companyGroup0.setName(NAME2);
            companyGroup0.setItems(List.of(NAME0, NAME1));

            List<CompanyGroup> companyGroups0 = Collections.emptyList();
            List<CompanyGroup> companyGroups1 = List.of(companyGroup0);

            return Stream.of(
                    arguments(List.of(speaker0, speaker1, speaker2), companyGroups0, new AtomicLong(0)),
                    arguments(List.of(speaker0, speaker1, speaker2), companyGroups1, new AtomicLong(0))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void splitCompanyGroupNames(List<Speaker> speakers, List<CompanyGroup> companyGroups, AtomicLong firstCompanyId) {
            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.splitCompanyGroupNames(speakers, companyGroups, firstCompanyId));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getTalkSpeakers method tests")
    class GetTalkSpeakersTest {
        private Stream<Arguments> data() {
            Speaker speaker0 = new Speaker();
            speaker0.setId(0);

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);

            Talk talk0 = new Talk();
            talk0.setSpeakers(List.of(speaker0));

            Talk talk1 = new Talk();
            talk1.setSpeakers(List.of(speaker0, speaker1));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(talk0), List.of(speaker0)),
                    arguments(List.of(talk1), List.of(speaker0, speaker1)),
                    arguments(List.of(talk0, talk1), List.of(speaker0, speaker1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getTalkSpeakers(List<Talk> talks, List<Speaker> expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.getTalkSpeakers(talks));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerCompanies method tests")
    class GetSpeakerCompaniesTest {
        private Stream<Arguments> data() {
            Company company0 = new Company(0, Collections.emptyList());
            Company company1 = new Company(1, Collections.emptyList());
            Company company2 = new Company(2, Collections.emptyList());

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setCompanies(List.of(company0, company1));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setCompanies(List.of(company0, company2));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(speaker0), List.of(company0, company1)),
                    arguments(List.of(speaker1), List.of(company0, company2)),
                    arguments(List.of(speaker0, speaker1), List.of(company0, company1, company2))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerCompanies(List<Speaker> speakers, List<Company> expected) {
            List<Company> actual = ConferenceDataLoaderExecutor.getSpeakerCompanies(speakers);

            assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getResourceLowerNameCompanyMap method tests")
    class GetResourceLowerNameCompanyMapTest {
        private Stream<Arguments> data() {
            Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company0")));
            Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company1")));
            Company company2 = new Company(2, List.of(new LocaleItem(Language.RUSSIAN.getCode(), "КОМПАНИЯ2")));
            Company company3 = new Company(3, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "company3"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания3")
            ));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyMap()),
                    arguments(List.of(company0), Map.of("company0", company0)),
                    arguments(List.of(company1), Map.of("company1", company1)),
                    arguments(List.of(company0, company1), Map.of("company0", company0, "company1", company1)),
                    arguments(List.of(company2), Map.of("компания2", company2)),
                    arguments(List.of(company3), Map.of("company3", company3, "компания3", company3)),
                    arguments(List.of(company2, company3), Map.of("компания2", company2, "company3", company3, "компания3", company3))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getResourceLowerNameCompanyMap(List<Company> companies, Map<String, Company> expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.getResourceLowerNameCompanyMap(companies));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("addLowerSynonymsToCompanyMap method tests")
    class AddLowerSynonymsToCompanyMapTest {
        private Stream<Arguments> data() {
            final String COMPANY_NAME0 = "EPAM Systems";
            final String COMPANY_NAME1 = "CROC";

            final String SYNONYM0 = "EPAM";
            final String SYNONYM1 = "KROK";

            Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME0)));
            Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME1)));

            CompanySynonyms companySynonyms0 = new CompanySynonyms();
            companySynonyms0.setName(COMPANY_NAME0);
            companySynonyms0.setSynonyms(List.of(SYNONYM0));

            CompanySynonyms companySynonyms1 = new CompanySynonyms();
            companySynonyms1.setName(COMPANY_NAME1);
            companySynonyms1.setSynonyms(List.of(SYNONYM1));

            CompanySynonyms companySynonyms2 = new CompanySynonyms();
            companySynonyms2.setName(COMPANY_NAME1);
            companySynonyms2.setSynonyms(List.of(COMPANY_NAME1));

            CompanySynonyms companySynonyms3 = new CompanySynonyms();
            companySynonyms3.setName(null);
            companySynonyms3.setSynonyms(List.of(SYNONYM1));

            CompanySynonyms companySynonyms4 = new CompanySynonyms();
            companySynonyms4.setName("");
            companySynonyms4.setSynonyms(List.of(SYNONYM1));

            Map<String, Company> companyMap0 = new HashMap<>();
            companyMap0.put(COMPANY_NAME0.toLowerCase(), company0);
            companyMap0.put(COMPANY_NAME1.toLowerCase(), company1);

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyMap(), null, Collections.emptyMap()),
                    arguments(List.of(companySynonyms0), new HashMap<>(companyMap0), null, Map.of(
                            COMPANY_NAME0.toLowerCase(), company0,
                            COMPANY_NAME1.toLowerCase(), company1,
                            SYNONYM0.toLowerCase(), company0)),
                    arguments(List.of(companySynonyms1), new HashMap<>(companyMap0), null, Map.of(
                            COMPANY_NAME0.toLowerCase(), company0,
                            COMPANY_NAME1.toLowerCase(), company1,
                            SYNONYM1.toLowerCase(), company1)),
                    arguments(List.of(companySynonyms0, companySynonyms1), new HashMap<>(companyMap0), null, Map.of(
                            COMPANY_NAME0.toLowerCase(), company0,
                            COMPANY_NAME1.toLowerCase(), company1,
                            SYNONYM1.toLowerCase(), company1,
                            SYNONYM0.toLowerCase(), company0)),
                    arguments(List.of(companySynonyms0), Collections.emptyMap(), NullPointerException.class, null),
                    arguments(List.of(companySynonyms2), new HashMap<>(companyMap0), IllegalArgumentException.class, null),
                    arguments(List.of(companySynonyms0, companySynonyms3), new HashMap<>(companyMap0), null, Map.of(
                            COMPANY_NAME0.toLowerCase(), company0,
                            COMPANY_NAME1.toLowerCase(), company1,
                            SYNONYM0.toLowerCase(), company0)),
                    arguments(List.of(companySynonyms0, companySynonyms4), new HashMap<>(companyMap0), null, Map.of(
                            COMPANY_NAME0.toLowerCase(), company0,
                            COMPANY_NAME1.toLowerCase(), company1,
                            SYNONYM0.toLowerCase(), company0)),
                    arguments(List.of(companySynonyms0, companySynonyms3, companySynonyms4), new HashMap<>(companyMap0), null, Map.of(
                            COMPANY_NAME0.toLowerCase(), company0,
                            COMPANY_NAME1.toLowerCase(), company1,
                            SYNONYM0.toLowerCase(), company0))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void addLowerSynonymsToCompanyMap(List<CompanySynonyms> companySynonymsList, Map<String, Company> companyMap,
                                          Class<? extends Throwable> expectedException, Map<String, Company> expectedValue) {
            if (expectedException == null) {
                ConferenceDataLoaderExecutor.addLowerSynonymsToCompanyMap(companySynonymsList, companyMap);

                assertEquals(expectedValue, companyMap);
            } else {
                assertThrows(expectedException, () -> ConferenceDataLoaderExecutor.addLowerSynonymsToCompanyMap(companySynonymsList, companyMap));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getCompanyLoadResult method tests")
    class GetCompanyLoadResultTest {
        Company createCompany(long id, String name) {
            return new Company(id, List.of(new LocaleItem(Language.ENGLISH.getCode(), name)));
        }

        private Stream<Arguments> data() {
            final String COMPANY_NAME0 = "EPAM Systems";
            final String COMPANY_NAME1 = "CROC";

            Map<String, Company> resourceCompanyMap0 = new HashMap<>();
            resourceCompanyMap0.put(COMPANY_NAME0.toLowerCase(), createCompany(0, COMPANY_NAME0));

            LoadResult<List<Company>> loadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );

            LoadResult<List<Company>> loadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(createCompany(1, COMPANY_NAME1)),
                    Collections.emptyList()
            );

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyMap(), new AtomicLong(-1), loadResult0),
                    arguments(List.of(
                                    createCompany(-1, COMPANY_NAME0)),
                            resourceCompanyMap0, new AtomicLong(0), loadResult0),
                    arguments(List.of(
                                    createCompany(-2, COMPANY_NAME1)),
                            resourceCompanyMap0, new AtomicLong(0), loadResult1),
                    arguments(List.of(
                                    createCompany(-1, COMPANY_NAME0),
                                    createCompany(-2, COMPANY_NAME1)),
                            resourceCompanyMap0, new AtomicLong(0), loadResult1),
                    arguments(List.of(
                                    createCompany(-1, COMPANY_NAME0),
                                    createCompany(-2, COMPANY_NAME1),
                                    createCompany(-3, COMPANY_NAME1)),
                            resourceCompanyMap0, new AtomicLong(0), loadResult1),
                    arguments(List.of(
                                    createCompany(-1, COMPANY_NAME0),
                                    createCompany(-2, COMPANY_NAME1),
                                    new Company(-3, Collections.emptyList())),
                            resourceCompanyMap0, new AtomicLong(0), loadResult1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void getCompanyLoadResult(List<Company> companies, Map<String, Company> resourceCompanyMap, AtomicLong lastCompanyId,
                                  LoadResult<List<Company>> expected) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.getCompanyLoadResult(Mockito.anyList(), Mockito.anyMap(), Mockito.any(AtomicLong.class)))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceCompany(Mockito.any(Company.class), Mockito.anyMap()))
                        .thenAnswer(
                                (Answer<Company>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Company company = (Company) args[0];
                                    Map<String, Company> rcp = (Map<String, Company>) args[1];

                                    return company.getName().stream()
                                            .map(localItem -> rcp.get(localItem.getText().toLowerCase()))
                                            .filter(Objects::nonNull)
                                            .findFirst()
                                            .orElse(null);
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.getCompanyLoadResult(companies, resourceCompanyMap, lastCompanyId));
            }
        }
    }

    @Test
    void fillCompanyIds() {
        Company company0 = new Company();
        company0.setId(0);

        Company company1 = new Company();
        company1.setId(1);

        Speaker speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setCompanies(List.of(company0));

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setCompanies(List.of(company0, company1));

        Speaker speaker2 = new Speaker();
        speaker1.setId(2);

        List<Long> expectedCompanyIds0 = List.of(0L);
        List<Long> expectedCompanyIds1 = List.of(0L, 1L);

        assertTrue(speaker0.getCompanyIds().isEmpty());
        assertTrue(speaker1.getCompanyIds().isEmpty());
        assertTrue(speaker2.getCompanyIds().isEmpty());

        ConferenceDataLoaderExecutor.fillCompanyIds(List.of(speaker0, speaker1));

        List<Long> actualCompanyIds0 = speaker0.getCompanyIds();
        List<Long> actualCompanyIds1 = speaker1.getCompanyIds();

        assertTrue(expectedCompanyIds0.containsAll(actualCompanyIds0) && actualCompanyIds0.containsAll(expectedCompanyIds0));
        assertTrue(expectedCompanyIds1.containsAll(actualCompanyIds1) && actualCompanyIds1.containsAll(expectedCompanyIds1));
        assertTrue(speaker2.getCompanyIds().isEmpty());
    }

    @Test
    void getResourceNameCompanySpeakerMap() {
        final String SPEAKER_NAME0 = "Name0";
        final String SPEAKER_NAME1 = "Name1";
        final String SPEAKER_NAME2 = "Name2";

        final String COMPANY_NAME0 = "EPAM Systems";
        final String COMPANY_NAME1 = "CROC";

        Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME0)));
        Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME1)));

        Speaker speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME0)));
        speaker0.setCompanies(List.of(company0));

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME1)));
        speaker1.setCompanies(List.of(company0, company1));

        Speaker speaker2 = new Speaker();
        speaker2.setId(2);
        speaker2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME2)));

        Map<NameCompany, Speaker> expected = new HashMap<>();
        expected.put(new NameCompany(SPEAKER_NAME0, company0), speaker0);
        expected.put(new NameCompany(SPEAKER_NAME1, company0), speaker1);
        expected.put(new NameCompany(SPEAKER_NAME1, company1), speaker1);

        assertEquals(expected, ConferenceDataLoaderExecutor.getResourceNameCompanySpeakerMap(List.of(speaker0, speaker1, speaker2)));
    }

    @Test
    void getResourceNameSpeakersMap() {
        final String SPEAKER_NAME0 = "Name0";
        final String SPEAKER_NAME1 = "Name1";

        Speaker speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME0)));

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME1)));

        Speaker speaker2 = new Speaker();
        speaker2.setId(2);
        speaker2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME1)));

        Map<String, Set<Speaker>> expected = new HashMap<>();
        expected.put(SPEAKER_NAME0, Set.of(speaker0));
        expected.put(SPEAKER_NAME1, Set.of(speaker1, speaker2));

        assertEquals(expected, ConferenceDataLoaderExecutor.getResourceNameSpeakersMap(List.of(speaker0, speaker1, speaker2)));
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerLoadResult method tests")
    class GetSpeakerLoadResultTest {
        final String PHOTO_FILE_NAME0 = "0000.jpg";
        final String PHOTO_FILE_NAME1 = "0001.jpg";
        final String PHOTO_FILE_NAME2 = "http://valid.com/2.jpg";
        final String WIDTH_PARAMETER_NAME = "w";

        private Stream<Arguments> data() {
            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setPhotoFileName(PHOTO_FILE_NAME0);

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setPhotoFileName(PHOTO_FILE_NAME1);

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);
            speaker2.setPhotoFileName(PHOTO_FILE_NAME2);

            SpeakerLoadMaps speakerLoadMaps = new SpeakerLoadMaps(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());

            SpeakerLoadResult speakerLoadResult0 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult1 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            List.of(speaker2),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            List.of(new UrlFilename(PHOTO_FILE_NAME2, "0000.jpg")),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult2 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(speaker0)),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(new UrlFilename(PHOTO_FILE_NAME0, PHOTO_FILE_NAME0))));

            SpeakerLoadResult speakerLoadResult3 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            return Stream.of(
                    arguments(Collections.emptyList(), speakerLoadMaps, new AtomicLong(-1), speakerLoadResult0, WIDTH_PARAMETER_NAME),
                    arguments(List.of(speaker2), speakerLoadMaps, new AtomicLong(-1), speakerLoadResult1, WIDTH_PARAMETER_NAME),
                    arguments(List.of(speaker0), speakerLoadMaps, new AtomicLong(-1), speakerLoadResult2, WIDTH_PARAMETER_NAME),
                    arguments(List.of(speaker1), speakerLoadMaps, new AtomicLong(-1), speakerLoadResult3, WIDTH_PARAMETER_NAME)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerLoadResult(List<Speaker> speakers, SpeakerLoadMaps speakerLoadMaps, AtomicLong lastSpeakerId,
                                  SpeakerLoadResult expected, String imageWidthParameterName) throws IOException {
            try (MockedStatic<ConferenceDataLoaderExecutor> conferenceDataLoaderExecutorMockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.needPhotoUpdate(
                                Mockito.nullable(ZonedDateTime.class), Mockito.nullable(ZonedDateTime.class), Mockito.nullable(String.class),
                                Mockito.nullable(String.class), Mockito.anyString()))
                        .thenAnswer(
                                (Answer<Boolean>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return PHOTO_FILE_NAME0.equals(args[3]);
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.needUpdate(Mockito.any(Speaker.class), Mockito.any(Speaker.class)))
                        .thenAnswer(
                                (Answer<Boolean>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return ((((Speaker) args[0]).getId() == 0) && (((Speaker) args[1]).getId() == 0));
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getSpeakerLoadResult(
                                Mockito.anyList(), Mockito.any(SpeakerLoadMaps.class), Mockito.any(AtomicLong.class), Mockito.anyString()))
                        .thenCallRealMethod();
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceSpeaker(Mockito.any(Speaker.class), Mockito.any(SpeakerLoadMaps.class)))
                        .thenAnswer(
                                (Answer<Speaker>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Speaker speaker = (Speaker) args[0];

                                    return ((speaker.getId() == 0) || (speaker.getId() == 1)) ? speaker : null;
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.getSpeakerLoadResult(speakers, speakerLoadMaps, lastSpeakerId, imageWidthParameterName));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillStringAttributeValue method tests")
    class FillStringAttributeValueTest {
        private Speaker createSpeaker(String twitter) {
            Speaker speaker = new Speaker();
            speaker.setTwitter(twitter);

            return speaker;
        }

        private Stream<Arguments> data() {
            final String RESOURCE_SPEAKER_TWITTER = "resourceSpeakerTwitter";
            final String TARGET_SPEAKER_TWITTER = "targetSpeakerTwitter";

            Speaker targetSpeaker0 = createSpeaker(null);
            Supplier<String> targetSupplier0 = targetSpeaker0::getTwitter;
            Consumer<String> targetConsumer0 = targetSpeaker0::setTwitter;
            Speaker resourceSpeaker0 = createSpeaker(null);
            Supplier<String> resourceSupplier0 = resourceSpeaker0::getTwitter;

            Speaker targetSpeaker1 = createSpeaker("");
            Supplier<String> targetSupplier1 = targetSpeaker1::getTwitter;
            Consumer<String> targetConsumer1 = targetSpeaker1::setTwitter;
            Speaker resourceSpeaker1 = createSpeaker(null);
            Supplier<String> resourceSupplier1 = resourceSpeaker1::getTwitter;

            Speaker targetSpeaker2 = createSpeaker(TARGET_SPEAKER_TWITTER);
            Supplier<String> targetSupplier2 = targetSpeaker2::getTwitter;
            Consumer<String> targetConsumer2 = targetSpeaker2::setTwitter;
            Speaker resourceSpeaker2 = createSpeaker(null);
            Supplier<String> resourceSupplier2 = resourceSpeaker2::getTwitter;

            Speaker targetSpeaker3 = createSpeaker(null);
            Supplier<String> targetSupplier3 = targetSpeaker3::getTwitter;
            Consumer<String> targetConsumer3 = targetSpeaker3::setTwitter;
            Speaker resourceSpeaker3 = createSpeaker("");
            Supplier<String> resourceSupplier3 = resourceSpeaker3::getTwitter;

            Speaker targetSpeaker4 = createSpeaker("");
            Supplier<String> targetSupplier4 = targetSpeaker4::getTwitter;
            Consumer<String> targetConsumer4 = targetSpeaker4::setTwitter;
            Speaker resourceSpeaker4 = createSpeaker("");
            Supplier<String> resourceSupplier4 = resourceSpeaker4::getTwitter;

            Speaker targetSpeaker5 = createSpeaker(TARGET_SPEAKER_TWITTER);
            Supplier<String> targetSupplier5 = targetSpeaker5::getTwitter;
            Consumer<String> targetConsumer5 = targetSpeaker5::setTwitter;
            Speaker resourceSpeaker5 = createSpeaker("");
            Supplier<String> resourceSupplier5 = resourceSpeaker5::getTwitter;

            Speaker targetSpeaker6 = createSpeaker(null);
            Supplier<String> targetSupplier6 = targetSpeaker6::getTwitter;
            Consumer<String> targetConsumer6 = targetSpeaker6::setTwitter;
            Speaker resourceSpeaker6 = createSpeaker(RESOURCE_SPEAKER_TWITTER);
            Supplier<String> resourceSupplier6 = resourceSpeaker6::getTwitter;

            Speaker targetSpeaker7 = createSpeaker("");
            Supplier<String> targetSupplier7 = targetSpeaker7::getTwitter;
            Consumer<String> targetConsumer7 = targetSpeaker7::setTwitter;
            Speaker resourceSpeaker7 = createSpeaker(RESOURCE_SPEAKER_TWITTER);
            Supplier<String> resourceSupplier7 = resourceSpeaker7::getTwitter;

            Speaker targetSpeaker8 = createSpeaker(TARGET_SPEAKER_TWITTER);
            Supplier<String> targetSupplier8 = targetSpeaker8::getTwitter;
            Consumer<String> targetConsumer8 = targetSpeaker8::setTwitter;
            Speaker resourceSpeaker8 = createSpeaker(RESOURCE_SPEAKER_TWITTER);
            Supplier<String> resourceSupplier8 = resourceSpeaker8::getTwitter;

            return Stream.of(
                    arguments(resourceSupplier0, targetSupplier0, targetConsumer0, null),
                    arguments(resourceSupplier1, targetSupplier1, targetConsumer1, ""),
                    arguments(resourceSupplier2, targetSupplier2, targetConsumer2, TARGET_SPEAKER_TWITTER),
                    arguments(resourceSupplier3, targetSupplier3, targetConsumer3, null),
                    arguments(resourceSupplier4, targetSupplier4, targetConsumer4, ""),
                    arguments(resourceSupplier5, targetSupplier5, targetConsumer5, TARGET_SPEAKER_TWITTER),
                    arguments(resourceSupplier6, targetSupplier6, targetConsumer6, RESOURCE_SPEAKER_TWITTER),
                    arguments(resourceSupplier7, targetSupplier7, targetConsumer7, RESOURCE_SPEAKER_TWITTER),
                    arguments(resourceSupplier8, targetSupplier8, targetConsumer8, TARGET_SPEAKER_TWITTER)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillStringAttributeValue(Supplier<String> resourceSupplier, Supplier<String> targetSupplier, Consumer<String> targetConsumer,
                                      String expected) {
            ConferenceDataLoaderExecutor.fillStringAttributeValue(resourceSupplier, targetSupplier, targetConsumer);

            assertEquals(expected, targetSupplier.get());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillBooleanAttributeValue method tests")
    class FillBooleanAttributeValueTest {
        private Speaker createSpeaker(boolean javaChampion) {
            Speaker speaker = new Speaker();
            speaker.setJavaChampion(javaChampion);

            return speaker;
        }

        Speaker resourceSpeaker0 = createSpeaker(false);
        BooleanSupplier resourceSupplier0 = resourceSpeaker0::isJavaChampion;

        Speaker targetSpeaker0 = createSpeaker(false);
        BooleanSupplier targetSupplier0 = targetSpeaker0::isJavaChampion;
        Consumer<Boolean> targetConsumer0 = targetSpeaker0::setJavaChampion;

        Speaker resourceSpeaker1 = createSpeaker(true);
        BooleanSupplier resourceSupplier1 = resourceSpeaker1::isJavaChampion;

        Speaker targetSpeaker1 = createSpeaker(false);
        BooleanSupplier targetSupplier1 = targetSpeaker1::isJavaChampion;
        Consumer<Boolean> targetConsumer1 = targetSpeaker1::setJavaChampion;

        Speaker resourceSpeaker2 = createSpeaker(false);
        BooleanSupplier resourceSupplier2 = resourceSpeaker2::isJavaChampion;

        Speaker targetSpeaker2 = createSpeaker(true);
        BooleanSupplier targetSupplier2 = targetSpeaker2::isJavaChampion;
        Consumer<Boolean> targetConsumer2 = targetSpeaker2::setJavaChampion;

        Speaker resourceSpeaker3 = createSpeaker(true);
        BooleanSupplier resourceSupplier3 = resourceSpeaker3::isJavaChampion;

        Speaker targetSpeaker3 = createSpeaker(true);
        BooleanSupplier targetSupplier3 = targetSpeaker3::isJavaChampion;
        Consumer<Boolean> targetConsumer3 = targetSpeaker3::setJavaChampion;

        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(resourceSupplier0, targetSupplier0, targetConsumer0, false),
                    arguments(resourceSupplier1, targetSupplier1, targetConsumer1, true),
                    arguments(resourceSupplier2, targetSupplier2, targetConsumer2, true),
                    arguments(resourceSupplier3, targetSupplier3, targetConsumer3, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillBooleanAttributeValue(BooleanSupplier resourceSupplier, BooleanSupplier targetSupplier, Consumer<Boolean> targetConsumer,
                                       boolean expected) {
            ConferenceDataLoaderExecutor.fillBooleanAttributeValue(resourceSupplier, targetSupplier, targetConsumer);

            assertEquals(expected, targetSupplier.getAsBoolean());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillLongAttributeValue method tests")
    class FillLongAttributeValueTest {
        private Topic createTopic(long id) {
            Topic topic = new Topic();
            topic.setId(id);

            return topic;
        }

        private EventType createEventType(Topic topic) {
            EventType eventType = new EventType();
            eventType.setTopic(topic);

            if (topic != null) {
                eventType.setTopicId(topic.getId());
            }

            return eventType;
        }

        private Stream<Arguments> data() {
            final long TARGET_TOPIC_ID1 = 1L;

            final long RESOURCE_TOPIC_ID2 = 2L;

            final long RESOURCE_TOPIC_ID3 = 3L;
            final long TARGET_TOPIC_ID3 = 4;

            // 0
            EventType resourceEventType0 = createEventType(null);
            Supplier<Long> resourceSupplier0 = resourceEventType0::getTopicId;

            EventType targetEventType0 = createEventType(null);
            Supplier<Long> targetSupplier0 = targetEventType0::getTopicId;
            LongConsumer targetConsumer0 = targetEventType0::setTopicId;

            // 1
            EventType resourceEventType1 = createEventType(null);
            Supplier<Long> resourceSupplier1 = resourceEventType1::getTopicId;

            Topic targetTopic1 = createTopic(TARGET_TOPIC_ID1);
            EventType targetEventType1 = createEventType(targetTopic1);
            Supplier<Long> targetSupplier1 = targetEventType1::getTopicId;
            LongConsumer targetConsumer1 = targetEventType1::setTopicId;

            // 2
            Topic resourceTopic2 = createTopic(RESOURCE_TOPIC_ID2);
            EventType resourceEventType2 = createEventType(resourceTopic2);
            Supplier<Long> resourceSupplier2 = resourceEventType2::getTopicId;

            EventType targetEventType2 = createEventType(null);
            Supplier<Long> targetSupplier2 = targetEventType2::getTopicId;
            LongConsumer targetConsumer2 = targetEventType2::setTopicId;

            // 3
            Topic resourceTopic3 = createTopic(RESOURCE_TOPIC_ID3);
            EventType resourceEventType3 = createEventType(resourceTopic3);
            Supplier<Long> resourceSupplier3 = resourceEventType3::getTopicId;

            Topic targetTopic3 = createTopic(TARGET_TOPIC_ID3);
            EventType targetEventType3 = createEventType(targetTopic3);
            Supplier<Long> targetSupplier3 = targetEventType3::getTopicId;
            LongConsumer targetConsumer3 = targetEventType3::setTopicId;

            return Stream.of(
                    arguments(resourceSupplier0, targetSupplier0, targetConsumer0, null),
                    arguments(resourceSupplier1, targetSupplier1, targetConsumer1, TARGET_TOPIC_ID1),
                    arguments(resourceSupplier2, targetSupplier2, targetConsumer2, RESOURCE_TOPIC_ID2),
                    arguments(resourceSupplier3, targetSupplier3, targetConsumer3, TARGET_TOPIC_ID3)
            );
        }


        @ParameterizedTest
        @MethodSource("data")
        void fillLongAttributeValue(Supplier<Long> resourceSupplier, Supplier<Long> targetSupplier, LongConsumer targetConsumer,
                                    Long expected) {
            ConferenceDataLoaderExecutor.fillLongAttributeValue(resourceSupplier, targetSupplier, targetConsumer);

            assertEquals(expected, targetSupplier.get());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillSpeakerMvp method tests")
    class FillSpeakerMvpTest {
        private Speaker createSpeaker(boolean mvp, boolean mvpReconnect) {
            Speaker speaker = new Speaker();
            speaker.setMvp(mvp);
            speaker.setMvpReconnect(mvpReconnect);

            return speaker;
        }

        private Speaker createSpeaker0() {
            return createSpeaker(false, false);
        }

        private Speaker createSpeaker1() {
            return createSpeaker(false, true);
        }

        private Speaker createSpeaker2() {
            return createSpeaker(true, false);
        }

        private Speaker createSpeaker3() {
            return createSpeaker(true, true);
        }

        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(createSpeaker0(), createSpeaker0(), false, false),
                    arguments(createSpeaker1(), createSpeaker0(), false, true),
                    arguments(createSpeaker2(), createSpeaker0(), true, false),
                    arguments(createSpeaker3(), createSpeaker0(), false, true),
                    arguments(createSpeaker0(), createSpeaker1(), false, true),
                    arguments(createSpeaker1(), createSpeaker1(), false, true),
                    arguments(createSpeaker2(), createSpeaker1(), true, false),
                    arguments(createSpeaker3(), createSpeaker1(), false, true),
                    arguments(createSpeaker0(), createSpeaker2(), true, false),
                    arguments(createSpeaker1(), createSpeaker2(), false, true),
                    arguments(createSpeaker2(), createSpeaker2(), true, false),
                    arguments(createSpeaker3(), createSpeaker2(), false, true),
                    arguments(createSpeaker0(), createSpeaker3(), false, true),
                    arguments(createSpeaker1(), createSpeaker3(), false, true),
                    arguments(createSpeaker2(), createSpeaker3(), true, false),
                    arguments(createSpeaker3(), createSpeaker3(), false, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillSpeakerMvp(Speaker targetSpeaker, Speaker resourceSpeaker,
                            boolean mvpExpected, boolean mvpReconnectExpected) {
            ConferenceDataLoaderExecutor.fillSpeakerMvp(targetSpeaker, resourceSpeaker);

            assertEquals(mvpExpected, targetSpeaker.isMvp());
            assertEquals(mvpReconnectExpected, targetSpeaker.isMvpReconnect());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillLocaleItemsAttributeValue method tests")
    class FillLocaleItemsAttributeValueTest {
        private Speaker createSpeaker(List<LocaleItem> name) {
            Speaker speaker = new Speaker();
            speaker.setName(name);

            return speaker;
        }

        private Stream<Arguments> data() {
            final List<LocaleItem> EMPTY_SPEAKER_NAME = Collections.emptyList();
            final List<LocaleItem> RESOURCE_SPEAKER_NAME = List.of(new LocaleItem(Language.ENGLISH.getCode(), "resourceSpeakerName"));
            final List<LocaleItem> TARGET_SPEAKER_NAME = List.of(new LocaleItem(Language.ENGLISH.getCode(), "targetSpeakerName"));

            Speaker targetSpeaker0 = createSpeaker(null);
            Supplier<List<LocaleItem>> targetSupplier0 = targetSpeaker0::getName;
            Consumer<List<LocaleItem>> targetConsumer0 = targetSpeaker0::setName;
            Speaker resourceSpeaker0 = createSpeaker(null);
            Supplier<List<LocaleItem>> resourceSupplier0 = resourceSpeaker0::getName;

            Speaker targetSpeaker1 = createSpeaker(EMPTY_SPEAKER_NAME);
            Supplier<List<LocaleItem>> targetSupplier1 = targetSpeaker1::getName;
            Consumer<List<LocaleItem>> targetConsumer1 = targetSpeaker1::setName;
            Speaker resourceSpeaker1 = createSpeaker(null);
            Supplier<List<LocaleItem>> resourceSupplier1 = resourceSpeaker1::getName;

            Speaker targetSpeaker2 = createSpeaker(TARGET_SPEAKER_NAME);
            Supplier<List<LocaleItem>> targetSupplier2 = targetSpeaker2::getName;
            Consumer<List<LocaleItem>> targetConsumer2 = targetSpeaker2::setName;
            Speaker resourceSpeaker2 = createSpeaker(null);
            Supplier<List<LocaleItem>> resourceSupplier2 = resourceSpeaker2::getName;

            Speaker targetSpeaker3 = createSpeaker(null);
            Supplier<List<LocaleItem>> targetSupplier3 = targetSpeaker3::getName;
            Consumer<List<LocaleItem>> targetConsumer3 = targetSpeaker3::setName;
            Speaker resourceSpeaker3 = createSpeaker(EMPTY_SPEAKER_NAME);
            Supplier<List<LocaleItem>> resourceSupplier3 = resourceSpeaker3::getName;

            Speaker targetSpeaker4 = createSpeaker(EMPTY_SPEAKER_NAME);
            Supplier<List<LocaleItem>> targetSupplier4 = targetSpeaker4::getName;
            Consumer<List<LocaleItem>> targetConsumer4 = targetSpeaker4::setName;
            Speaker resourceSpeaker4 = createSpeaker(EMPTY_SPEAKER_NAME);
            Supplier<List<LocaleItem>> resourceSupplier4 = resourceSpeaker4::getName;

            Speaker targetSpeaker5 = createSpeaker(TARGET_SPEAKER_NAME);
            Supplier<List<LocaleItem>> targetSupplier5 = targetSpeaker5::getName;
            Consumer<List<LocaleItem>> targetConsumer5 = targetSpeaker5::setName;
            Speaker resourceSpeaker5 = createSpeaker(EMPTY_SPEAKER_NAME);
            Supplier<List<LocaleItem>> resourceSupplier5 = resourceSpeaker5::getName;

            Speaker targetSpeaker6 = createSpeaker(null);
            Supplier<List<LocaleItem>> targetSupplier6 = targetSpeaker6::getName;
            Consumer<List<LocaleItem>> targetConsumer6 = targetSpeaker6::setName;
            Speaker resourceSpeaker6 = createSpeaker(RESOURCE_SPEAKER_NAME);
            Supplier<List<LocaleItem>> resourceSupplier6 = resourceSpeaker6::getName;

            Speaker targetSpeaker7 = createSpeaker(EMPTY_SPEAKER_NAME);
            Supplier<List<LocaleItem>> targetSupplier7 = targetSpeaker7::getName;
            Consumer<List<LocaleItem>> targetConsumer7 = targetSpeaker7::setName;
            Speaker resourceSpeaker7 = createSpeaker(RESOURCE_SPEAKER_NAME);
            Supplier<List<LocaleItem>> resourceSupplier7 = resourceSpeaker7::getName;

            Speaker targetSpeaker8 = createSpeaker(TARGET_SPEAKER_NAME);
            Supplier<List<LocaleItem>> targetSupplier8 = targetSpeaker8::getName;
            Consumer<List<LocaleItem>> targetConsumer8 = targetSpeaker8::setName;
            Speaker resourceSpeaker8 = createSpeaker(RESOURCE_SPEAKER_NAME);
            Supplier<List<LocaleItem>> resourceSupplier8 = resourceSpeaker8::getName;

            return Stream.of(
                    arguments(resourceSupplier0, targetSupplier0, targetConsumer0, null),
                    arguments(resourceSupplier1, targetSupplier1, targetConsumer1, EMPTY_SPEAKER_NAME),
                    arguments(resourceSupplier2, targetSupplier2, targetConsumer2, TARGET_SPEAKER_NAME),
                    arguments(resourceSupplier3, targetSupplier3, targetConsumer3, null),
                    arguments(resourceSupplier4, targetSupplier4, targetConsumer4, EMPTY_SPEAKER_NAME),
                    arguments(resourceSupplier5, targetSupplier5, targetConsumer5, TARGET_SPEAKER_NAME),
                    arguments(resourceSupplier6, targetSupplier6, targetConsumer6, RESOURCE_SPEAKER_NAME),
                    arguments(resourceSupplier7, targetSupplier7, targetConsumer7, RESOURCE_SPEAKER_NAME),
                    arguments(resourceSupplier8, targetSupplier8, targetConsumer8, TARGET_SPEAKER_NAME)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillLocaleItemsAttributeValue(Supplier<List<LocaleItem>> resourceSupplier, Supplier<List<LocaleItem>> targetSupplier, Consumer<List<LocaleItem>> targetConsumer,
                                           List<LocaleItem> expected) {
            ConferenceDataLoaderExecutor.fillLocaleItemsAttributeValue(resourceSupplier, targetSupplier, targetConsumer);

            assertEquals(expected, targetSupplier.get());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillUpdatedAt method tests")
    class FillUpdatedAtTest {
        private Speaker createSpeaker(ZonedDateTime photoUpdatedAt) {
            Speaker speaker = new Speaker();
            speaker.setPhotoUpdatedAt(photoUpdatedAt);

            return speaker;
        }

        private Stream<Arguments> data() {
            final ZonedDateTime NOW = ZonedDateTime.now();
            final ZonedDateTime YESTERDAY = NOW.minus(1, ChronoUnit.DAYS);

            return Stream.of(
                    arguments(createSpeaker(null), createSpeaker(null), null),
                    arguments(createSpeaker(null), createSpeaker(NOW), null),
                    arguments(createSpeaker(NOW), createSpeaker(null), NOW),
                    arguments(createSpeaker(NOW), createSpeaker(NOW), NOW),
                    arguments(createSpeaker(YESTERDAY), createSpeaker(NOW), NOW),
                    arguments(createSpeaker(NOW), createSpeaker(YESTERDAY), NOW)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillUpdatedAt(Speaker targetSpeaker, Speaker resourceSpeaker, ZonedDateTime expected) {
            ConferenceDataLoaderExecutor.fillUpdatedAt(targetSpeaker, resourceSpeaker);

            assertEquals(expected, targetSpeaker.getPhotoUpdatedAt());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillSpeakerIds method tests")
    class FillSpeakerIdsTest {
        private Stream<Arguments> data() {
            Speaker speaker0 = new Speaker();
            speaker0.setId(0);

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);

            Speaker speaker3 = new Speaker();
            speaker3.setId(3);

            Talk talk0 = new Talk();
            talk0.setSpeakers(List.of(speaker0));

            Talk talk1 = new Talk();
            talk1.setSpeakers(List.of(speaker1, speaker2));

            Talk talk2 = new Talk();
            talk2.setSpeakers(List.of(speaker3));

            return Stream.of(
                    arguments(Collections.emptyList(), Collections.emptyList()),
                    arguments(List.of(talk0), List.of(List.of(0L))),
                    arguments(List.of(talk1), List.of(List.of(1L, 2L))),
                    arguments(List.of(talk1, talk2), List.of(List.of(1L, 2L), List.of(3L)))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillSpeakerIds(List<Talk> talks, List<List<Long>> expected) {
            ConferenceDataLoaderExecutor.fillSpeakerIds(talks);

            for (int i = 0; i < talks.size(); i++) {
                assertEquals(
                        expected.get(i),
                        talks.get(i).getSpeakerIds());
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getTalkLoadResult method tests")
    class GetTalkLoadResultTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);

            Talk talk1 = new Talk();
            talk1.setId(1);
            talk1.setName(List.of(
                    new LocaleItem(Language.RUSSIAN.getCode(), "Наименование1"),
                    new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

            Talk talk2 = new Talk();
            talk2.setId(2);
            talk2.setName(List.of(
                    new LocaleItem(Language.RUSSIAN.getCode(), "Наименование2"),
                    new LocaleItem(Language.ENGLISH.getCode(), "Name2")));

            Event resourceEvent = new Event();
            resourceEvent.setTalks(List.of(talk1, talk2));

            List<Event> resourceEvents = Collections.emptyList();

            LoadResult<List<Talk>> talkLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(talk0),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult2 = new LoadResult<>(
                    List.of(talk1, talk2),
                    List.of(talk2),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult3 = new LoadResult<>(
                    List.of(talk1, talk2),
                    Collections.emptyList(),
                    List.of(talk0));

            return Stream.of(
                    arguments(Collections.emptyList(), null, resourceEvents, new AtomicLong(-1), talkLoadResult0),
                    arguments(List.of(talk0), null, resourceEvents, new AtomicLong(-1), talkLoadResult1),
                    arguments(List.of(talk2), resourceEvent, resourceEvents, new AtomicLong(-1), talkLoadResult2),
                    arguments(List.of(talk0, talk1), resourceEvent, resourceEvents, new AtomicLong(-1), talkLoadResult3)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getTalkLoadResult(List<Talk> talks, Event resourceEvent, List<Event> resourceEvents,
                               AtomicLong lasTalksId, LoadResult<List<Talk>> expected) {
            try (MockedStatic<ConferenceDataLoaderExecutor> conferenceDataLoaderExecutorMockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.needUpdate(Mockito.any(Talk.class), Mockito.any(Talk.class)))
                        .thenAnswer(
                                (Answer<Boolean>) invocation -> {
                                    Object[] args = invocation.getArguments();

                                    return ((((Talk) args[0]).getId() == 0) && (((Talk) args[1]).getId() == 0));
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.getTalkLoadResult(
                                Mockito.anyList(), Mockito.nullable(Event.class), Mockito.anyList(), Mockito.any(AtomicLong.class)))
                        .thenCallRealMethod();
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceTalk(Mockito.any(Talk.class), Mockito.anyMap(), Mockito.anyMap()))
                        .thenAnswer(
                                (Answer<Talk>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Talk talk = (Talk) args[0];

                                    return ((talk.getId() == 0) || (talk.getId() == 1)) ? talk : null;
                                }
                        );
                conferenceDataLoaderExecutorMockedStatic.when(() -> ConferenceDataLoaderExecutor.needDeleteTalk(
                                Mockito.anyList(), Mockito.any(Talk.class), Mockito.anyList(), Mockito.any(Event.class)))
                        .thenReturn(true);

                assertEquals(expected, ConferenceDataLoaderExecutor.getTalkLoadResult(talks, resourceEvent, resourceEvents, lasTalksId));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needDeleteTalk method tests")
    class NeedDeleteTalkTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);

            Talk talk1 = new Talk();
            talk1.setId(1);

            EventType eventType0 = new EventType();
            eventType0.setId(0);

            EventType eventType1 = new EventType();
            eventType1.setId(1);

            Event event0 = new Event();
            event0.setId(0);
            event0.setEventType(eventType0);
            event0.setTalks(List.of(talk0));

            Event event1 = new Event();
            event1.setId(1);
            event1.setEventType(eventType1);

            return Stream.of(
                    arguments(List.of(talk0), talk0, Collections.emptyList(), null, false),
                    arguments(Collections.emptyList(), talk0, List.of(event0), event0, true),
                    arguments(Collections.emptyList(), talk0, List.of(event0), event1, false)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needDeleteTalk(List<Talk> talks, Talk resourceTalk, List<Event> resourceEvents, Event resourceEvent,
                            boolean expected) {
            try (MockedStatic<LocalizationUtils> mockedStatic = Mockito.mockStatic(LocalizationUtils.class)) {
                mockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenReturn("");

                assertEquals(expected, ConferenceDataLoaderExecutor.needDeleteTalk(talks, resourceTalk, resourceEvents, resourceEvent));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getPlaceLoadResult method tests")
    class GetPlaceLoadResultTest {
        private static Place createPlace(long id, String city, String venueAddress) {
            return new Place(
                    id,
                    List.of(new LocaleItem(Language.ENGLISH.getCode(), city)),
                    List.of(new LocaleItem(Language.ENGLISH.getCode(), venueAddress)),
                    null
            );
        }

        private static Place createPlace(long id, String city) {
            return createPlace(id, city, "");
        }

        private static EventDays createEventDays(long id, String city, String venueAddress) {
            return new EventDays(null, null, createPlace(id, city, venueAddress));
        }

        private static EventDays createEventDays(long id, String city) {
            return createEventDays(id, city, "");
        }

        private Stream<Arguments> data() {
            List<Place> resourcePlaces = List.of(
                    createPlace(0, "Online"),
                    createPlace(1, "Moscow")
            );

            LoadResult<List<Place>> placeLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(createPlace(43, "City0")),
                    Collections.emptyList());

            LoadResult<List<Place>> placeLoadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Place>> placeLoadResult2 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    List.of(createPlace(1, "Moscow", "Tverskaya")));

            LoadResult<List<Place>> placeLoadResult3 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(createPlace(43, "City0"), createPlace(44, "City1")),
                    List.of(createPlace(1, "Moscow", "Tverskaya")));

            return Stream.of(
                    arguments(List.of(
                            createEventDays(-1, "City0")
                    ), resourcePlaces, new AtomicLong(42), placeLoadResult0),
                    arguments(List.of(
                            createEventDays(0, "")
                    ), resourcePlaces, new AtomicLong(42), placeLoadResult1),
                    arguments(List.of(
                            createEventDays(-1, "Moscow", "Tverskaya")
                    ), resourcePlaces, new AtomicLong(42), placeLoadResult2),
                    arguments(List.of(
                            createEventDays(-1, "Moscow")
                    ), resourcePlaces, new AtomicLong(42), placeLoadResult1),
                    arguments(List.of(
                            createEventDays(-1, "City0"),
                            createEventDays(-1, "City1"),
                            createEventDays(-1, "Moscow", "Tverskaya")
                    ), resourcePlaces, new AtomicLong(42), placeLoadResult3),
                    arguments(Collections.emptyList(), resourcePlaces, new AtomicLong(42), placeLoadResult1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void getPlaceLoadResult(List<EventDays> eventDaysList, List<Place> resourcePlaces, AtomicLong lastPlaceId, LoadResult<List<Place>> expected) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.getPlaceLoadResult(Mockito.anyList(), Mockito.anyList(), Mockito.any()))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.fixVenueAddress(Mockito.any(Place.class)))
                        .thenAnswer(
                                (Answer<List<LocaleItem>>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Place place = (Place) args[0];

                                    return place.getVenueAddress();
                                }
                        );
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourcePlace(Mockito.any(Place.class), Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap()))
                        .thenAnswer(
                                (Answer<Place>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Place place = (Place) args[0];
                                    Map<Long, Place> resourceIdPlaces = (Map<Long, Place>) args[1];
                                    Map<CityVenueAddress, Place> resourceEnCityVenueAddressPlaces = (Map<CityVenueAddress, Place>) args[3];

                                    if (place.getId() >= 0) {
                                        return resourceIdPlaces.get(place.getId());
                                    } else {
                                        String city = LocalizationUtils.getString(place.getCity(), Language.ENGLISH);
                                        CityVenueAddress cityVenueAddress = new CityVenueAddress(city, "");

                                        return resourceEnCityVenueAddressPlaces.get(cityVenueAddress);
                                    }
                                }
                        );
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.needUpdate(Mockito.any(Place.class), Mockito.any(Place.class)))
                        .thenAnswer(
                                (Answer<Boolean>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Place a = (Place) args[0];
                                    Place b = (Place) args[1];
                                    String aCity = LocalizationUtils.getString(a.getCity(), Language.ENGLISH);
                                    String bCity = LocalizationUtils.getString(b.getCity(), Language.ENGLISH);
                                    String aVenueAddress = LocalizationUtils.getString(a.getVenueAddress(), Language.ENGLISH);
                                    String bVenueAddress = LocalizationUtils.getString(b.getVenueAddress(), Language.ENGLISH);

                                    return !((a.getId() == b.getId()) && aCity.equals(bCity) && aVenueAddress.equals(bVenueAddress));
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.getPlaceLoadResult(eventDaysList, resourcePlaces, lastPlaceId));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventLoadResult method tests")
    class GetEventLoadResultTest {
        private Stream<Arguments> data() {
            Event event0 = new Event();
            event0.setId(0);

            Event event1 = new Event();
            event1.setId(1);

            LoadResult<Event> eventLoadResult0 = new LoadResult<>(
                    null,
                    event0,
                    null);

            LoadResult<Event> eventLoadResult1 = new LoadResult<>(
                    null,
                    null,
                    event0);

            LoadResult<Event> eventLoadResult2 = new LoadResult<>(
                    null,
                    null,
                    null);

            return Stream.of(
                    arguments(event0, null, eventLoadResult0),
                    arguments(event0, event0, eventLoadResult1),
                    arguments(event0, event1, eventLoadResult2)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEventLoadResult(Event event, Event resourceEvent, LoadResult<Event> expected) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.getEventLoadResult(Mockito.nullable(Event.class), Mockito.nullable(Event.class)))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.needUpdate(Mockito.any(Event.class), Mockito.any(Event.class)))
                        .thenAnswer(
                                (Answer<Boolean>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Event a = (Event) args[0];
                                    Event b = (Event) args[1];

                                    return ((a.getId() == 0) && (b.getId() == 0));
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.getEventLoadResult(event, resourceEvent));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fillEventTimeZone method tests")
    class FillEventTimeZoneTest {
        Event createEvent(String timeZone) {
            Event event = new Event();

            if (timeZone != null) {
                event.setTimeZone(timeZone);
            }

            return event;
        }

        private Stream<Arguments> data() {
            final String TIME_ZONE0 = "Europe/Moscow";
            final String TIME_ZONE1 = "Asia/Novosibirsk";

            return Stream.of(
                    arguments(createEvent(null), createEvent(null), null),
                    arguments(createEvent(null), createEvent(""), null),
                    arguments(createEvent(null), createEvent(TIME_ZONE0), TIME_ZONE0),

                    arguments(createEvent(""), createEvent(null), ""),
                    arguments(createEvent(""), createEvent(""), ""),
                    arguments(createEvent(""), createEvent(TIME_ZONE0), TIME_ZONE0),

                    arguments(createEvent(TIME_ZONE1), createEvent(null), TIME_ZONE1),
                    arguments(createEvent(TIME_ZONE1), createEvent(""), TIME_ZONE1),
                    arguments(createEvent(TIME_ZONE1), createEvent(TIME_ZONE0), TIME_ZONE1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fillEventTimeZone(Event targetEvent, Event resourceEvent, String expected) {
            ConferenceDataLoaderExecutor.fillEventTimeZone(targetEvent, resourceEvent);

            assertEquals(expected, targetEvent.getTimeZone());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("saveFiles method tests")
    class SaveFilesTest {
        private Stream<Arguments> data() {
            Company company0 = new Company();
            Speaker speaker0 = new Speaker();
            UrlFilename urlFilename0 = new UrlFilename("url0", "filename0");
            Talk talk0 = new Talk();
            Place place0 = new Place();
            Event event0 = new Event();

            LoadResult<List<Company>> companyLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Company>> companyLoadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(company0),
                    Collections.emptyList());

            SpeakerLoadResult speakerLoadResult0 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult1 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            List.of(speaker0),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult2 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(speaker0)),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult3 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            List.of(urlFilename0),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult4 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(urlFilename0)));

            LoadResult<List<Talk>> talkLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult1 = new LoadResult<>(
                    List.of(talk0),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult2 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(talk0),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult3 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    List.of(talk0));

            LoadResult<List<Place>> placeLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Place>> placeLoadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(place0),
                    Collections.emptyList());

            LoadResult<List<Place>> placeLoadResult2 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    List.of(place0));

            LoadResult<Event> eventLoadResult0 = new LoadResult<>(
                    null,
                    null,
                    null);

            LoadResult<Event> eventLoadResult1 = new LoadResult<>(
                    null,
                    event0,
                    null);

            LoadResult<Event> eventLoadResult2 = new LoadResult<>(
                    null,
                    null,
                    event0);

            List<Arguments> argumentsList = new ArrayList<>();

            for (LoadResult<Event> eventLoadResult : List.of(eventLoadResult0, eventLoadResult1, eventLoadResult2)) {
                for (LoadResult<List<Place>> placeLoadResult : List.of(placeLoadResult0, placeLoadResult1, placeLoadResult2)) {
                    for (LoadResult<List<Talk>> talkLoadResult : List.of(talkLoadResult0, talkLoadResult1, talkLoadResult2, talkLoadResult3)) {
                        for (SpeakerLoadResult speakerLoadResult : List.of(speakerLoadResult0, speakerLoadResult1, speakerLoadResult2, speakerLoadResult3, speakerLoadResult4)) {
                            for (LoadResult<List<Company>> companyLoadResult : List.of(companyLoadResult0, companyLoadResult1)) {
                                argumentsList.add(arguments(companyLoadResult, speakerLoadResult, talkLoadResult, placeLoadResult, eventLoadResult, "w"));
                            }
                        }
                    }
                }
            }

            return Stream.of(argumentsList.toArray(new Arguments[0]));
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void saveFiles(LoadResult<List<Company>> companyLoadResult, SpeakerLoadResult speakerLoadResult, LoadResult<List<Talk>> talkLoadResult,
                       LoadResult<List<Place>> placeLoadResult, LoadResult<Event> eventLoadResult, String imageWidthParameterName) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.saveFiles(
                                Mockito.any(LoadResult.class), Mockito.any(SpeakerLoadResult.class), Mockito.any(LoadResult.class),
                                Mockito.any(LoadResult.class), Mockito.any(LoadResult.class), Mockito.anyString()))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveFiles(companyLoadResult, speakerLoadResult,
                        talkLoadResult, placeLoadResult, eventLoadResult, imageWidthParameterName));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("saveCompanies method tests")
    class SaveCompaniesTest {
        private Stream<Arguments> data() {
            Company company0 = new Company(0, Collections.emptyList());
            Company company1 = new Company(1, Collections.emptyList());

            LoadResult<List<Company>> companyLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );

            LoadResult<List<Company>> companyLoadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(company0, company1),
                    Collections.emptyList()
            );

            return Stream.of(
                    arguments(companyLoadResult0),
                    arguments(companyLoadResult1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void saveSpeakers(LoadResult<List<Company>> companyLoadResult) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.saveCompanies(Mockito.any(LoadResult.class)))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveCompanies(companyLoadResult));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("saveImages method tests")
    class SaveImagesTest {
        private Stream<Arguments> data() {
            UrlFilename urlFilename0 = new UrlFilename("url0", "filename0");
            UrlFilename urlFilename1 = new UrlFilename("url1", "filename1");
            String WIDTH_PARAMETER_NAME = "w";

            SpeakerLoadResult speakerLoadResult0 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult1 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            List.of(urlFilename0),
                            List.of(urlFilename1)));

            return Stream.of(
                    arguments(speakerLoadResult0, WIDTH_PARAMETER_NAME),
                    arguments(speakerLoadResult1, WIDTH_PARAMETER_NAME)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void saveImages(SpeakerLoadResult speakerLoadResult, String imageWidthParameterName) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.saveImages(Mockito.any(SpeakerLoadResult.class), Mockito.anyString()))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveImages(speakerLoadResult, imageWidthParameterName));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("saveSpeakers method tests")
    class SaveSpeakersTest {
        private Stream<Arguments> data() {
            Speaker speaker0 = new Speaker();
            Speaker speaker1 = new Speaker();

            SpeakerLoadResult speakerLoadResult0 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            SpeakerLoadResult speakerLoadResult1 = new SpeakerLoadResult(
                    new LoadResult<>(
                            Collections.emptyList(),
                            List.of(speaker0),
                            List.of(speaker1)),
                    new LoadResult<>(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()));

            return Stream.of(
                    arguments(speakerLoadResult0),
                    arguments(speakerLoadResult1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void saveSpeakers(SpeakerLoadResult speakerLoadResult) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.saveSpeakers(Mockito.any(SpeakerLoadResult.class)))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveSpeakers(speakerLoadResult));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("saveTalks method tests")
    class SaveTalksTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            Talk talk1 = new Talk();
            Talk talk2 = new Talk();

            LoadResult<List<Talk>> talkLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Talk>> talkLoadResult1 = new LoadResult<>(
                    List.of(talk0),
                    List.of(talk1),
                    List.of(talk2));

            return Stream.of(
                    arguments(talkLoadResult0),
                    arguments(talkLoadResult1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void saveTalks(LoadResult<List<Talk>> talkLoadResult) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.saveTalks(Mockito.any(LoadResult.class)))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveTalks(talkLoadResult));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("savePlaces method tests")
    class SavePlacesTest {
        private Stream<Arguments> data() {
            Place place0 = new Place();
            Place place1 = new Place();

            LoadResult<List<Place>> placeLoadResult0 = new LoadResult<>(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList());

            LoadResult<List<Place>> placeLoadResult1 = new LoadResult<>(
                    Collections.emptyList(),
                    List.of(place0),
                    List.of(place1));

            return Stream.of(
                    arguments(placeLoadResult0),
                    arguments(placeLoadResult1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void savePlaces(LoadResult<List<Place>> placeLoadResult) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.savePlaces(Mockito.any(LoadResult.class)))
                        .thenCallRealMethod();

                assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.savePlaces(placeLoadResult));
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @DisplayName("saveEvents method tests")
        class SaveEventsTest {
            private Stream<Arguments> data() {
                Event event0 = new Event();
                Event event1 = new Event();

                LoadResult<Event> eventLoadResult0 = new LoadResult<>(
                        null,
                        null,
                        null);

                LoadResult<Event> eventLoadResult1 = new LoadResult<>(
                        null,
                        event0,
                        event1);

                return Stream.of(
                        arguments(eventLoadResult0),
                        arguments(eventLoadResult1)
                );
            }

            @ParameterizedTest
            @MethodSource("data")
            @SuppressWarnings("unchecked")
            void saveEvents(LoadResult<Event> eventLoadResult) {
                try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                    mockedStatic.when(() -> ConferenceDataLoaderExecutor.saveEvents(Mockito.any(LoadResult.class)))
                            .thenCallRealMethod();

                    assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveEvents(eventLoadResult));
                }
            }
        }
    }

    @Test
    void logAndSaveEventTypes() {
        try (MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
             MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class)
        ) {
            localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                    .thenReturn("");

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.logAndSaveEventTypes(List.of(new EventType()), "{}", "filename"));
        }
    }

    @Test
    void logAndSaveCompanies() {
        try (MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
             MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class)
        ) {
            localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                    .thenReturn("");

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.logAndSaveCompanies(List.of(new Company()), "{}", "filename"));
        }
    }

    @Test
    void logAndCreateSpeakerImages() {
        try (MockedStatic<ImageUtils> mockedStatic = Mockito.mockStatic(ImageUtils.class)) {
            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.logAndCreateSpeakerImages(List.of(new UrlFilename("url", "filename")), "{}", "w"));
        }
    }

    @Test
    void logAndSaveSpeakers() {
        try (MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
             MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class)
        ) {
            localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                    .thenReturn("");

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.logAndSaveSpeakers(List.of(new Speaker()), "{}", "filename"));
        }
    }

    @Test
    void logAndSaveTalks() {
        try (MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
             MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class)
        ) {
            localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                    .thenReturn("");

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.logAndSaveTalks(List.of(new Talk()), "{}", "filename"));
        }
    }

    @Test
    void logAndSavePlaces() {
        try (MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
             MockedStatic<YamlUtils> yamlUtilsMockedStatic = Mockito.mockStatic(YamlUtils.class)
        ) {
            localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                    .thenReturn("");

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.logAndSavePlaces(List.of(new Place()), "{}", "filename"));
        }
    }

    @Test
    void saveEvent() {
        try (MockedStatic<YamlUtils> mockedStatic = Mockito.mockStatic(YamlUtils.class)) {
            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.saveEvent(new Event(), "filename"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourceCompany method tests")
    class FindResourceCompanyTest {
        private Stream<Arguments> data() {
            final String COMPANY_NAME0 = "Company0";
            final String COMPANY_NAME1 = "Company1";

            Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME0)));
            Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME0)));
            Company company2 = new Company(2, List.of(new LocaleItem(Language.ENGLISH.getCode(), COMPANY_NAME1)));

            Map<String, Company> resourceCompanyMap = Map.of(COMPANY_NAME0.toLowerCase(), company0);

            return Stream.of(
                    arguments(company0, resourceCompanyMap, company0),
                    arguments(company1, resourceCompanyMap, company0),
                    arguments(company2, resourceCompanyMap, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void findResourceCompany(Company company, Map<String, Company> resourceCompanyMap, Company expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.findResourceCompany(company, resourceCompanyMap));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourceSpeaker method tests")
    class FindResourceSpeakerTest {
        private Stream<Arguments> data() {
            final String SPEAKER_NAME0 = "Имя0";
            final String SPEAKER_NAME1 = "Имя1";
            final String SPEAKER_NAME2 = "Имя2";
            final String SPEAKER_NAME3 = "Имя3";
            final String SPEAKER_NAME4 = "Имя4";
            final String SPEAKER_NAME5 = "Имя5";
            final String SPEAKER_NAME6 = "Имя6";
            final String SPEAKER_NAME7 = "Имя7";
            final String SPEAKER_NAME8 = "Имя8";
            final String COMPANY_NAME0 = "Компания0";
            final String COMPANY_NAME1 = "Компания1";
            final String COMPANY_NAME2 = "Компания2";
            final String COMPANY_NAME3 = "Компания3";
            final String COMPANY_NAME4 = "Компания4";
            final String COMPANY_NAME5 = "Компания5";

            Company company0 = new Company(0, List.of(new LocaleItem(Language.RUSSIAN.getCode(), COMPANY_NAME0)));
            Company company1 = new Company(1, List.of(new LocaleItem(Language.RUSSIAN.getCode(), COMPANY_NAME1)));
            Company company2 = new Company(2, List.of(new LocaleItem(Language.RUSSIAN.getCode(), COMPANY_NAME2)));
            Company company3 = new Company(3, List.of(new LocaleItem(Language.RUSSIAN.getCode(), COMPANY_NAME3)));
            Company company4 = new Company(4, List.of(new LocaleItem(Language.RUSSIAN.getCode(), COMPANY_NAME4)));
            Company company5 = new Company(5, List.of(new LocaleItem(Language.RUSSIAN.getCode(), COMPANY_NAME5)));

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME0)));
            speaker0.setCompanies(List.of(company0));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME1)));
            speaker1.setCompanies(List.of(company1));

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);
            speaker2.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME2)));
            speaker2.setCompanies(List.of(company2));

            Speaker speaker3 = new Speaker();
            speaker3.setId(3);
            speaker3.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME3)));
            speaker3.setCompanies(List.of(company3));

            Speaker speaker4 = new Speaker();
            speaker4.setId(4);
            speaker4.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME4)));
            speaker4.setCompanies(List.of(company4));

            Speaker speaker5 = new Speaker();
            speaker5.setId(5);
            speaker5.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME5)));
            speaker5.setCompanies(List.of(company5));

            Speaker speaker6 = new Speaker();
            speaker6.setId(6);
            speaker6.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME6)));
            speaker6.setCompanies(Collections.singletonList(null));

            Speaker speaker7 = new Speaker();
            speaker7.setId(7);
            speaker7.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME7)));
            speaker7.setCompanies(null);

            Speaker speaker8 = new Speaker();
            speaker8.setId(8);
            speaker8.setName(List.of(new LocaleItem(Language.RUSSIAN.getCode(), SPEAKER_NAME8)));
            speaker8.setCompanies(Collections.emptyList());

            NameCompany nameCompany0 = new NameCompany(SPEAKER_NAME0, company0);
            NameCompany nameCompany1 = new NameCompany(SPEAKER_NAME1, company1);
            NameCompany nameCompany6 = new NameCompany(SPEAKER_NAME6, null);
            NameCompany nameCompany7 = new NameCompany(SPEAKER_NAME7, null);
            NameCompany nameCompany8 = new NameCompany(SPEAKER_NAME8, null);

            SpeakerLoadMaps speakerLoadMaps = new SpeakerLoadMaps(
                    Map.of(nameCompany0, 0L, nameCompany1, 1L, nameCompany6, 6L, nameCompany7, 7L, nameCompany8, 8L),
                    Map.of(0L, speaker0, 7L, speaker7, 8L, speaker8),
                    Collections.emptyMap(),
                    Collections.emptyMap());

            return Stream.of(
                    arguments(speaker0, speakerLoadMaps, null),
                    arguments(speaker1, speakerLoadMaps, NullPointerException.class),
                    arguments(speaker2, speakerLoadMaps, null),
                    arguments(speaker3, speakerLoadMaps, null),
                    arguments(speaker4, speakerLoadMaps, null),
                    arguments(speaker5, speakerLoadMaps, null),
                    arguments(speaker6, speakerLoadMaps, NullPointerException.class),
                    arguments(speaker7, speakerLoadMaps, null),
                    arguments(speaker8, speakerLoadMaps, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void findResourceSpeaker(Speaker speaker, SpeakerLoadMaps speakerLoadMaps, Class<? extends Throwable> expectedException) {
            try (MockedStatic<ConferenceDataLoaderExecutor> conferenceDataLoaderMockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class);
                 MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class)) {
                conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceSpeaker(Mockito.any(Speaker.class), Mockito.any(SpeakerLoadMaps.class)))
                        .thenCallRealMethod();
                conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceSpeakerByNameCompany(Mockito.any(Speaker.class), Mockito.anyMap()))
                        .thenAnswer(
                                (Answer<Speaker>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Speaker localSpeaker = (Speaker) args[0];

                                    return ((localSpeaker.getId() == 2) || (localSpeaker.getId() == 3)) ? localSpeaker : null;
                                }
                        );
                conferenceDataLoaderMockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceSpeakerByName(Mockito.any(Speaker.class), Mockito.anyMap()))
                        .thenAnswer(
                                (Answer<Speaker>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Speaker localSpeaker = (Speaker) args[0];

                                    return ((localSpeaker.getId() == 4) || (localSpeaker.getId() == 5)) ? localSpeaker : null;
                                }
                        );
                localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<String>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    List<LocaleItem> localeItems = (List<LocaleItem>) args[0];

                                    return ((localeItems != null) && !localeItems.isEmpty()) ? localeItems.get(0).getText() : null;
                                }
                        );

                if (expectedException == null) {
                    assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.findResourceSpeaker(speaker, speakerLoadMaps));
                } else {
                    assertThrows(expectedException, () -> ConferenceDataLoaderExecutor.findResourceSpeaker(speaker, speakerLoadMaps));
                }
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourceTalk method tests")
    class FindResourceTalkTest {
        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);

            Talk talk1 = new Talk();
            talk1.setId(1);

            return Stream.of(
                    arguments(talk0, Collections.emptyMap(), Collections.emptyMap(), talk0),
                    arguments(talk1, Collections.emptyMap(), Collections.emptyMap(), null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void findResourceTalk(Talk talk, Map<String, Set<Talk>> resourceRuNameTalks, Map<String, Set<Talk>> resourceEnNameTalks,
                              Talk expected) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceTalk(Mockito.any(Talk.class), Mockito.anyMap(), Mockito.anyMap()))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourceTalkByName(Mockito.any(Talk.class), Mockito.anyMap(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<Talk>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Talk localTalk = (Talk) args[0];

                                    return (localTalk.getId() == 0) ? localTalk : null;
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.findResourceTalk(talk, resourceRuNameTalks, resourceEnNameTalks));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourceSpeakerByNameCompany method tests")
    class FindResourceSpeakerByNameCompanyTest {
        private Stream<Arguments> data() {
            Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company0")));
            Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company1")));

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            speaker0.setCompanies(List.of(company0));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
            speaker1.setCompanies(List.of(company1));

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);
            speaker2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name2")));

            NameCompany nameCompany0 = new NameCompany("Name0", company0);
            Map<NameCompany, Speaker> resourceNameCompanySpeakers0 = Map.of(nameCompany0, speaker0);

            return Stream.of(
                    arguments(speaker0, resourceNameCompanySpeakers0, speaker0),
                    arguments(speaker1, resourceNameCompanySpeakers0, null),
                    arguments(speaker2, resourceNameCompanySpeakers0, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void findResourceSpeakerByNameCompany(Speaker speaker, Map<NameCompany, Speaker> resourceNameCompanySpeakers,
                                              Speaker expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.findResourceSpeakerByNameCompany(speaker, resourceNameCompanySpeakers));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourceSpeakerByName method tests")
    class FindResourceSpeakerByNameTest {
        private Stream<Arguments> data() {
            final String SPEAKER_NAME0 = "Name0";
            final String SPEAKER_NAME1 = "Name1";
            final String SPEAKER_NAME2 = "Name2";
            final String SPEAKER_NAME3 = "Name3";

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME0)));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME1)));

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);
            speaker2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME2)));

            Speaker speaker3 = new Speaker();
            speaker3.setId(3);
            speaker3.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), SPEAKER_NAME3)));

            Map<String, Set<Speaker>> resourceNameSpeakers0 = new HashMap<>();
            resourceNameSpeakers0.put(SPEAKER_NAME0, Set.of(speaker0));
            resourceNameSpeakers0.put(SPEAKER_NAME2, Collections.emptySet());
            resourceNameSpeakers0.put(SPEAKER_NAME3, Set.of(speaker0, speaker3));

            return Stream.of(
                    arguments(speaker1, resourceNameSpeakers0, null, null),
                    arguments(speaker2, resourceNameSpeakers0, null, IllegalStateException.class),
                    arguments(speaker3, resourceNameSpeakers0, null, null),
                    arguments(speaker0, resourceNameSpeakers0, speaker0, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void findResourceSpeakerByName(Speaker speaker, Map<String, Set<Speaker>> resourceNameSpeakers,
                                       Speaker expected, Class<? extends Throwable> expectedException) {
            try (MockedStatic<LocalizationUtils> mockedStatic = Mockito.mockStatic(LocalizationUtils.class)) {
                mockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<String>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    List<LocaleItem> localeItems = (List<LocaleItem>) args[0];

                                    return ((localeItems != null) && !localeItems.isEmpty()) ? localeItems.get(0).getText() : null;
                                }
                        );

                if (expectedException == null) {
                    assertEquals(expected, ConferenceDataLoaderExecutor.findResourceSpeakerByName(speaker, resourceNameSpeakers));
                } else {
                    assertThrows(expectedException, () -> ConferenceDataLoaderExecutor.findResourceSpeakerByName(speaker, resourceNameSpeakers));
                }
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourceTalkByName method tests")
    class FindResourceTalkByNameTest {
        final String TALK_NAME0 = "Name0";
        final String TALK_NAME1 = "Name1";
        final String TALK_NAME2 = "Name2";
        final String TALK_NAME3 = "Name3";

        private Stream<Arguments> data() {
            Talk talk0 = new Talk();
            talk0.setId(0);
            talk0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), TALK_NAME0)));

            Talk talk1 = new Talk();
            talk1.setId(1);
            talk1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), TALK_NAME1)));

            Talk talk2 = new Talk();
            talk2.setId(2);
            talk2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), TALK_NAME2)));

            Talk talk3 = new Talk();
            talk3.setId(3);
            talk3.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), TALK_NAME3)));

            Map<String, Set<Talk>> resourceNameTalks0 = new HashMap<>();
            resourceNameTalks0.put(TALK_NAME0, Set.of(talk0));
            resourceNameTalks0.put(TALK_NAME2, Collections.emptySet());
            resourceNameTalks0.put(TALK_NAME3, Set.of(talk0, talk3));

            return Stream.of(
                    arguments(talk1, resourceNameTalks0, Language.ENGLISH, null, null),
                    arguments(talk2, resourceNameTalks0, Language.ENGLISH, null, IllegalStateException.class),
                    arguments(talk3, resourceNameTalks0, Language.ENGLISH, null, null),
                    arguments(talk0, resourceNameTalks0, Language.ENGLISH, talk0, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void findResourceTalkByName(Talk talk, Map<String, Set<Talk>> resourceNameTalks, Language language,
                                    Talk expected, Class<? extends Throwable> expectedException) {
            try (MockedStatic<LocalizationUtils> mockedStatic = Mockito.mockStatic(LocalizationUtils.class)) {
                mockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<String>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    List<LocaleItem> localeItems = (List<LocaleItem>) args[0];

                                    return ((localeItems != null) && !localeItems.isEmpty()) ? localeItems.get(0).getText() : null;
                                }
                        );

                if (expectedException == null) {
                    assertEquals(expected, ConferenceDataLoaderExecutor.findResourceTalkByName(talk, resourceNameTalks, language));
                } else {
                    assertThrows(expectedException, () -> ConferenceDataLoaderExecutor.findResourceTalkByName(talk, resourceNameTalks, language));
                }
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourcePlaceByCityVenueAddress method tests")
    class FindResourcePlaceByCityVenueAddressTest {
        final String CITY0 = "City0";
        final String CITY1 = "City1";
        final String VENUE_ADDRESS0 = "Venue Address0";
        final String VENUE_ADDRESS1 = "Venue Address1";

        private Stream<Arguments> data() {
            Place place0 = new Place();
            place0.setId(0);
            place0.setCity(List.of(new LocaleItem(Language.ENGLISH.getCode(), CITY0)));
            place0.setVenueAddress(List.of(new LocaleItem(Language.ENGLISH.getCode(), VENUE_ADDRESS0)));

            Place place1 = new Place();
            place1.setId(1);
            place1.setCity(List.of(new LocaleItem(Language.ENGLISH.getCode(), CITY1)));
            place1.setVenueAddress(List.of(new LocaleItem(Language.ENGLISH.getCode(), VENUE_ADDRESS1)));

            Map<CityVenueAddress, Place> resourceCityVenueAddressPlaces0 = Map.of(
                    new CityVenueAddress(CITY0, VENUE_ADDRESS0), place0);

            return Stream.of(
                    arguments(place0, resourceCityVenueAddressPlaces0, Language.ENGLISH, place0),
                    arguments(place1, resourceCityVenueAddressPlaces0, Language.ENGLISH, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void findResourcePlaceByCityVenueAddress(Place place, Map<CityVenueAddress, Place> resourceCityVenueAddressPlaces,
                                                 Language language, Place expected) {
            try (MockedStatic<LocalizationUtils> mockedStatic = Mockito.mockStatic(LocalizationUtils.class)) {
                mockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<String>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    List<LocaleItem> localeItems = (List<LocaleItem>) args[0];

                                    return ((localeItems != null) && !localeItems.isEmpty()) ? localeItems.get(0).getText() : null;
                                }
                        );

                assertEquals(expected, ConferenceDataLoaderExecutor.findResourcePlaceByCityVenueAddress(place, resourceCityVenueAddressPlaces, language));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("findResourcePlace method tests")
    class FindResourcePlaceTest {
        private final Place place0;
        private final Place place1;
        private final Place place2;

        public FindResourcePlaceTest() {
            place0 = new Place();
            place0.setId(0);

            place1 = new Place();
            place1.setId(-1);

            place2 = new Place();
            place2.setId(-2);
        }

        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(place0, Map.of(0L, place0), Collections.emptyMap(), Collections.emptyMap(), null, place0),
                    arguments(place0, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), NullPointerException.class, place0),
                    arguments(place1, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), null, place1),
                    arguments(place2, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), null, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void findResourcePlace(Place place, Map<Long, Place> resourceIdPlaces, Map<CityVenueAddress, Place> resourceRuCityVenueAddressPlaces,
                               Map<CityVenueAddress, Place> resourceEnCityVenueAddressPlaces, Class<? extends Throwable> expectedException, Place expectedValue) {
            try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourcePlace(Mockito.any(Place.class), Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap()))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ConferenceDataLoaderExecutor.findResourcePlaceByCityVenueAddress(Mockito.any(Place.class), Mockito.anyMap(), Mockito.any(Language.class)))
                        .thenAnswer(
                                (Answer<Place>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Place localPlace = (Place) args[0];

                                    if (localPlace != null) {
                                        return (localPlace.getId() == -1) ? localPlace : null;
                                    } else {
                                        return null;
                                    }
                                }
                        );

                if (expectedException == null) {
                    assertEquals(expectedValue, ConferenceDataLoaderExecutor.findResourcePlace(place, resourceIdPlaces, resourceRuCityVenueAddressPlaces, resourceEnCityVenueAddressPlaces));
                } else {
                    assertThrows(expectedException, () -> ConferenceDataLoaderExecutor.findResourcePlace(place, resourceIdPlaces, resourceRuCityVenueAddressPlaces, resourceEnCityVenueAddressPlaces));
                }
            }
        }
    }

    @Test
    void fixVenueAddress() {
        try (MockedStatic<ConferenceDataLoaderExecutor> mockedStatic = Mockito.mockStatic(ConferenceDataLoaderExecutor.class)) {
            mockedStatic.when(() -> ConferenceDataLoaderExecutor.fixVenueAddress(Mockito.any(Place.class)))
                    .thenCallRealMethod();
            mockedStatic.when(() -> ConferenceDataLoaderExecutor.getFixedVenueAddress(Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
                    .thenReturn("");

            assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.fixVenueAddress(new Place()));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getFixedVenueAddress method tests")
    class GetFixedVenueAddressTest {
        private Stream<Arguments> data() {
            final String CITY = "City";
            final String VENUE_ADDRESS = "Venue Address";
            final String VALID_VENUE_ADDRESS = "Valid Venue Address";

            List<FixingVenueAddress> fixingVenueAddresses0 = List.of(
                    new FixingVenueAddress("", "", VALID_VENUE_ADDRESS));

            List<FixingVenueAddress> fixingVenueAddresses1 = List.of(
                    new FixingVenueAddress(CITY, "", VALID_VENUE_ADDRESS));

            List<FixingVenueAddress> fixingVenueAddresses2 = List.of(
                    new FixingVenueAddress("", VENUE_ADDRESS, VALID_VENUE_ADDRESS));

            List<FixingVenueAddress> fixingVenueAddresses3 = List.of(
                    new FixingVenueAddress(CITY, VENUE_ADDRESS, VALID_VENUE_ADDRESS));

            return Stream.of(
                    arguments(CITY, VENUE_ADDRESS, Collections.emptyList(), VENUE_ADDRESS),
                    arguments(CITY, VENUE_ADDRESS, fixingVenueAddresses0, VENUE_ADDRESS),
                    arguments(CITY, VENUE_ADDRESS, fixingVenueAddresses1, VENUE_ADDRESS),
                    arguments(CITY, VENUE_ADDRESS, fixingVenueAddresses2, VENUE_ADDRESS),
                    arguments(CITY, VENUE_ADDRESS, fixingVenueAddresses3, VALID_VENUE_ADDRESS)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getFixedVenueAddress(String city, String venueAddress, List<FixingVenueAddress> fixingVenueAddresses,
                                  String expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.getFixedVenueAddress(city, venueAddress, fixingVenueAddresses));
        }
    }

    @Test
    void checkVideoLinks() {
        try (MockedStatic<YamlUtils> mockedStatic = Mockito.mockStatic(YamlUtils.class)) {
            LocalDate now = LocalDate.now();
            LocalDate yesterday = now.minusDays(1);
            LocalDate tomorrow = now.plusDays(1);

            EventType eventType0 = new EventType();

            EventType eventType1 = new EventType();
            eventType1.setConference(Conference.JOKER);

            Event event0 = new Event();
            event0.setEventType(eventType0);
            event0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            event0.setDays(List.of());

            Event event1 = new Event();
            event1.setEventType(eventType1);
            event1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
            event1.setDays(List.of(new EventDays(
                    now,
                    null,
                    new Place()
            )));

            Event event2 = new Event();
            event2.setEventType(eventType1);
            event2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name2")));
            event2.setDays(List.of(new EventDays(
                    tomorrow,
                    null,
                    new Place()
            )));

            Event event3 = new Event();
            event3.setEventType(eventType1);
            event3.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name3")));
            event3.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event4 = new Event();
            event4.setEventType(eventType1);
            event4.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name4")));
            event4.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event5 = new Event();
            event5.setEventType(eventType1);
            event5.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name5")));
            event5.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event6 = new Event();
            event6.setEventType(eventType1);
            event6.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name6")));
            event6.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Talk talk0 = new Talk();

            Talk talk1 = new Talk();
            talk1.setVideoLinks(Collections.emptyList());

            Talk talk2 = new Talk();
            talk2.setVideoLinks(List.of("Link0"));

            Talk talk3 = new Talk();
            talk3.setVideoLinks(List.of("Link0"));

            Talk talk4 = new Talk();
            talk4.setVideoLinks(List.of("Link0"));

            event2.setTalks(List.of(talk0));
            event3.setTalks(List.of(talk1));
            event4.setTalks(List.of(talk2));
            event5.setTalks(List.of(talk1, talk2));
            event6.setTalks(List.of(talk1, talk2, talk3, talk4));

            mockedStatic.when(YamlUtils::readSourceInformation)
                    .thenReturn(new SourceInformation(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(event0, event1, event2, event3, event4, event5, event6),
                            new SourceInformation.SpeakerInformation(
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList()
                            ),
                            Collections.emptyList()
                    ));

            assertDoesNotThrow(ConferenceDataLoaderExecutor::checkVideoLinks);
        }
    }

    @Test
    void checkCompanies() {
        try (MockedStatic<YamlUtils> mockedStatic = Mockito.mockStatic(YamlUtils.class)) {
            Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")), "");
            Company company2 = new Company(2, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name2")), " ");
            Company company3 = new Company(3, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name3")), "https://site1.com");
            Company company4 = new Company(4, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name4")), "https://site2.com");
            Company company5 = new Company(5, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name5")), "https://site2.com");

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            speaker0.setCompanies(List.of(company0));

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
            speaker1.setCompanies(Collections.emptyList());

            mockedStatic.when(YamlUtils::readSourceInformation)
                    .thenReturn(new SourceInformation(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            new SourceInformation.SpeakerInformation(
                                    List.of(company0, company1, company2, company3, company4, company5),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    List.of(speaker0, speaker1)
                            ),
                            Collections.emptyList()
                    ));

            assertDoesNotThrow(ConferenceDataLoaderExecutor::checkCompanies);
        }
    }

    @Test
    void checkTalkAttributes() {
        try (MockedStatic<YamlUtils> mockedStatic = Mockito.mockStatic(YamlUtils.class)) {
            LocalDate now = LocalDate.now();
            LocalDate yesterday = now.minusDays(1);
            LocalDate tomorrow = now.plusDays(1);

            EventType eventType0 = new EventType();

            EventType eventType1 = new EventType();
            eventType1.setConference(Conference.JOKER);

            Event event0 = new Event();
            event0.setEventType(eventType0);
            event0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
            event0.setDays(List.of(new EventDays(
                    now,
                    null,
                    new Place()
            )));

            Event event1 = new Event();
            event1.setEventType(eventType1);
            event1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
            event1.setDays(List.of(new EventDays(
                    now,
                    null,
                    new Place()
            )));

            Event event2 = new Event();
            event2.setEventType(eventType1);
            event2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name2")));
            event2.setDays(List.of(new EventDays(
                    tomorrow,
                    null,
                    new Place()
            )));

            Event event3 = new Event();
            event3.setEventType(eventType1);
            event3.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name3")));
            event3.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event4 = new Event();
            event4.setEventType(eventType1);
            event4.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name4")));
            event4.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event5 = new Event();
            event5.setEventType(eventType1);
            event5.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name5")));
            event5.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event6 = new Event();
            event6.setEventType(eventType1);
            event6.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name6")));
            event6.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Event event7 = new Event();
            event7.setEventType(eventType1);
            event7.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name7")));
            event7.setDays(List.of(new EventDays(
                    yesterday,
                    null,
                    new Place()
            )));

            Talk talk0 = new Talk();

            Talk talk1 = new Talk();
            talk1.setTalkDay(1L);

            Talk talk2 = new Talk();
            talk2.setStartTime(LocalTime.of(10, 0));

            Talk talk3 = new Talk();
            talk3.setTalkDay(1L);
            talk3.setStartTime(LocalTime.of(10, 0));

            Talk talk4 = new Talk();
            talk4.setEndTime(LocalTime.of(11, 0));

            Talk talk5 = new Talk();
            talk5.setTalkDay(1L);
            talk5.setEndTime(LocalTime.of(11, 0));

            Talk talk6 = new Talk();
            talk6.setStartTime(LocalTime.of(10, 0));
            talk6.setEndTime(LocalTime.of(11, 0));

            Talk talk7 = new Talk();
            talk7.setTalkDay(1L);
            talk7.setStartTime(LocalTime.of(10, 0));
            talk7.setEndTime(LocalTime.of(11, 0));

            Talk talk8 = new Talk();
            talk8.setTrack(1L);

            Talk talk9 = new Talk();
            talk9.setTalkDay(1L);
            talk9.setTrack(1L);

            Talk talk10 = new Talk();
            talk10.setStartTime(LocalTime.of(10, 0));
            talk10.setTrack(1L);

            Talk talk11 = new Talk();
            talk11.setTalkDay(1L);
            talk11.setStartTime(LocalTime.of(10, 0));
            talk11.setTrack(1L);

            Talk talk12 = new Talk();
            talk12.setEndTime(LocalTime.of(11, 0));
            talk12.setTrack(1L);

            Talk talk13 = new Talk();
            talk13.setTalkDay(1L);
            talk13.setEndTime(LocalTime.of(11, 0));
            talk13.setTrack(1L);

            Talk talk14 = new Talk();
            talk14.setStartTime(LocalTime.of(10, 0));
            talk14.setEndTime(LocalTime.of(11, 0));
            talk14.setTrack(1L);

            Talk talk15 = new Talk();
            talk15.setTalkDay(1L);
            talk15.setStartTime(LocalTime.of(10, 0));
            talk15.setEndTime(LocalTime.of(11, 0));
            talk15.setTrack(1L);

            Talk talk16 = new Talk();
            talk16.setTalkDay(1L);
            talk16.setStartTime(LocalTime.of(12, 0));
            talk16.setEndTime(LocalTime.of(12, 45));
            talk16.setTrack(1L);

            Talk talk17 = new Talk();
            talk17.setTalkDay(1L);
            talk17.setStartTime(LocalTime.of(13, 0));
            talk17.setEndTime(LocalTime.of(13, 15));
            talk17.setTrack(1L);

            event2.setTalks(List.of(talk0, talk1, talk2, talk3, talk4));
            event3.setTalks(List.of(talk5, talk6, talk7, talk8, talk9));
            event4.setTalks(List.of(talk10, talk11, talk12, talk13, talk14));
            event5.setTalks(List.of(talk15));
            event6.setTalks(List.of(talk1, talk15));
            event7.setTalks(List.of(talk2, talk15, talk16, talk17));

            mockedStatic.when(YamlUtils::readSourceInformation)
                    .thenReturn(new SourceInformation(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            List.of(event0, event1, event2, event3, event4, event5, event6, event7),
                            new SourceInformation.SpeakerInformation(
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList()
                            ),
                            Collections.emptyList()
                    ));

            assertDoesNotThrow(ConferenceDataLoaderExecutor::checkTalkAttributes);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needUpdate method tests (EventType)")
    class NeedUpdateEventTypeTest {
        private Stream<Arguments> data() {
            Organizer organizer0 = new Organizer(0, Collections.emptyList());
            Organizer organizer1 = new Organizer(1, Collections.emptyList());

            Topic topic0 = new Topic(0, Collections.emptyList(), false, 0);
            Topic topic1 = new Topic(1, Collections.emptyList(), false, 1);

            EventType eventType0 = new EventType();
            eventType0.setId(0);
            eventType0.setConference(Conference.JPOINT);
            eventType0.setLogoFileName("logoFileName0");
            eventType0.setName(List.of(new LocaleItem("en", "name0")));
            eventType0.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType0.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType0.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType0.setVkLink("vkLink0");
            eventType0.setTwitterLink("twitterLink0");
            eventType0.setFacebookLink("facebookLink0");
            eventType0.setYoutubeLink("youtubeLink0");
            eventType0.setTelegramLink("telegramLink0");
            eventType0.setSpeakerdeckLink("speakerdeckLink0");
            eventType0.setHabrLink("habrLink0");
            eventType0.setOrganizer(organizer0);
            eventType0.setTimeZone("Europe/Moscow");
            eventType0.setTopic(topic0);

            EventType eventType1 = new EventType();
            eventType1.setId(1);

            EventType eventType2 = new EventType();
            eventType2.setId(0);
            eventType2.setConference(Conference.JOKER);

            EventType eventType3 = new EventType();
            eventType3.setId(0);
            eventType3.setConference(Conference.JPOINT);
            eventType3.setLogoFileName("logoFileName3");

            EventType eventType4 = new EventType();
            eventType4.setId(0);
            eventType4.setConference(Conference.JPOINT);
            eventType4.setLogoFileName("logoFileName0");
            eventType4.setName(List.of(new LocaleItem("en", "name4")));

            EventType eventType5 = new EventType();
            eventType5.setId(0);
            eventType5.setConference(Conference.JPOINT);
            eventType5.setLogoFileName("logoFileName0");
            eventType5.setName(List.of(new LocaleItem("en", "name0")));
            eventType5.setShortDescription(List.of(new LocaleItem("en", "shortDescription5")));

            EventType eventType6 = new EventType();
            eventType6.setId(0);
            eventType6.setConference(Conference.JPOINT);
            eventType6.setLogoFileName("logoFileName0");
            eventType6.setName(List.of(new LocaleItem("en", "name0")));
            eventType6.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType6.setLongDescription(List.of(new LocaleItem("en", "longDescription6")));

            EventType eventType7 = new EventType();
            eventType7.setId(0);
            eventType7.setConference(Conference.JPOINT);
            eventType7.setLogoFileName("logoFileName0");
            eventType7.setName(List.of(new LocaleItem("en", "name0")));
            eventType7.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType7.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType7.setSiteLink(List.of(new LocaleItem("en", "siteLink7")));

            EventType eventType8 = new EventType();
            eventType8.setId(0);
            eventType8.setConference(Conference.JPOINT);
            eventType8.setLogoFileName("logoFileName0");
            eventType8.setName(List.of(new LocaleItem("en", "name0")));
            eventType8.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType8.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType8.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType8.setVkLink("vkLink8");

            EventType eventType9 = new EventType();
            eventType9.setId(0);
            eventType9.setConference(Conference.JPOINT);
            eventType9.setLogoFileName("logoFileName0");
            eventType9.setName(List.of(new LocaleItem("en", "name0")));
            eventType9.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType9.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType9.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType9.setVkLink("vkLink0");
            eventType9.setTwitterLink("twitterLink9");

            EventType eventType10 = new EventType();
            eventType10.setId(0);
            eventType10.setConference(Conference.JPOINT);
            eventType10.setLogoFileName("logoFileName0");
            eventType10.setName(List.of(new LocaleItem("en", "name0")));
            eventType10.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType10.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType10.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType10.setVkLink("vkLink0");
            eventType10.setTwitterLink("twitterLink0");
            eventType10.setFacebookLink("facebookLink10");

            EventType eventType11 = new EventType();
            eventType11.setId(0);
            eventType11.setConference(Conference.JPOINT);
            eventType11.setLogoFileName("logoFileName0");
            eventType11.setName(List.of(new LocaleItem("en", "name0")));
            eventType11.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType11.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType11.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType11.setVkLink("vkLink0");
            eventType11.setTwitterLink("twitterLink0");
            eventType11.setFacebookLink("facebookLink0");
            eventType11.setYoutubeLink("youtubeLink11");

            EventType eventType12 = new EventType();
            eventType12.setId(0);
            eventType12.setConference(Conference.JPOINT);
            eventType12.setLogoFileName("logoFileName0");
            eventType12.setName(List.of(new LocaleItem("en", "name0")));
            eventType12.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType12.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType12.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType12.setVkLink("vkLink0");
            eventType12.setTwitterLink("twitterLink0");
            eventType12.setFacebookLink("facebookLink0");
            eventType12.setYoutubeLink("youtubeLink0");
            eventType12.setTelegramLink("telegramLink12");

            EventType eventType13 = new EventType();
            eventType13.setId(0);
            eventType13.setConference(Conference.JPOINT);
            eventType13.setLogoFileName("logoFileName0");
            eventType13.setName(List.of(new LocaleItem("en", "name0")));
            eventType13.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType13.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType13.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType13.setVkLink("vkLink0");
            eventType13.setTwitterLink("twitterLink0");
            eventType13.setFacebookLink("facebookLink0");
            eventType13.setYoutubeLink("youtubeLink0");
            eventType13.setTelegramLink("telegramLink0");
            eventType13.setSpeakerdeckLink("speakerdeckLink13");

            EventType eventType14 = new EventType();
            eventType14.setId(0);
            eventType14.setConference(Conference.JPOINT);
            eventType14.setLogoFileName("logoFileName0");
            eventType14.setName(List.of(new LocaleItem("en", "name0")));
            eventType14.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType14.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType14.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType14.setVkLink("vkLink0");
            eventType14.setTwitterLink("twitterLink0");
            eventType14.setFacebookLink("facebookLink0");
            eventType14.setYoutubeLink("youtubeLink0");
            eventType14.setTelegramLink("telegramLink0");
            eventType14.setSpeakerdeckLink("speakerdeckLink0");
            eventType14.setHabrLink("habrLink14");

            EventType eventType15 = new EventType();
            eventType15.setId(0);
            eventType15.setConference(Conference.JPOINT);
            eventType15.setLogoFileName("logoFileName0");
            eventType15.setName(List.of(new LocaleItem("en", "name0")));
            eventType15.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType15.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType15.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType15.setVkLink("vkLink0");
            eventType15.setTwitterLink("twitterLink0");
            eventType15.setFacebookLink("facebookLink0");
            eventType15.setYoutubeLink("youtubeLink0");
            eventType15.setTelegramLink("telegramLink0");
            eventType15.setSpeakerdeckLink("speakerdeckLink0");
            eventType15.setHabrLink("habrLink0");
            eventType15.setOrganizer(organizer1);

            EventType eventType16 = new EventType();
            eventType16.setId(0);
            eventType16.setConference(Conference.JPOINT);
            eventType16.setLogoFileName("logoFileName0");
            eventType16.setName(List.of(new LocaleItem("en", "name0")));
            eventType16.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType16.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType16.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType16.setVkLink("vkLink0");
            eventType16.setTwitterLink("twitterLink0");
            eventType16.setFacebookLink("facebookLink0");
            eventType16.setYoutubeLink("youtubeLink0");
            eventType16.setTelegramLink("telegramLink0");
            eventType16.setSpeakerdeckLink("speakerdeckLink0");
            eventType16.setHabrLink("habrLink0");
            eventType16.setOrganizer(organizer0);

            EventType eventType17 = new EventType();
            eventType17.setId(0);
            eventType17.setConference(Conference.JPOINT);
            eventType17.setLogoFileName("logoFileName0");
            eventType17.setName(List.of(new LocaleItem("en", "name0")));
            eventType17.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType17.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType17.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType17.setVkLink("vkLink0");
            eventType17.setTwitterLink("twitterLink0");
            eventType17.setFacebookLink("facebookLink0");
            eventType17.setYoutubeLink("youtubeLink0");
            eventType17.setTelegramLink("telegramLink0");
            eventType17.setSpeakerdeckLink("speakerdeckLink0");
            eventType17.setHabrLink("habrLink0");
            eventType17.setOrganizer(organizer0);
            eventType17.setTimeZone("Europe/Moscow");
            eventType17.setTopic(topic1);

            EventType eventType18 = new EventType();
            eventType18.setId(0);
            eventType18.setConference(Conference.JPOINT);
            eventType18.setLogoFileName("logoFileName0");
            eventType18.setName(List.of(new LocaleItem("en", "name0")));
            eventType18.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            eventType18.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            eventType18.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            eventType18.setVkLink("vkLink0");
            eventType18.setTwitterLink("twitterLink0");
            eventType18.setFacebookLink("facebookLink0");
            eventType18.setYoutubeLink("youtubeLink0");
            eventType18.setTelegramLink("telegramLink0");
            eventType18.setSpeakerdeckLink("speakerdeckLink0");
            eventType18.setHabrLink("habrLink0");
            eventType18.setOrganizer(organizer0);
            eventType18.setTimeZone("Europe/Moscow");
            eventType18.setTopic(topic0);
            eventType18.setInactive(true);

            return Stream.of(
                    arguments(eventType0, eventType0, false),
                    arguments(eventType0, eventType1, true),
                    arguments(eventType0, eventType2, true),
                    arguments(eventType0, eventType3, true),
                    arguments(eventType0, eventType4, true),
                    arguments(eventType0, eventType5, true),
                    arguments(eventType0, eventType6, true),
                    arguments(eventType0, eventType7, true),
                    arguments(eventType0, eventType8, true),
                    arguments(eventType0, eventType9, true),
                    arguments(eventType0, eventType10, true),
                    arguments(eventType0, eventType11, true),
                    arguments(eventType0, eventType12, true),
                    arguments(eventType0, eventType13, true),
                    arguments(eventType0, eventType14, true),
                    arguments(eventType0, eventType15, true),
                    arguments(eventType0, eventType16, true),
                    arguments(eventType0, eventType17, true),
                    arguments(eventType0, eventType18, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needUpdate(EventType a, EventType b, boolean expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.needUpdate(a, b));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needUpdate method tests (Place)")
    class NeedUpdatePlaceTest {
        private Stream<Arguments> data() {
            Place place0 = new Place();
            place0.setId(0);
            place0.setCity(List.of(new LocaleItem("en", "city0")));
            place0.setVenueAddress(List.of(new LocaleItem("en", "venueAddress0")));
            place0.setMapCoordinates("mapCoordinates0");

            Place place1 = new Place();
            place1.setId(1);

            Place place2 = new Place();
            place2.setId(0);
            place2.setCity(List.of(new LocaleItem("en", "city2")));

            Place place3 = new Place();
            place3.setId(0);
            place3.setCity(List.of(new LocaleItem("en", "city0")));
            place3.setVenueAddress(List.of(new LocaleItem("en", "venueAddress3")));

            Place place4 = new Place();
            place4.setId(0);
            place4.setCity(List.of(new LocaleItem("en", "city0")));
            place4.setVenueAddress(List.of(new LocaleItem("en", "venueAddress0")));
            place4.setMapCoordinates("mapCoordinates4");

            return Stream.of(
                    arguments(place0, place0, false),
                    arguments(place0, place1, true),
                    arguments(place0, place2, true),
                    arguments(place0, place3, true),
                    arguments(place0, place4, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needUpdate(Place a, Place b, boolean expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.needUpdate(a, b));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needUpdate method tests (Speaker)")
    class NeedUpdateSpeakerTest {
        private Stream<Arguments> data() {
            ZonedDateTime now = ZonedDateTime.now();

            Company company0 = new Company(0, List.of(new LocaleItem("en", "company0")));
            Company company5 = new Company(4, List.of(new LocaleItem("en", "company4")));

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setPhotoFileName("photoFileName0");
            speaker0.setPhotoUpdatedAt(now);
            speaker0.setName(List.of(new LocaleItem("en", "name0")));
            speaker0.setCompanies(List.of(company0));
            speaker0.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker0.setTwitter("twitter0");
            speaker0.setGitHub("gitHub0");
            speaker0.setHabr("habr0");
            speaker0.setJavaChampion(true);
            speaker0.setMvp(true);
            speaker0.setMvpReconnect(true);

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);

            Speaker speaker2 = new Speaker();
            speaker2.setId(0);
            speaker2.setPhotoFileName("photoFileName2");

            Speaker speaker3 = new Speaker();
            speaker3.setId(0);
            speaker3.setPhotoFileName("photoFileName0");
            speaker3.setPhotoUpdatedAt(now.plus(1, ChronoUnit.DAYS));

            Speaker speaker4 = new Speaker();
            speaker4.setId(0);
            speaker4.setPhotoFileName("photoFileName0");
            speaker4.setPhotoUpdatedAt(now);
            speaker4.setName(List.of(new LocaleItem("en", "name3")));

            Speaker speaker5 = new Speaker();
            speaker5.setId(0);
            speaker5.setPhotoFileName("photoFileName0");
            speaker5.setPhotoUpdatedAt(now);
            speaker5.setName(List.of(new LocaleItem("en", "name0")));
            speaker5.setCompanies(List.of(company5));

            Speaker speaker6 = new Speaker();
            speaker6.setId(0);
            speaker6.setPhotoFileName("photoFileName0");
            speaker6.setPhotoUpdatedAt(now);
            speaker6.setName(List.of(new LocaleItem("en", "name0")));
            speaker6.setCompanies(List.of(company0));
            speaker6.setBio(List.of(new LocaleItem("en", "bio6")));

            Speaker speaker7 = new Speaker();
            speaker7.setId(0);
            speaker7.setPhotoFileName("photoFileName0");
            speaker7.setPhotoUpdatedAt(now);
            speaker7.setName(List.of(new LocaleItem("en", "name0")));
            speaker7.setCompanies(List.of(company0));
            speaker7.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker7.setTwitter("twitter7");

            Speaker speaker8 = new Speaker();
            speaker8.setId(0);
            speaker8.setPhotoFileName("photoFileName0");
            speaker8.setPhotoUpdatedAt(now);
            speaker8.setName(List.of(new LocaleItem("en", "name0")));
            speaker8.setCompanies(List.of(company0));
            speaker8.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker8.setTwitter("twitter0");
            speaker8.setGitHub("gitHub8");

            Speaker speaker9 = new Speaker();
            speaker9.setId(0);
            speaker9.setPhotoFileName("photoFileName0");
            speaker9.setPhotoUpdatedAt(now);
            speaker9.setName(List.of(new LocaleItem("en", "name0")));
            speaker9.setCompanies(List.of(company0));
            speaker9.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker9.setTwitter("twitter0");
            speaker9.setGitHub("gitHub0");
            speaker9.setHabr("habr9");

            Speaker speaker10 = new Speaker();
            speaker10.setId(0);
            speaker10.setPhotoFileName("photoFileName0");
            speaker10.setPhotoUpdatedAt(now);
            speaker10.setName(List.of(new LocaleItem("en", "name0")));
            speaker10.setCompanies(List.of(company0));
            speaker10.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker10.setTwitter("twitter0");
            speaker10.setGitHub("gitHub0");
            speaker10.setHabr("habr0");
            speaker10.setJavaChampion(false);

            Speaker speaker11 = new Speaker();
            speaker11.setId(0);
            speaker11.setPhotoFileName("photoFileName0");
            speaker11.setPhotoUpdatedAt(now);
            speaker11.setName(List.of(new LocaleItem("en", "name0")));
            speaker11.setCompanies(List.of(company0));
            speaker11.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker11.setTwitter("twitter0");
            speaker11.setGitHub("gitHub0");
            speaker11.setHabr("habr0");
            speaker11.setJavaChampion(true);
            speaker11.setMvp(false);

            Speaker speaker12 = new Speaker();
            speaker12.setId(0);
            speaker12.setPhotoFileName("photoFileName0");
            speaker12.setPhotoUpdatedAt(now);
            speaker12.setName(List.of(new LocaleItem("en", "name0")));
            speaker12.setCompanies(List.of(company0));
            speaker12.setBio(List.of(new LocaleItem("en", "bio0")));
            speaker12.setTwitter("twitter0");
            speaker12.setGitHub("gitHub0");
            speaker12.setHabr("habr0");
            speaker12.setJavaChampion(true);
            speaker12.setMvp(true);
            speaker12.setMvpReconnect(false);

            return Stream.of(
                    arguments(speaker0, speaker0, false),
                    arguments(speaker0, speaker1, true),
                    arguments(speaker0, speaker2, true),
                    arguments(speaker0, speaker3, true),
                    arguments(speaker0, speaker4, true),
                    arguments(speaker0, speaker5, true),
                    arguments(speaker0, speaker6, true),
                    arguments(speaker0, speaker7, true),
                    arguments(speaker0, speaker8, true),
                    arguments(speaker0, speaker9, true),
                    arguments(speaker0, speaker10, true),
                    arguments(speaker0, speaker11, true),
                    arguments(speaker0, speaker12, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needUpdate(Speaker a, Speaker b, boolean expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.needUpdate(a, b));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needUpdate method tests (Talk)")
    class NeedUpdateTalkTest {
        private Stream<Arguments> data() {
            Topic topic0 = new Topic(0, Collections.emptyList(), false, 0);
            Topic topic1 = new Topic(1, Collections.emptyList(), false, 1);

            Talk talk0 = new Talk();
            talk0.setId(0);
            talk0.setName(List.of(new LocaleItem("en", "name0")));
            talk0.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk0.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk0.setTalkDay(1L);
            talk0.setStartTime(LocalTime.of(10, 0));
            talk0.setEndTime(LocalTime.of(11, 0));
            talk0.setTrack(1L);
            talk0.setLanguage("en");
            talk0.setPresentationLinks(List.of("presentationLink0"));
            talk0.setMaterialLinks(List.of("materialLink0"));
            talk0.setVideoLinks(List.of("videoLink0"));
            talk0.setSpeakerIds(List.of(0L));
            talk0.setTopic(topic0);

            Talk talk1 = new Talk();
            talk1.setId(1);

            Talk talk2 = new Talk();
            talk2.setId(0);
            talk2.setName(List.of(new LocaleItem("en", "name2")));

            Talk talk3 = new Talk();
            talk3.setId(0);
            talk3.setName(List.of(new LocaleItem("en", "name0")));
            talk3.setShortDescription(List.of(new LocaleItem("en", "shortDescription3")));

            Talk talk4 = new Talk();
            talk4.setId(0);
            talk4.setName(List.of(new LocaleItem("en", "name0")));
            talk4.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk4.setLongDescription(List.of(new LocaleItem("en", "longDescription4")));

            Talk talk5 = new Talk();
            talk5.setId(0);
            talk5.setName(List.of(new LocaleItem("en", "name0")));
            talk5.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk5.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk5.setTalkDay(2L);

            Talk talk6 = new Talk();
            talk6.setId(0);
            talk6.setName(List.of(new LocaleItem("en", "name0")));
            talk6.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk6.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk6.setTalkDay(1L);
            talk6.setStartTime(LocalTime.of(10, 30));

            Talk talk7 = new Talk();
            talk7.setId(0);
            talk7.setName(List.of(new LocaleItem("en", "name0")));
            talk7.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk7.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk7.setTalkDay(1L);
            talk7.setStartTime(LocalTime.of(10, 0));
            talk7.setEndTime(LocalTime.of(11, 30));

            Talk talk8 = new Talk();
            talk8.setId(0);
            talk8.setName(List.of(new LocaleItem("en", "name0")));
            talk8.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk8.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk8.setTalkDay(1L);
            talk8.setStartTime(LocalTime.of(10, 0));
            talk8.setEndTime(LocalTime.of(11, 0));
            talk8.setTrack(2L);

            Talk talk9 = new Talk();
            talk9.setId(0);
            talk9.setName(List.of(new LocaleItem("en", "name0")));
            talk9.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk9.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk9.setTalkDay(1L);
            talk9.setStartTime(LocalTime.of(10, 0));
            talk9.setEndTime(LocalTime.of(11, 0));
            talk9.setTrack(1L);
            talk9.setLanguage("ru");

            Talk talk10 = new Talk();
            talk10.setId(0);
            talk10.setName(List.of(new LocaleItem("en", "name0")));
            talk10.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk10.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk10.setTalkDay(1L);
            talk10.setStartTime(LocalTime.of(10, 0));
            talk10.setEndTime(LocalTime.of(11, 0));
            talk10.setTrack(1L);
            talk10.setLanguage("en");
            talk10.setPresentationLinks(List.of("presentationLink9"));

            Talk talk11 = new Talk();
            talk11.setId(0);
            talk11.setName(List.of(new LocaleItem("en", "name0")));
            talk11.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk11.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk11.setTalkDay(1L);
            talk11.setStartTime(LocalTime.of(10, 0));
            talk11.setEndTime(LocalTime.of(11, 0));
            talk11.setTrack(1L);
            talk11.setLanguage("en");
            talk11.setPresentationLinks(List.of("presentationLink0"));
            talk11.setMaterialLinks(List.of("materialLink10"));

            Talk talk12 = new Talk();
            talk12.setId(0);
            talk12.setName(List.of(new LocaleItem("en", "name0")));
            talk12.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk12.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk12.setTalkDay(1L);
            talk12.setStartTime(LocalTime.of(10, 0));
            talk12.setEndTime(LocalTime.of(11, 0));
            talk12.setTrack(1L);
            talk12.setLanguage("en");
            talk12.setPresentationLinks(List.of("presentationLink0"));
            talk12.setMaterialLinks(List.of("materialLink0"));
            talk12.setVideoLinks(List.of("videoLink11"));

            Talk talk13 = new Talk();
            talk13.setId(0);
            talk13.setName(List.of(new LocaleItem("en", "name0")));
            talk13.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk13.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk13.setTalkDay(1L);
            talk13.setStartTime(LocalTime.of(10, 0));
            talk13.setEndTime(LocalTime.of(11, 0));
            talk13.setTrack(1L);
            talk13.setLanguage("en");
            talk13.setPresentationLinks(List.of("presentationLink0"));
            talk13.setMaterialLinks(List.of("materialLink0"));
            talk13.setVideoLinks(List.of("videoLink0"));
            talk13.setSpeakerIds(List.of(1L));

            Talk talk14 = new Talk();
            talk14.setId(0);
            talk14.setName(List.of(new LocaleItem("en", "name0")));
            talk14.setShortDescription(List.of(new LocaleItem("en", "shortDescription0")));
            talk14.setLongDescription(List.of(new LocaleItem("en", "longDescription0")));
            talk14.setTalkDay(1L);
            talk14.setStartTime(LocalTime.of(10, 0));
            talk14.setEndTime(LocalTime.of(11, 0));
            talk14.setTrack(1L);
            talk14.setLanguage("en");
            talk14.setPresentationLinks(List.of("presentationLink0"));
            talk14.setMaterialLinks(List.of("materialLink0"));
            talk14.setVideoLinks(List.of("videoLink0"));
            talk14.setSpeakerIds(List.of(0L));
            talk14.setTopic(topic1);

            return Stream.of(
                    arguments(talk0, talk0, false),
                    arguments(talk0, talk1, true),
                    arguments(talk0, talk2, true),
                    arguments(talk0, talk3, true),
                    arguments(talk0, talk4, true),
                    arguments(talk0, talk5, true),
                    arguments(talk0, talk6, true),
                    arguments(talk0, talk7, true),
                    arguments(talk0, talk8, true),
                    arguments(talk0, talk9, true),
                    arguments(talk0, talk10, true),
                    arguments(talk0, talk11, true),
                    arguments(talk0, talk12, true),
                    arguments(talk0, talk13, true),
                    arguments(talk0, talk14, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needUpdate(Talk a, Talk b, boolean expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.needUpdate(a, b));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needUpdate method tests (Event)")
    class NeedUpdateEventTest {
        private Stream<Arguments> data() {
            final List<EventDays> VALID_EVENT_DAYS = List.of(new EventDays(
                    LocalDate.of(2020, 8, 5),
                    LocalDate.of(2020, 8, 6),
                    new Place(
                            0,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            null
                    )
            ));
            final List<EventDays> INVALID_EVENT_DAYS = List.of(new EventDays(
                    LocalDate.of(2020, 8, 6),
                    LocalDate.of(2020, 8, 6),
                    new Place(
                            0,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            null
                    )
            ));

            Event event0 = new Event();
            event0.setEventTypeId(0);
            event0.setName(List.of(new LocaleItem("en", "name0")));
            event0.setDays(VALID_EVENT_DAYS);
            event0.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            event0.setYoutubeLink("youtubeLink0");
            event0.setTalkIds(List.of(0L));
            event0.setTimeZone("Europe/Moscow");

            Event event1 = new Event();
            event1.setEventTypeId(1);

            Event event2 = new Event();
            event2.setEventTypeId(0);
            event2.setName(List.of(new LocaleItem("en", "name2")));

            Event event3 = new Event();
            event3.setEventTypeId(0);
            event3.setName(List.of(new LocaleItem("en", "name0")));
            event3.setDays(INVALID_EVENT_DAYS);

            Event event4 = new Event();
            event4.setEventTypeId(0);
            event4.setName(List.of(new LocaleItem("en", "name0")));
            event4.setDays(VALID_EVENT_DAYS);
            event4.setSiteLink(List.of(new LocaleItem("en", "siteLink5")));

            Event event5 = new Event();
            event5.setEventTypeId(0);
            event5.setName(List.of(new LocaleItem("en", "name0")));
            event5.setDays(VALID_EVENT_DAYS);
            event5.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            event5.setYoutubeLink("youtubeLink6");

            Event event6 = new Event();
            event6.setEventTypeId(0);
            event6.setName(List.of(new LocaleItem("en", "name0")));
            event6.setDays(VALID_EVENT_DAYS);
            event6.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            event6.setYoutubeLink("youtubeLink0");
            event6.setTalkIds(List.of(8L));

            Event event7 = new Event();
            event7.setEventTypeId(0);
            event7.setName(List.of(new LocaleItem("en", "name0")));
            event7.setDays(VALID_EVENT_DAYS);
            event7.setSiteLink(List.of(new LocaleItem("en", "siteLink0")));
            event7.setYoutubeLink("youtubeLink0");
            event7.setTalkIds(List.of(0L));

            return Stream.of(
                    arguments(event0, event0, false),
                    arguments(event0, event1, true),
                    arguments(event0, event2, true),
                    arguments(event0, event3, true),
                    arguments(event0, event4, true),
                    arguments(event0, event5, true),
                    arguments(event0, event6, true),
                    arguments(event0, event7, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needUpdate(Event a, Event b, boolean expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.needUpdate(a, b));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("needPhotoUpdate method tests")
    class NeedPhotoUpdateTest {
        private Stream<Arguments> data() {
            final ZonedDateTime NOW = ZonedDateTime.now();
            final ZonedDateTime YESTERDAY = NOW.minus(1, ChronoUnit.DAYS);
            final String VALID_URL = "https://valid.com";
            final String PHOTO_FILE_NAME = "0000.jpg";
            final String WIDTH_PARAMETER_NAME = "w";

            return Stream.of(
                    arguments(null, null, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, true, true),
                    arguments(null, NOW, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, true, true),
                    arguments(null, null, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, false, false),
                    arguments(null, NOW, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, false, false),
                    arguments(NOW, null, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, true, true),
                    arguments(NOW, null, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, false, true),
                    arguments(NOW, NOW, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, true, false),
                    arguments(NOW, NOW, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, false, false),
                    arguments(NOW, YESTERDAY, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, true, true),
                    arguments(NOW, YESTERDAY, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, false, true),
                    arguments(YESTERDAY, NOW, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, true, false),
                    arguments(YESTERDAY, NOW, VALID_URL, PHOTO_FILE_NAME, WIDTH_PARAMETER_NAME, false, false)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void needPhotoUpdate(ZonedDateTime targetPhotoUpdatedAt, ZonedDateTime resourcePhotoUpdatedAt,
                             String targetPhotoUrl, String resourcePhotoFileName, String imageWidthParameterName,
                             boolean needUpdate, boolean expected) throws IOException {
            try (MockedStatic<ImageUtils> mockedStatic = Mockito.mockStatic(ImageUtils.class)) {
                mockedStatic.when(() -> ImageUtils.needUpdate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                        .thenReturn(needUpdate);

                assertEquals(expected, ConferenceDataLoaderExecutor.needPhotoUpdate(targetPhotoUpdatedAt, resourcePhotoUpdatedAt,
                        targetPhotoUrl, resourcePhotoFileName, imageWidthParameterName));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("equals method tests")
    class EqualsTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, true),
                    arguments(null, List.of(""), false),
                    arguments(List.of(""), null, false),
                    arguments(List.of(""), List.of(""), true),
                    arguments(List.of(""), List.of("a"), false),
                    arguments(List.of("a"), List.of(""), false),
                    arguments(List.of("a"), List.of("a"), true),
                    arguments(List.of("a"), List.of("b"), false),
                    arguments(List.of("a", "b"), List.of("a", "b"), true),
                    arguments(List.of("a"), List.of("a", "b"), false),
                    arguments(List.of("a", "b"), List.of("a"), false)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void equals(List<String> a, List<String> b, boolean expected) {
            assertEquals(expected, ConferenceDataLoaderExecutor.equals(a, b));
        }
    }

    @Test
    void createEventTemplate() {
        final String EN_TEXT = "Text";
        final String RU_TEXT = "Текст";
        final long PLACE_ID = 42;

        Event actual = ConferenceDataLoaderExecutor.createEventTemplate(EN_TEXT, RU_TEXT, List.of(PLACE_ID));

        assertEquals(EN_TEXT, LocalizationUtils.getString(actual.getName(), Language.ENGLISH));
        assertEquals(RU_TEXT, LocalizationUtils.getString(actual.getName(), Language.RUSSIAN));
        assertEquals(PLACE_ID, actual.getDays().get(0).getPlace().getId());
    }

    @Test
    void main() {
        assertDoesNotThrow(() -> ConferenceDataLoaderExecutor.main(new String[]{}));
    }
}
