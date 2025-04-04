package guess.util.load.jrgcms;

import guess.domain.Conference;
import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.source.cms.jrgcms.JrgCmsLinks;
import guess.domain.source.cms.jrgcms.JrgCmsObject;
import guess.domain.source.cms.jrgcms.JrgCmsPhoto;
import guess.domain.source.cms.jrgcms.JrgCmsTokenResponse;
import guess.domain.source.cms.jrgcms.event.JrgCmsAboutPage;
import guess.domain.source.cms.jrgcms.event.JrgCmsConferenceSiteContent;
import guess.domain.source.cms.jrgcms.event.JrgCmsConferenceSiteContentResponse;
import guess.domain.source.cms.jrgcms.event.JrgCmsEvent;
import guess.domain.source.cms.jrgcms.speaker.JrgCmsParticipant;
import guess.domain.source.cms.jrgcms.speaker.JrgCmsParticipation;
import guess.domain.source.cms.jrgcms.speaker.JrgCmsSpeaker;
import guess.domain.source.cms.jrgcms.speaker.JrgContact;
import guess.domain.source.cms.jrgcms.talk.*;
import guess.domain.source.cms.jrgcms.talk.schedule.*;
import guess.domain.source.image.UrlDates;
import guess.util.FileUtils;
import guess.util.LocalizationUtils;
import guess.util.load.CmsDataLoader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.stubbing.Answer;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static guess.util.load.jrgcms.JrgCmsDataLoader.IMAGE_PARAMETERS_TEMPLATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("JrgCmsDataLoader class tests")
class JrgCmsDataLoaderTest {
    private static final String OPTIONS_DIRECTORY_NAME = "options";

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.deleteDirectory(OPTIONS_DIRECTORY_NAME);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(OPTIONS_DIRECTORY_NAME);
    }

    @Test
    void getRestTemplate() {
        assertDoesNotThrow(JrgCmsDataLoader::getRestTemplate);
    }

    @Test
    void getOptionsDirectoryName() {
        assertNotNull(JrgCmsDataLoader.getOptionsDirectoryName());
    }

    @Test
    void getEventId() {
        assertDoesNotThrow(() -> new JrgCmsDataLoader().getEventId());
    }

    @Test
    void setEventId() {
        assertDoesNotThrow(() -> new JrgCmsDataLoader().setEventId(42L));
    }

    @Test
    void getTokenFromCacheAndStoreTokenInCache() throws IOException {
        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            mockedStatic.when(JrgCmsDataLoader::getOptionsDirectoryName)
                    .thenReturn(OPTIONS_DIRECTORY_NAME);
            mockedStatic.when(() -> JrgCmsDataLoader.storeTokenInCache(Mockito.any(JrgCmsTokenResponse.class)))
                    .thenCallRealMethod();
            mockedStatic.when(JrgCmsDataLoader::getTokenFromCache)
                    .thenCallRealMethod();

            final String accessToken = "ACCESS_TOKEN";

            JrgCmsTokenResponse tokenResponse = JrgCmsDataLoader.getTokenFromCache();

            assertNull(tokenResponse);

            JrgCmsTokenResponse jrgCmsTokenResponse = new JrgCmsTokenResponse();
            jrgCmsTokenResponse.setAccessToken(accessToken);

            assertDoesNotThrow(() -> JrgCmsDataLoader.storeTokenInCache(jrgCmsTokenResponse));

            tokenResponse = JrgCmsDataLoader.getTokenFromCache();

            assertNotNull(tokenResponse);
            assertEquals(accessToken, tokenResponse.getAccessToken());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getToken method tests")
    class GetTokenTest {
        private Stream<Arguments> data() {
            final String clientId = "clientId";
            final String clientSecret = "clientSecret";

            return Stream.of(
                    arguments(null, null, IllegalArgumentException.class),
                    arguments(null, clientSecret, IllegalArgumentException.class),
                    arguments(clientId, null, IllegalArgumentException.class),
                    arguments(clientId, clientSecret, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getToken(String clientId, String clientSecret, Class<? extends Exception> expectedException) {
            try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
                mockedStatic.when(() -> JrgCmsDataLoader.getToken(Mockito.nullable(String.class), Mockito.nullable(String.class)))
                        .thenCallRealMethod();

                if (expectedException == null) {
                    final String accessToken = "accessToken";

                    JrgCmsTokenResponse tokenResponse = new JrgCmsTokenResponse();
                    tokenResponse.setAccessToken(accessToken);

                    ResponseEntity<JrgCmsTokenResponse> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);

                    RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
                    Mockito.when(restTemplateMock.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.POST),
                                    Mockito.nullable(HttpEntity.class), Mockito.eq(JrgCmsTokenResponse.class)))
                            .thenReturn(responseEntity);

                    mockedStatic.when(JrgCmsDataLoader::getRestTemplate)
                            .thenReturn(restTemplateMock);

                    JrgCmsTokenResponse actual = JrgCmsDataLoader.getToken(clientId, clientSecret);

                    assertEquals(accessToken, actual.getAccessToken());
                } else {
                    assertThrows(expectedException, () -> JrgCmsDataLoader.getToken(clientId, clientSecret));
                }
            }
        }
    }

    @Test
    void getToken() {
        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            mockedStatic.when(JrgCmsDataLoader::getToken)
                    .thenCallRealMethod();

            JrgCmsDataLoader.getToken();

            mockedStatic.verify(JrgCmsDataLoader::getToken);
            mockedStatic.verify(() -> JrgCmsDataLoader.getToken(Mockito.nullable(String.class), Mockito.nullable(String.class)));
            mockedStatic.verifyNoMoreInteractions();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("makeRequest method tests")
    class MakeRequestTest {
        private Stream<Arguments> data() {
            final int result = 42;
            final String accessToken0 = "accessToken0";
            final String accessToken1 = "accessToken1";
            final String accessToken2 = "accessToken2";
            final String accessToken3 = "accessToken3";

            Function<String, Integer> requestFunction0 = s -> result;

            Function<String, Integer> requestFunction1 = s -> {
                return switch (s) {
                    case accessToken0, accessToken2 ->
                            throw HttpClientErrorException.create(null, HttpStatus.UNAUTHORIZED, "", new HttpHeaders(), null, null);
                    case accessToken1 -> result;
                    default ->
                            throw HttpClientErrorException.create(null, HttpStatus.BAD_REQUEST, "", new HttpHeaders(), null, null);
                };
            };

            JrgCmsTokenResponse tokenResponse0 = new JrgCmsTokenResponse();

            JrgCmsTokenResponse tokenResponse1 = new JrgCmsTokenResponse();
            tokenResponse1.setAccessToken(accessToken0);

            JrgCmsTokenResponse tokenResponse2 = new JrgCmsTokenResponse();
            tokenResponse2.setAccessToken(accessToken1);

            JrgCmsTokenResponse tokenResponse3 = new JrgCmsTokenResponse();
            tokenResponse3.setAccessToken(accessToken2);

            JrgCmsTokenResponse tokenResponse4 = new JrgCmsTokenResponse();
            tokenResponse4.setAccessToken(accessToken3);

            return Stream.of(
                    arguments(requestFunction0, null, tokenResponse0, null, result),
                    arguments(requestFunction0, tokenResponse0, tokenResponse0, null, result),
                    arguments(requestFunction0, tokenResponse1, null, null, result),
                    arguments(requestFunction1, tokenResponse1, tokenResponse2, null, result),
                    arguments(requestFunction1, tokenResponse1, tokenResponse3, HttpClientErrorException.Unauthorized.class, null),
                    arguments(requestFunction1, tokenResponse1, tokenResponse4, HttpClientErrorException.BadRequest.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void makeRequest(Function<String, Integer> requestFunction, JrgCmsTokenResponse getTokenFromCacheResult,
                         JrgCmsTokenResponse getTokenResult, Class<? extends Exception> expectedException, Integer expectedValue)
                throws IOException, NoSuchFieldException {
            try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
                mockedStatic.when(() -> JrgCmsDataLoader.makeRequest(Mockito.any(Function.class)))
                        .thenCallRealMethod();
                mockedStatic.when(JrgCmsDataLoader::getTokenFromCache)
                        .thenReturn(getTokenFromCacheResult);
                mockedStatic.when(JrgCmsDataLoader::getToken)
                        .thenReturn(getTokenResult);

                if (expectedException == null) {
                    assertEquals(expectedValue, JrgCmsDataLoader.makeRequest(requestFunction));
                } else {
                    assertThrowsExactly(expectedException, () -> JrgCmsDataLoader.makeRequest(requestFunction));
                }
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTags() throws IOException, NoSuchFieldException {
        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            final String mobiusEventProject = "MOBIUS";
            final String heisenbugEventProject = "HEISENBUG";
            final String springEventVersion = "2022 Spring";
            final String autumnEventVersion = "2022 Autumn";

            // Event projects
            JrgCmsObject<String> eventProjectObject0 = new JrgCmsObject<>();
            eventProjectObject0.setIv(mobiusEventProject);

            JrgCmsObject<String> eventProjectObject1 = new JrgCmsObject<>();
            eventProjectObject1.setIv(heisenbugEventProject);

            // Event versions
            JrgCmsObject<String> eventVersion0 = new JrgCmsObject<>();
            eventVersion0.setIv(springEventVersion);

            JrgCmsObject<String> eventVersion1 = new JrgCmsObject<>();
            eventVersion1.setIv(autumnEventVersion);

            // Events
            JrgCmsEvent jrgCmsEvent0 = new JrgCmsEvent();
            jrgCmsEvent0.setEventProject(eventProjectObject0);
            jrgCmsEvent0.setEventVersion(eventVersion0);

            JrgCmsEvent jrgCmsEvent1 = new JrgCmsEvent();
            jrgCmsEvent1.setEventProject(eventProjectObject1);
            jrgCmsEvent1.setEventVersion(eventVersion0);

            JrgCmsEvent jrgCmsEvent2 = new JrgCmsEvent();
            jrgCmsEvent2.setEventProject(eventProjectObject1);
            jrgCmsEvent2.setEventVersion(eventVersion1);

            // Conference sile contents
            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent0 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent0.setData(jrgCmsEvent0);

            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent1 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent1.setData(jrgCmsEvent1);

            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent2 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent2.setData(jrgCmsEvent2);

            // Conference sile content list
            JrgCmsConferenceSiteContentResponse response = new JrgCmsConferenceSiteContentResponse();
            response.setItems(List.of(jrgCmsConferenceSiteContent0, jrgCmsConferenceSiteContent1, jrgCmsConferenceSiteContent2));

            ResponseEntity<JrgCmsConferenceSiteContentResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET),
                            Mockito.nullable(HttpEntity.class), Mockito.eq(JrgCmsConferenceSiteContentResponse.class)))
                    .thenReturn(responseEntity);

            // Mock methods
            mockedStatic.when(() -> JrgCmsDataLoader.makeRequest(Mockito.any(Function.class)))
                    .thenAnswer(
                            (Answer<Map<String, List<String>>>) invocation -> {
                                Object[] args = invocation.getArguments();
                                Function<String, Map<String, List<String>>> requestFunction = (Function<String, Map<String, List<String>>>) args[0];

                                return requestFunction.apply("");
                            }
                    );
            mockedStatic.when(JrgCmsDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);

            // Mock method under test
            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
            Mockito.when(jrgCmsDataLoader.getTags(Mockito.anyString()))
                    .thenCallRealMethod();

            // Expected result
            Map<String, List<String>> expected = new LinkedHashMap<>();
            expected.put(heisenbugEventProject, List.of(autumnEventVersion, springEventVersion));
            expected.put(mobiusEventProject, List.of(springEventVersion));

            // Actual result
            Map<String, List<String>> actual = jrgCmsDataLoader.getTags("2022");

            assertEquals(expected, actual);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getFilterValue method tests")
    class GetFilterValueTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null),
                    arguments("GOFUNC", "GOFUNC"),
                    arguments("I'ML", "I''ML")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getFilterValue(String source, String expected) {
            assertEquals(expected, JrgCmsDataLoader.getFilterValue(source));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEventTypes() throws IOException, NoSuchFieldException {
        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            final String mobiusEventProject = "MOBIUS";
            final String heisenbugEventProject = "HEISENBUG";
            final String springEventVersion = "2022 Spring";
            final String autumnEventVersion = "2022 Autumn";

            // Event projects
            JrgCmsObject<String> eventProjectObject0 = new JrgCmsObject<>();
            eventProjectObject0.setIv(mobiusEventProject);

            JrgCmsObject<String> eventProjectObject1 = new JrgCmsObject<>();
            eventProjectObject1.setIv(heisenbugEventProject);

            // Event versions
            JrgCmsObject<String> eventVersion0 = new JrgCmsObject<>();
            eventVersion0.setIv(springEventVersion);

            JrgCmsObject<String> eventVersion1 = new JrgCmsObject<>();
            eventVersion1.setIv(autumnEventVersion);

            // Event about pages
            JrgCmsAboutPage jrgCmsAboutPage0 = new JrgCmsAboutPage();
            jrgCmsAboutPage0.setMain("");

            JrgCmsAboutPage jrgCmsAboutPage1 = new JrgCmsAboutPage();
            jrgCmsAboutPage1.setMain("");

            Map<String, JrgCmsAboutPage> aboutPage = new HashMap<>();
            aboutPage.put(JrgCmsDataLoader.ENGLISH_TEXT_KEY, jrgCmsAboutPage0);
            aboutPage.put(JrgCmsDataLoader.RUSSIAN_TEXT_KEY, jrgCmsAboutPage1);

            // Events
            JrgCmsEvent jrgCmsEvent0 = new JrgCmsEvent();
            jrgCmsEvent0.setEventProject(eventProjectObject0);
            jrgCmsEvent0.setEventVersion(eventVersion0);
            jrgCmsEvent0.setAboutPage(aboutPage);

            JrgCmsEvent jrgCmsEvent1 = new JrgCmsEvent();
            jrgCmsEvent1.setEventProject(eventProjectObject1);
            jrgCmsEvent1.setEventVersion(eventVersion0);
            jrgCmsEvent1.setAboutPage(aboutPage);

            JrgCmsEvent jrgCmsEvent2 = new JrgCmsEvent();
            jrgCmsEvent2.setEventProject(eventProjectObject1);
            jrgCmsEvent2.setEventVersion(eventVersion1);
            jrgCmsEvent2.setAboutPage(aboutPage);

            // Conference sile contents
            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent0 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent0.setData(jrgCmsEvent0);

            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent1 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent1.setData(jrgCmsEvent1);

            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent2 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent2.setData(jrgCmsEvent2);

            // Conference site content list
            JrgCmsConferenceSiteContentResponse response = new JrgCmsConferenceSiteContentResponse();
            response.setItems(List.of(jrgCmsConferenceSiteContent0, jrgCmsConferenceSiteContent1, jrgCmsConferenceSiteContent2));

            ResponseEntity<JrgCmsConferenceSiteContentResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

            // Mock RestTemplate
            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET),
                            Mockito.nullable(HttpEntity.class), Mockito.eq(JrgCmsConferenceSiteContentResponse.class)))
                    .thenReturn(responseEntity);

            // Mock methods
            mockedStatic.when(() -> JrgCmsDataLoader.makeRequest(Mockito.any(Function.class)))
                    .thenAnswer(
                            (Answer<List<EventType>>) invocation -> {
                                Object[] args = invocation.getArguments();
                                Function<String, List<EventType>> requestFunction = (Function<String, List<EventType>>) args[0];

                                return requestFunction.apply("");
                            }
                    );
            mockedStatic.when(JrgCmsDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);
            mockedStatic.when(() -> JrgCmsDataLoader.createEventType(Mockito.any(JrgCmsEvent.class), Mockito.any(AtomicLong.class), Mockito.any(Conference.class)))
                    .thenAnswer(
                            (Answer<EventType>) invocation -> {
                                Object[] args = invocation.getArguments();
                                Conference conference = (Conference) args[2];

                                EventType eventType = new EventType();
                                eventType.setConference(conference);

                                return eventType;
                            }
                    );

            // Mock method under test
            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
            Mockito.when(jrgCmsDataLoader.getEventTypes())
                    .thenCallRealMethod();

            // Actual result
            List<EventType> actual = jrgCmsDataLoader.getEventTypes();

            assertEquals(2, actual.size());
            assertEquals(1, actual.stream()
                    .filter(et -> Conference.MOBIUS.equals(et.getConference()))
                    .count());
            assertEquals(1, actual.stream()
                    .filter(et -> Conference.HEISENBUG.equals(et.getConference()))
                    .count());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createEventType method tests")
    class CreateEventTypeTest {
        private JrgCmsEvent createJrgCmsEvent(String enLongDescription, String ruLongDescription) {
            JrgCmsAboutPage jrgCmsAboutPage0 = new JrgCmsAboutPage();
            jrgCmsAboutPage0.setMain(enLongDescription);

            JrgCmsAboutPage jrgCmsAboutPage1 = new JrgCmsAboutPage();
            jrgCmsAboutPage1.setMain(ruLongDescription);

            Map<String, JrgCmsAboutPage> aboutPage0 = new HashMap<>();
            aboutPage0.put(JrgCmsDataLoader.ENGLISH_TEXT_KEY, jrgCmsAboutPage0);
            aboutPage0.put(JrgCmsDataLoader.RUSSIAN_TEXT_KEY, jrgCmsAboutPage1);

            JrgCmsEvent jrgCmsEvent = new JrgCmsEvent();
            jrgCmsEvent.setAboutPage(aboutPage0);

            return jrgCmsEvent;
        }

        private EventType createEventType(long id, Conference conference, String enLongDescription, String ruLongDescription) {
            EventType eventType = new EventType();

            eventType.setId(id);
            eventType.setConference(conference);
            eventType.setLongDescription(List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), enLongDescription),
                    new LocaleItem(Language.RUSSIAN.getCode(), ruLongDescription)
            ));

            return eventType;
        }

        private Stream<Arguments> data() {
            final long id0 = 42;
            final long id1 = 43;
            final String englishLongDescription0 = "LongDescription0";
            final String englishLongDescription1 = "LongDescription1";
            final String russianLongDescription0 = "ДлинноеОписание0";
            final String russianLongDescription1 = "ДлинноеОписание1";
            final Conference conference0 = Conference.HEISENBUG;
            final Conference conference1 = Conference.MOBIUS;

            JrgCmsEvent jrgCmsEvent0 = createJrgCmsEvent(englishLongDescription0, russianLongDescription0);
            JrgCmsEvent jrgCmsEvent1 = createJrgCmsEvent(englishLongDescription1, russianLongDescription1);

            EventType eventType0 = createEventType(id0, conference0, englishLongDescription0, russianLongDescription0);
            EventType eventType1 = createEventType(id1, conference1, englishLongDescription1, russianLongDescription1);

            return Stream.of(
                    arguments(jrgCmsEvent0, new AtomicLong(id0), conference0, eventType0),
                    arguments(jrgCmsEvent1, new AtomicLong(id1), conference1, eventType1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createEventType(JrgCmsEvent et, AtomicLong id, Conference conference, EventType expected) {
            EventType actual = JrgCmsDataLoader.createEventType(et, id, conference);

            assertEquals(expected, actual);
            assertEquals(expected.getConference(), actual.getConference());
            assertEquals(expected.getLongDescription(), actual.getLongDescription());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEvent method tests")
    class GetEventTest {
        private Stream<Arguments> data() {
            final String enName0 = "Name0";
            final String ruName0 = "Наименование0";
            final String enName1 = "Name1";
            final String ruName1 = "Наименование1";
            final String enName2 = "Name2";
            final String ruName2 = "Наименование2";
            final LocalDate startDate0 = LocalDate.of(2022, 6, 29);
            final LocalDate endDate0 = LocalDate.of(2022, 6, 30);
            final LocalDate startDate1 = LocalDate.of(2022, 7, 1);
            final LocalDate endDate1 = LocalDate.of(2022, 7, 2);

            // Names
            List<LocaleItem> name0 = List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), enName0),
                    new LocaleItem(Language.RUSSIAN.getCode(), ruName0)
            );

            List<LocaleItem> name1 = List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), enName1),
                    new LocaleItem(Language.RUSSIAN.getCode(), ruName1)
            );

            List<LocaleItem> name2 = List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), enName2),
                    new LocaleItem(Language.RUSSIAN.getCode(), ruName2)
            );

            // Places
            Place place0 = new Place();
            place0.setId(0);

            Place place1 = new Place();
            place1.setId(1);

            // Event templates
            Event eventTemplate0 = new Event();
            eventTemplate0.setName(name0);
            eventTemplate0.setDays(Collections.emptyList());

            Event eventTemplate1 = new Event();
            eventTemplate1.setName(name1);
            eventTemplate1.setDays(List.of(new EventDays(null, null, place0)));

            Event eventTemplate2 = new Event();
            eventTemplate2.setName(name2);
            eventTemplate2.setDays(List.of(new EventDays(null, null, place1)));

            // Events
            Event event0 = new Event();
            event0.setId(-1L);
            event0.setName(name1);
            event0.setDays(List.of(new EventDays(startDate0, endDate0, place0)));

            Event event1 = new Event();
            event1.setId(-1L);
            event1.setName(name2);
            event1.setDays(List.of(new EventDays(startDate1, endDate1, place1)));

            // Results of getEventDatesList method
            List<JrgCmsDataLoader.EventDates> getEventDatesListResult0 = List.of(
                    new JrgCmsDataLoader.EventDates(startDate0, endDate0)
            );

            List<JrgCmsDataLoader.EventDates> getEventDatesListResult1 = List.of(
                    new JrgCmsDataLoader.EventDates(startDate1, endDate1)
            );

            return Stream.of(
                    arguments(null, null, null, eventTemplate0, null, getEventDatesListResult0, IllegalArgumentException.class, null),
                    arguments(null, null, null, eventTemplate1, null, getEventDatesListResult0, null, event0),
                    arguments(null, null, null, eventTemplate2, null, getEventDatesListResult1, null, event1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEvent(Conference conference, LocalDate startDate, String conferenceCode, Event eventTemplate, String timeZone,
                      List<JrgCmsDataLoader.EventDates> getEventDatesListResult, Class<? extends Exception> expectedException,
                      Event expectedValue) throws IOException, NoSuchFieldException {
            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);

            Mockito.when(jrgCmsDataLoader.getEvent(Mockito.nullable(Conference.class), Mockito.nullable(LocalDate.class),
                            Mockito.nullable(String.class), Mockito.any(Event.class), Mockito.nullable(String.class)))
                    .thenCallRealMethod();
            Mockito.when(jrgCmsDataLoader.getEventId(Mockito.any(Conference.class), Mockito.any(String.class)))
                    .thenReturn(42L);
            Mockito.when(jrgCmsDataLoader.getEventDatesList(Mockito.anyLong(), Mockito.nullable(String.class)))
                    .thenReturn(getEventDatesListResult);

            if (expectedException == null) {
                Event actual = jrgCmsDataLoader.getEvent(conference, startDate, conferenceCode, eventTemplate, timeZone);

                assertEquals(expectedValue, actual);
                assertEquals(expectedValue.getName(), actual.getName());
                assertEquals(expectedValue.getDays(), actual.getDays());
            } else {
                assertThrows(expectedException, () -> jrgCmsDataLoader.getEvent(conference, startDate, conferenceCode,
                        eventTemplate, timeZone));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getTalks method tests")
    class GetTalksTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null, false, null, null),
                    arguments(null, null, null, false, null, 42L)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getTalks(Conference conference, LocalDate startDate, String conferenceCode, boolean ignoreDemoStage,
                      String timeZone, Long eventId) throws IOException, NoSuchFieldException {
            AtomicReference<Long> internalEventId = new AtomicReference<>();

            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);

            Mockito.when(jrgCmsDataLoader.getTalks(Mockito.nullable(Conference.class), Mockito.nullable(LocalDate.class),
                            Mockito.nullable(String.class), Mockito.anyBoolean(), Mockito.nullable(String.class)))
                    .thenCallRealMethod();
            Mockito.when(jrgCmsDataLoader.getEventId())
                    .thenAnswer(
                            (Answer<Long>) invocation -> (eventId != null) ? eventId : internalEventId.get()
                    );
            Mockito.when(jrgCmsDataLoader.getEventId(Mockito.nullable(Conference.class), Mockito.nullable(String.class)))
                    .thenReturn(43L);
            Mockito.doAnswer(
                    invocation -> {
                        internalEventId.set(invocation.getArgument(0));

                        return null;
                    }
            ).when(jrgCmsDataLoader).setEventId(Mockito.nullable(Long.class));

            assertDoesNotThrow(() -> jrgCmsDataLoader.getTalks(conference, startDate, conferenceCode, ignoreDemoStage, timeZone));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEventId method tests")
    class GetEventIdTest {
        private Stream<Arguments> data() {
            final Conference conference = Conference.JPOINT;
            final String conferenceCode = "2022 Spring";
            final long iv0 = 42;
            final long iv1 = 43;

            // Events ids
            JrgCmsObject<Long> eventId0 = new JrgCmsObject<>();
            eventId0.setIv(iv0);

            JrgCmsObject<Long> eventId1 = new JrgCmsObject<>();
            eventId1.setIv(iv1);

            // Events
            JrgCmsEvent jrgCmsEvent0 = new JrgCmsEvent();
            jrgCmsEvent0.setEventId(eventId0);

            JrgCmsEvent jrgCmsEvent1 = new JrgCmsEvent();
            jrgCmsEvent1.setEventId(eventId1);

            // Conference sile contents
            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent0 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent0.setData(jrgCmsEvent0);

            JrgCmsConferenceSiteContent jrgCmsConferenceSiteContent1 = new JrgCmsConferenceSiteContent();
            jrgCmsConferenceSiteContent1.setData(jrgCmsEvent1);

            return Stream.of(
                    arguments(conference, conferenceCode, Collections.emptyList(), IllegalStateException.class, 0),
                    arguments(conference, conferenceCode, List.of(jrgCmsConferenceSiteContent0, jrgCmsConferenceSiteContent1), IllegalStateException.class, 0),
                    arguments(conference, conferenceCode, List.of(jrgCmsConferenceSiteContent0), null, iv0),
                    arguments(conference, conferenceCode, List.of(jrgCmsConferenceSiteContent1), null, iv1)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void getEventId(Conference conference, String conferenceCode, List<JrgCmsConferenceSiteContent> items,
                        Class<? extends Exception> expectedException, long expectedValue) throws IOException, NoSuchFieldException {
            try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
                // Conference site content list
                JrgCmsConferenceSiteContentResponse response = new JrgCmsConferenceSiteContentResponse();
                response.setItems(items);

                ResponseEntity<JrgCmsConferenceSiteContentResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

                // Mock RestTemplate
                RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
                Mockito.when(restTemplateMock.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET),
                                Mockito.nullable(HttpEntity.class), Mockito.eq(JrgCmsConferenceSiteContentResponse.class)))
                        .thenReturn(responseEntity);

                // Mock methods
                mockedStatic.when(() -> JrgCmsDataLoader.makeRequest(Mockito.any(Function.class)))
                        .thenAnswer(
                                (Answer<Long>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Function<String, Long> requestFunction = (Function<String, Long>) args[0];

                                    return requestFunction.apply("");
                                }
                        );
                mockedStatic.when(JrgCmsDataLoader::getRestTemplate)
                        .thenReturn(restTemplateMock);

                // Mock method under test
                JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
                Mockito.when(jrgCmsDataLoader.getEventId(Mockito.any(Conference.class), Mockito.anyString()))
                        .thenCallRealMethod();

                if (expectedException == null) {
                    assertEquals(expectedValue, jrgCmsDataLoader.getEventId(conference, conferenceCode));
                } else {
                    assertThrows(expectedException, () -> jrgCmsDataLoader.getEventId(conference, conferenceCode));
                }
            }
        }
    }

    @Test
    void getScheduleInfo() {
        try (MockedStatic<JrgCmsDataLoader> jrgCmsDataLoaderMockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class);
             MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
            // Days
            JrgCmsDay jrgCmsDay0 = new JrgCmsDay();
            jrgCmsDay0.setDayStartsAt("2022-05-14T00:00:00Z");

            JrgCmsDay jrgCmsDay1 = new JrgCmsDay();
            jrgCmsDay1.setDayStartsAt("2022-05-15T00:00:00Z");

            JrgCmsDay jrgCmsDay2 = new JrgCmsDay();
            jrgCmsDay2.setDayStartsAt("2022-05-17T00:00:00Z");

            // Schedule
            JrgCmsSchedule jrgCmsSchedule = new JrgCmsSchedule();
            jrgCmsSchedule.setDays(List.of(jrgCmsDay0, jrgCmsDay1, jrgCmsDay2));

            // Schedule response
            JrgCmsScheduleResponse response = new JrgCmsScheduleResponse();
            response.setData(jrgCmsSchedule);

            // Mock RestTemplate
            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.eq(JrgCmsScheduleResponse.class)))
                    .thenReturn(response);

            // Mock methods
            jrgCmsDataLoaderMockedStatic.when(JrgCmsDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);
            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.createEventLocalDate(Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(
                            (Answer<LocalDate>) invocation -> {
                                Object[] args = invocation.getArguments();
                                String zonedDateTimeString = (String) args[0];

                                return ZonedDateTime.ofInstant(
                                                ZonedDateTime.parse(zonedDateTimeString).toInstant(),
                                                ZoneId.of("UTC"))
                                        .toLocalDate();
                            }
                    );

            // Mock method under test
            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
            Mockito.when(jrgCmsDataLoader.getScheduleInfo(Mockito.anyLong(), Mockito.anyString()))
                    .thenCallRealMethod();

            // Expected result
            List<JrgCmsDataLoader.EventDates> expectedEventDatesList = List.of(
                    new JrgCmsDataLoader.EventDates(LocalDate.of(2022, 5, 14),
                            LocalDate.of(2022, 5, 15)),
                    new JrgCmsDataLoader.EventDates(LocalDate.of(2022, 5, 17),
                            LocalDate.of(2022, 5, 17))
            );

            // Actual result
            JrgCmsDataLoader.ScheduleInfo actual = jrgCmsDataLoader.getScheduleInfo(42, "Europe/Moscow");

            assertEquals(expectedEventDatesList, actual.eventDatesList());
        }
    }

    @Test
    void getEventDatesList() {
        final long eventId = 42;
        final String timeZone = "Europe/Moscow";

        JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
        Mockito.when(jrgCmsDataLoader.getEventDatesList(Mockito.anyLong(), Mockito.anyString()))
                .thenCallRealMethod();
        Mockito.when(jrgCmsDataLoader.getScheduleInfo(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(new JrgCmsDataLoader.ScheduleInfo(Collections.emptyList(), Collections.emptyList()));

        assertDoesNotThrow(() -> jrgCmsDataLoader.getEventDatesList(eventId, timeZone));
        Mockito.verify(jrgCmsDataLoader, VerificationModeFactory.times(1)).getEventDatesList(eventId, timeZone);
        Mockito.verify(jrgCmsDataLoader, VerificationModeFactory.times(1)).getScheduleInfo(eventId, timeZone);
        Mockito.verifyNoMoreInteractions(jrgCmsDataLoader);
    }

    @Test
    void getDayTrackTimeMap() {
        try (MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
            final String activityId1 = "1";
            final String activityId2 = "2";
            final String activityId3 = "3";
            final String activityId4 = "4";

            // Activities
            JrgCmsActivity jrgCmsActivity1 = new JrgCmsActivity();
            jrgCmsActivity1.setId(activityId1);

            JrgCmsActivity jrgCmsActivity2 = new JrgCmsActivity();
            jrgCmsActivity2.setId(activityId2);

            JrgCmsActivity jrgCmsActivity3 = new JrgCmsActivity();
            jrgCmsActivity3.setId(activityId3);

            JrgCmsActivity jrgCmsActivity4 = new JrgCmsActivity();
            jrgCmsActivity4.setId(activityId4);

            // Slots
            JrgCmsSlot jrgCmsSlot0 = new JrgCmsSlot();

            JrgCmsSlot jrgCmsSlot1 = new JrgCmsSlot();
            jrgCmsSlot1.setSlotStartTime("2022-05-14T08:45:00Z");
            jrgCmsSlot1.setSlotEndTime("2022-05-14T09:00:00Z");
            jrgCmsSlot1.setActivity(jrgCmsActivity1);

            JrgCmsSlot jrgCmsSlot2 = new JrgCmsSlot();
            jrgCmsSlot2.setSlotStartTime("2022-05-14T09:45:00Z");
            jrgCmsSlot2.setSlotEndTime("2022-05-14T10:00:00Z");
            jrgCmsSlot2.setActivity(jrgCmsActivity2);

            JrgCmsSlot jrgCmsSlot3 = new JrgCmsSlot();
            jrgCmsSlot3.setSlotStartTime("2022-05-14T11:15:00Z");
            jrgCmsSlot3.setSlotEndTime("2022-05-14T11:30:00Z");
            jrgCmsSlot3.setActivity(jrgCmsActivity3);

            JrgCmsSlot jrgCmsSlot4 = new JrgCmsSlot();
            jrgCmsSlot4.setSlotStartTime("2022-05-14T13:00:00Z");
            jrgCmsSlot4.setSlotEndTime("2022-05-14T13:15:00Z");
            jrgCmsSlot4.setActivity(jrgCmsActivity4);

            // Tracks
            JrgCmsTrack jrgCmsTrack0 = new JrgCmsTrack();
            jrgCmsTrack0.setTrackNumber(1L);
            jrgCmsTrack0.setSlots(List.of(jrgCmsSlot0));

            JrgCmsTrack jrgCmsTrack1 = new JrgCmsTrack();
            jrgCmsTrack1.setTrackNumber(2L);
            jrgCmsTrack1.setSlots(List.of(jrgCmsSlot1));

            JrgCmsTrack jrgCmsTrack2 = new JrgCmsTrack();
            jrgCmsTrack2.setTrackNumber(3L);
            jrgCmsTrack2.setSlots(List.of(jrgCmsSlot2));

            JrgCmsTrack jrgCmsTrack3 = new JrgCmsTrack();
            jrgCmsTrack3.setTrackNumber(4L);
            jrgCmsTrack3.setSlots(List.of(jrgCmsSlot3));

            JrgCmsTrack jrgCmsTrack4 = new JrgCmsTrack();
            jrgCmsTrack4.setTrackNumber(5L);
            jrgCmsTrack4.setSlots(List.of(jrgCmsSlot4));

            // Days
            JrgCmsDay jrgCmsDay0 = new JrgCmsDay();
            jrgCmsDay0.setDayNumber(1L);
            jrgCmsDay0.setTracks(List.of(jrgCmsTrack0, jrgCmsTrack1));

            JrgCmsDay jrgCmsDay1 = new JrgCmsDay();
            jrgCmsDay1.setDayNumber(2L);
            jrgCmsDay1.setTracks(List.of(jrgCmsTrack2, jrgCmsTrack3));

            JrgCmsDay jrgCmsDay2 = new JrgCmsDay();
            jrgCmsDay2.setDayNumber(3L);
            jrgCmsDay2.setTracks(List.of(jrgCmsTrack4));

            // Mock methods
            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
            Mockito.when(jrgCmsDataLoader.getDayTrackTimeMap(Mockito.anyLong(), Mockito.nullable(String.class)))
                    .thenCallRealMethod();
            Mockito.when(jrgCmsDataLoader.getScheduleInfo(Mockito.anyLong(), Mockito.nullable(String.class)))
                    .thenReturn(new JrgCmsDataLoader.ScheduleInfo(List.of(jrgCmsDay0, jrgCmsDay1, jrgCmsDay2), Collections.emptyList()));

            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.createEventLocalTime(Mockito.anyString(), Mockito.nullable(String.class)))
                    .thenAnswer(
                            (Answer<LocalTime>) invocation -> {
                                Object[] args = invocation.getArguments();
                                String zonedDateTimeString = (String) args[0];

                                return ZonedDateTime.ofInstant(
                                                ZonedDateTime.parse(zonedDateTimeString).toInstant(),
                                                ZoneId.of("UTC"))
                                        .toLocalTime();
                            }
                    );

            // Expected result
            Map<String, DayTrackTime> expected = new HashMap<>();
            expected.put(activityId1, new DayTrackTime(1L, 2L, LocalTime.of(8, 45), LocalTime.of(9, 0)));
            expected.put(activityId2, new DayTrackTime(2L, 3L, LocalTime.of(9, 45), LocalTime.of(10, 0)));
            expected.put(activityId3, new DayTrackTime(2L, 4L, LocalTime.of(11, 15), LocalTime.of(11, 30)));
            expected.put(activityId4, new DayTrackTime(3L, 5L, LocalTime.of(13, 0), LocalTime.of(13, 15)));

            assertEquals(expected, jrgCmsDataLoader.getDayTrackTimeMap(42, null));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTalks() {
        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            final String id0 = "0";
            final String id1 = "1";
            final String id2 = "2";

            // Activities
            JrgCmsActivity jrgCmsActivity0 = new JrgCmsActivity();
            jrgCmsActivity0.setId(id0);

            JrgCmsActivity jrgCmsActivity1 = new JrgCmsActivity();
            jrgCmsActivity1.setId(id1);

            JrgCmsActivity jrgCmsActivity2 = new JrgCmsActivity();
            jrgCmsActivity2.setId(id2);

            // Activity response
            JrgCmsActivityResponse response = new JrgCmsActivityResponse();
            response.setData(List.of(jrgCmsActivity0, jrgCmsActivity1, jrgCmsActivity2));

            // Mock RestTemplate
            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.eq(JrgCmsActivityResponse.class)))
                    .thenReturn(response);

            // Mock methods
            mockedStatic.when(JrgCmsDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);
            mockedStatic.when(() -> JrgCmsDataLoader.isValidTalk(Mockito.any(JrgCmsActivity.class), Mockito.anyBoolean()))
                    .thenReturn(true);
            mockedStatic.when(() -> JrgCmsDataLoader.getSpeakerMap(Mockito.anyList()))
                    .thenReturn(Collections.emptyMap());
            mockedStatic.when(() -> JrgCmsDataLoader.createTalk(Mockito.any(JrgCmsActivity.class), Mockito.anyMap(), Mockito.any(AtomicLong.class), Mockito.anyMap()))
                    .thenAnswer(
                            (Answer<Talk>) invocation -> {
                                Object[] args = invocation.getArguments();
                                JrgCmsActivity jrgCmsActivity = (JrgCmsActivity) args[0];
                                Map<String, DayTrackTime> dayTrackTimeMap = (Map<String, DayTrackTime>) args[3];
                                DayTrackTime dayTrackTime = dayTrackTimeMap.get(jrgCmsActivity.getId());

                                Talk talk = new Talk();
                                talk.setTalkDay(dayTrackTime.dayNumber());
                                talk.setStartTime(dayTrackTime.startTime());
                                talk.setTrack(dayTrackTime.trackNumber());

                                return talk;
                            }
                    );

            // Mock method under test
            JrgCmsDataLoader jrgCmsDataLoader = Mockito.mock(JrgCmsDataLoader.class);
            Mockito.when(jrgCmsDataLoader.getTalks(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyMap()))
                    .thenCallRealMethod();

            Map<String, DayTrackTime> dayTrackTimeMap = new HashMap<>();
            dayTrackTimeMap.put(id0, new DayTrackTime(2L, 1L, LocalTime.of(10, 0), LocalTime.of(10, 15)));
            dayTrackTimeMap.put(id1, new DayTrackTime(1L, 1L, LocalTime.of(10, 0), LocalTime.of(10, 15)));

            List<Talk> actual = jrgCmsDataLoader.getTalks(42, false, dayTrackTimeMap);

            assertEquals(2, actual.size());
            assertEquals(1, actual.get(0).getTalkDay());
            assertEquals(2, actual.get(1).getTalkDay());
        }
    }

    @Test
    void logNotTalkActivities() {
        // Talks
        JrgCmsTalk jrgCmsTalk1 = new JrgCmsTalk();
        jrgCmsTalk1.setTitle(Map.of(JrgCmsDataLoader.RUSSIAN_TEXT_KEY, "Наименование1"));

        JrgCmsTalk jrgCmsTalk2 = new JrgCmsTalk();
        jrgCmsTalk2.setTitle(Map.of(JrgCmsDataLoader.RUSSIAN_TEXT_KEY, "Наименование2"));

        // Activities
        JrgCmsActivity jrgCmsActivity0 = new JrgCmsActivity();
        jrgCmsActivity0.setType(JrgCmsDataLoader.TALK_ACTIVITY_TYPE);

        JrgCmsActivity jrgCmsActivity1 = new JrgCmsActivity();
        jrgCmsActivity1.setType("BOF");
        jrgCmsActivity1.setData(jrgCmsTalk1);

        JrgCmsActivity jrgCmsActivity2 = new JrgCmsActivity();
        jrgCmsActivity2.setData(jrgCmsTalk2);

        assertDoesNotThrow(() -> JrgCmsDataLoader.logNotTalkActivities(List.of(jrgCmsActivity0, jrgCmsActivity1, jrgCmsActivity2)));
    }

    @Test
    void getImageParametersTemplate() {
        assertEquals(IMAGE_PARAMETERS_TEMPLATE, new JrgCmsDataLoader().getImageParametersTemplate());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("isValidTalk method tests")
    class IsValidTalkTest {
        private Stream<Arguments> data() {
            // Options
            JrgTalkOptions jrgTalkOptions1 = new JrgTalkOptions();

            JrgTalkOptions jrgTalkOptions2 = new JrgTalkOptions();
            jrgTalkOptions2.setDemoStage(true);

            JrgTalkOptions jrgTalkOptions3 = new JrgTalkOptions();
            jrgTalkOptions3.setDemoStage(false);

            // Talks
            JrgCmsTalk jrgCmsTalk0 = new JrgCmsTalk();

            JrgCmsTalk jrgCmsTalk1 = new JrgCmsTalk();
            jrgCmsTalk1.setOptions(jrgTalkOptions1);

            JrgCmsTalk jrgCmsTalk2 = new JrgCmsTalk();
            jrgCmsTalk2.setOptions(jrgTalkOptions2);

            JrgCmsTalk jrgCmsTalk3 = new JrgCmsTalk();
            jrgCmsTalk3.setOptions(jrgTalkOptions3);

            // Activities
            JrgCmsActivity jrgCmsActivity0 = new JrgCmsActivity();
            jrgCmsActivity0.setData(jrgCmsTalk0);

            JrgCmsActivity jrgCmsActivity1 = new JrgCmsActivity();
            jrgCmsActivity1.setData(jrgCmsTalk1);

            JrgCmsActivity jrgCmsActivity2 = new JrgCmsActivity();
            jrgCmsActivity2.setData(jrgCmsTalk2);

            JrgCmsActivity jrgCmsActivity3 = new JrgCmsActivity();
            jrgCmsActivity3.setData(jrgCmsTalk3);

            return Stream.of(
                    arguments(null, false, true),
                    arguments(jrgCmsActivity0, true, true),
                    arguments(jrgCmsActivity1, true, true),
                    arguments(jrgCmsActivity2, true, false),
                    arguments(jrgCmsActivity3, true, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void isValidTalk(JrgCmsActivity activity, boolean ignoreDemoStage, boolean expected) {
            assertEquals(expected, JrgCmsDataLoader.isValidTalk(activity, ignoreDemoStage));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("isValidSpeaker method tests")
    class IsValidSpeakerTest {
        private Stream<Arguments> data() {
            // Participations
            JrgCmsParticipation jrgCmsParticipation0 = new JrgCmsParticipation();

            JrgCmsParticipation jrgCmsParticipation1 = new JrgCmsParticipation();
            jrgCmsParticipation1.setRole("");

            JrgCmsParticipation jrgCmsParticipation2 = new JrgCmsParticipation();
            jrgCmsParticipation2.setRole("HOST");

            JrgCmsParticipation jrgCmsParticipation3 = new JrgCmsParticipation();
            jrgCmsParticipation3.setRole(JrgCmsDataLoader.SPEAKER_ROLE);

            // Participants
            JrgCmsParticipant jrgCmsParticipant0 = new JrgCmsParticipant();
            jrgCmsParticipant0.setParticipation(jrgCmsParticipation0);

            JrgCmsParticipant jrgCmsParticipant1 = new JrgCmsParticipant();
            jrgCmsParticipant1.setParticipation(jrgCmsParticipation1);

            JrgCmsParticipant jrgCmsParticipant2 = new JrgCmsParticipant();
            jrgCmsParticipant2.setParticipation(jrgCmsParticipation2);

            JrgCmsParticipant jrgCmsParticipant3 = new JrgCmsParticipant();
            jrgCmsParticipant3.setParticipation(jrgCmsParticipation3);

            return Stream.of(
                    arguments(jrgCmsParticipant0, false),
                    arguments(jrgCmsParticipant1, false),
                    arguments(jrgCmsParticipant2, false),
                    arguments(jrgCmsParticipant3, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void isValidSpeaker(JrgCmsParticipant jrgCmsParticipant, boolean expected) {
            assertEquals(expected, JrgCmsDataLoader.isValidSpeaker(jrgCmsParticipant));
        }
    }

    @Test
    void getSpeakerMap() {
        final String id0 = "0";
        final String id1 = "1";
        final String id2 = "2";

        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            // Mock methods
            mockedStatic.when(() -> JrgCmsDataLoader.getSpeakerMap(Mockito.anyList()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> JrgCmsDataLoader.isValidSpeaker(Mockito.any(JrgCmsParticipant.class)))
                    .thenReturn(true);
            mockedStatic.when(() -> JrgCmsDataLoader.createSpeaker(Mockito.any(JrgCmsSpeaker.class), Mockito.any(AtomicLong.class),
                            Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                    .thenReturn(new Speaker());

            // Speakers
            JrgCmsSpeaker jrgCmsSpeaker0 = new JrgCmsSpeaker();
            jrgCmsSpeaker0.setId(id0);

            JrgCmsSpeaker jrgCmsSpeaker1 = new JrgCmsSpeaker();
            jrgCmsSpeaker1.setId(id1);

            JrgCmsSpeaker jrgCmsSpeaker2 = new JrgCmsSpeaker();
            jrgCmsSpeaker2.setId(id2);

            // Participants
            JrgCmsParticipant jrgCmsParticipant0 = new JrgCmsParticipant();
            jrgCmsParticipant0.setData(jrgCmsSpeaker0);

            JrgCmsParticipant jrgCmsParticipant1 = new JrgCmsParticipant();
            jrgCmsParticipant1.setData(jrgCmsSpeaker1);

            JrgCmsParticipant jrgCmsParticipant2 = new JrgCmsParticipant();
            jrgCmsParticipant2.setData(jrgCmsSpeaker2);

            // Activities
            JrgCmsActivity jrgCmsActivity0 = new JrgCmsActivity();
            jrgCmsActivity0.setParticipants(List.of(jrgCmsParticipant0, jrgCmsParticipant1));

            JrgCmsActivity jrgCmsActivity1 = new JrgCmsActivity();
            jrgCmsActivity1.setParticipants(List.of(jrgCmsParticipant2));

            JrgCmsActivity jrgCmsActivity2 = new JrgCmsActivity();

            Map<String, Speaker> actual = JrgCmsDataLoader.getSpeakerMap(List.of(jrgCmsActivity0, jrgCmsActivity1, jrgCmsActivity2));

            assertEquals(3, actual.size());
            assertTrue(actual.containsKey(id0));
            assertTrue(actual.containsKey(id1));
            assertTrue(actual.containsKey(id2));
        }
    }

    @Test
    void getFixedContacts() {
        final String value0 = "user";
        final String value1 = "-";
        final String value2 = "t.me/alexmeta";
        final String value3 = "igerta (тг)";

        JrgContact jrgContact0 = new JrgContact();

        JrgContact jrgContact1 = new JrgContact();
        jrgContact1.setValue(value0);

        JrgContact jrgContact2 = new JrgContact();
        jrgContact2.setValue(value1);

        JrgContact jrgContact3 = new JrgContact();
        jrgContact3.setValue(value2);

        JrgContact jrgContact4 = new JrgContact();
        jrgContact4.setValue(value3);

        List<JrgContact> jrgContacts = List.of(jrgContact0, jrgContact1, jrgContact2, jrgContact3, jrgContact4);

        assertEquals(5, jrgContacts.size());
        assertEquals(1, jrgContacts.stream()
                .filter(c -> c.getValue() == null)
                .count());
        assertEquals(1, jrgContacts.stream()
                .filter(c -> value0.equals(c.getValue()))
                .count());
        assertEquals(1, jrgContacts.stream()
                .filter(c -> value1.equals(c.getValue()))
                .count());
        assertEquals(1, jrgContacts.stream()
                .filter(c -> value2.equals(c.getValue()))
                .count());
        assertEquals(1, jrgContacts.stream()
                .filter(c -> value3.equals(c.getValue()))
                .count());

        assertEquals(Collections.emptyList(), JrgCmsDataLoader.getFixedContacts(null));

        List<JrgContact> actual = JrgCmsDataLoader.getFixedContacts(jrgContacts);

        assertEquals(5, actual.size());
        assertEquals(4, actual.stream()
                .filter(c -> c.getValue() == null)
                .count());
        assertEquals(1, actual.stream()
                .filter(c -> value0.equals(c.getValue()))
                .count());
        assertEquals(0, actual.stream()
                .filter(c -> value1.equals(c.getValue()))
                .count());
        assertEquals(0, actual.stream()
                .filter(c -> value2.equals(c.getValue()))
                .count());
        assertEquals(0, actual.stream()
                .filter(c -> value3.equals(c.getValue()))
                .count());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createSpeaker method tests")
    class CreateSpeakerTest {
        private Stream<Arguments> data() {
            // Contacts
            JrgContact jrgContact0 = new JrgContact();
            jrgContact0.setType(JrgCmsDataLoader.TWITTER_CONTACT_TYPE);
            jrgContact0.setValue("twitter0");

            JrgContact jrgContact1 = new JrgContact();
            jrgContact1.setType(JrgCmsDataLoader.GITHUB_CONTACT_TYPE);
            jrgContact1.setValue("gitHub1");

            JrgContact jrgContact2 = new JrgContact();
            jrgContact2.setType(JrgCmsDataLoader.GITHUB_CONTACT_TYPE);
            jrgContact2.setValue("gitHub2");

            // Speakers
            JrgCmsSpeaker jrgCmsSpeaker0 = new JrgCmsSpeaker();
            jrgCmsSpeaker0.setContacts(List.of(jrgContact0));

            JrgCmsSpeaker jrgCmsSpeaker1 = new JrgCmsSpeaker();
            jrgCmsSpeaker1.setCompany(Collections.emptyMap());
            jrgCmsSpeaker1.setContacts(List.of(jrgContact1, jrgContact2));

            return Stream.of(
                    arguments(jrgCmsSpeaker0, new AtomicLong(-1), new AtomicLong(-1), false),
                    arguments(jrgCmsSpeaker1, new AtomicLong(-1), new AtomicLong(-1), true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void createSpeaker(JrgCmsSpeaker jrgCmsSpeaker, AtomicLong speakerId, AtomicLong companyId, boolean checkEnTextExistence) {
            try (MockedStatic<JrgCmsDataLoader> jrgCmsDataLoaderMockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class);
                 MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
                 MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
                // Mock methods
                JrgContact jrgContact0 = new JrgContact();
                jrgContact0.setType("type");
                jrgContact0.setValue("value0");

                JrgContact jrgContact1 = new JrgContact();
                jrgContact1.setType("type");
                jrgContact1.setValue("value1");

                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.createSpeaker(Mockito.any(JrgCmsSpeaker.class),
                                Mockito.any(AtomicLong.class), Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                        .thenCallRealMethod();
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.extractPhoto(Mockito.any(JrgCmsSpeaker.class)))
                        .thenReturn(new UrlDates(null, null, null));
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.nullable(Map.class)))
                        .thenReturn(Collections.emptyList());
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.nullable(Map.class), Mockito.anyBoolean()))
                        .thenReturn(Collections.emptyList());
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.getSpeakerName(Mockito.anyList(), Mockito.anyList(), Mockito.any(Language.class)))
                        .thenReturn("");
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.getSpeakerFixedName(Mockito.nullable(String.class)))
                        .thenReturn("");
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.extractContactValue(Mockito.anyMap(), Mockito.nullable(String.class),
                                Mockito.any(UnaryOperator.class)))
                        .thenReturn("");
                cmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.getFixedContacts(Mockito.anyList()))
                        .thenReturn(List.of(jrgContact0, jrgContact1));
                localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.any(Language.class)))
                        .thenReturn("");
                cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractLocaleItems(Mockito.nullable(String.class), Mockito.nullable(String.class),
                                Mockito.anyBoolean(), Mockito.anyBoolean()))
                        .thenReturn(Collections.emptyList());
                cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.createCompanies(Mockito.nullable(String.class), Mockito.nullable(String.class),
                                Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                        .thenReturn(Collections.emptyList());

                assertNotNull(JrgCmsDataLoader.createSpeaker(jrgCmsSpeaker, speakerId, companyId, checkEnTextExistence));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createTalk method tests")
    class CreateTalkTest {
        private Stream<Arguments> data() {
            final String speakerId0 = "0";
            final String speakerId1 = "1";
            final String speakerId2 = "2";
            final String activityId0 = "42";
            final String activityId1 = "43";
            final String activityId2 = "44";
            final String activityId3 = "45";

            // Talks
            JrgCmsTalk jrgCmsTalk0 = new JrgCmsTalk();
            jrgCmsTalk0.setTitle(Collections.emptyMap());

            JrgCmsTalk jrgCmsTalk1 = new JrgCmsTalk();
            jrgCmsTalk1.setTitle(Collections.emptyMap());

            JrgCmsTalk jrgCmsTalk2 = new JrgCmsTalk();
            jrgCmsTalk2.setTitle(Collections.emptyMap());

            JrgCmsTalk jrgCmsTalk3 = new JrgCmsTalk();
            jrgCmsTalk3.setTitle(Collections.emptyMap());

            // Speakers
            JrgCmsSpeaker jrgCmsSpeaker0 = new JrgCmsSpeaker();
            jrgCmsSpeaker0.setId(speakerId0);

            JrgCmsSpeaker jrgCmsSpeaker1 = new JrgCmsSpeaker();
            jrgCmsSpeaker1.setId(speakerId1);

            JrgCmsSpeaker jrgCmsSpeaker2 = new JrgCmsSpeaker();
            jrgCmsSpeaker2.setId(speakerId2);

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);

            // Participants
            JrgCmsParticipant jrgCmsParticipant0 = new JrgCmsParticipant();
            jrgCmsParticipant0.setData(jrgCmsSpeaker0);

            JrgCmsParticipant jrgCmsParticipant1 = new JrgCmsParticipant();
            jrgCmsParticipant1.setData(jrgCmsSpeaker1);

            JrgCmsParticipant jrgCmsParticipant2 = new JrgCmsParticipant();
            jrgCmsParticipant2.setData(jrgCmsSpeaker2);

            // Activities
            JrgCmsActivity jrgCmsActivity0 = new JrgCmsActivity();
            jrgCmsActivity0.setId(activityId0);
            jrgCmsActivity0.setData(jrgCmsTalk0);
            jrgCmsActivity0.setParticipants(List.of(jrgCmsParticipant0, jrgCmsParticipant1));

            JrgCmsActivity jrgCmsActivity1 = new JrgCmsActivity();
            jrgCmsActivity1.setId(activityId1);
            jrgCmsActivity1.setData(jrgCmsTalk1);
            jrgCmsActivity1.setParticipants(List.of(jrgCmsParticipant2));

            JrgCmsActivity jrgCmsActivity2 = new JrgCmsActivity();
            jrgCmsActivity2.setId(activityId2);
            jrgCmsActivity2.setData(jrgCmsTalk2);
            jrgCmsActivity2.setParticipants(List.of(jrgCmsParticipant0));

            JrgCmsActivity jrgCmsActivity3 = new JrgCmsActivity();
            jrgCmsActivity3.setId(activityId3);
            jrgCmsActivity3.setData(jrgCmsTalk3);
            jrgCmsActivity3.setParticipants(null);

            Map<String, Speaker> speakerMap = Map.of(speakerId0, speaker0, speakerId1, speaker1);
            Map<String, DayTrackTime> dayTrackTimeMap = Map.of(activityId0, new DayTrackTime(1L, 1L, LocalTime.of(10, 0), LocalTime.of(10, 15)),
                    activityId3, new DayTrackTime(1L, 1L, LocalTime.of(10, 15), LocalTime.of(10, 30)));

            return Stream.of(
                    arguments(jrgCmsActivity0, speakerMap, new AtomicLong(-1), dayTrackTimeMap, null),
                    arguments(jrgCmsActivity1, speakerMap, new AtomicLong(-1), dayTrackTimeMap, NullPointerException.class),
                    arguments(jrgCmsActivity2, speakerMap, new AtomicLong(-1), dayTrackTimeMap, NullPointerException.class),
                    arguments(jrgCmsActivity3, speakerMap, new AtomicLong(-1), dayTrackTimeMap, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void createTalk(JrgCmsActivity jrgCmsActivity, Map<String, Speaker> speakerMap, AtomicLong talkId,
                        Map<String, DayTrackTime> dayTrackTimeMap, Class<? extends Exception> expectedException) {
            try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
                // Mock methods
                mockedStatic.when(() -> JrgCmsDataLoader.createTalk(Mockito.any(JrgCmsActivity.class), Mockito.anyMap(),
                                Mockito.any(AtomicLong.class), Mockito.anyMap()))
                        .thenCallRealMethod();
                mockedStatic.when(() -> JrgCmsDataLoader.isValidSpeaker(Mockito.any(JrgCmsParticipant.class)))
                        .thenReturn(true);
                mockedStatic.when(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.nullable(Map.class)))
                        .thenReturn(Collections.emptyList());
                mockedStatic.when(() -> JrgCmsDataLoader.extractLanguage(Mockito.nullable(String.class)))
                        .thenReturn(Language.ENGLISH.getCode());
                mockedStatic.when(() -> JrgCmsDataLoader.extractPresentationLinks(Mockito.any(JrgTalkPresentation.class)))
                        .thenReturn(Collections.emptyList());

                if (expectedException == null) {
                    assertNotNull(JrgCmsDataLoader.createTalk(jrgCmsActivity, speakerMap, talkId, dayTrackTimeMap));
                } else {
                    assertThrowsExactly(expectedException, () -> JrgCmsDataLoader.createTalk(jrgCmsActivity, speakerMap, talkId, dayTrackTimeMap));
                }
            }
        }
    }

    @Test
    void extractLocaleItems() {
        try (MockedStatic<JrgCmsDataLoader> jrgCmsDataLoaderMockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class);
             MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
            jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.anyMap(), Mockito.anyBoolean()))
                    .thenCallRealMethod();
            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractLocaleItems(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> JrgCmsDataLoader.extractLocaleItems(Map.of(), true));
            cmsDataLoaderMockedStatic.verify(() -> CmsDataLoader.extractLocaleItems(Mockito.nullable(String.class), Mockito.nullable(String.class), Mockito.anyBoolean()),
                    VerificationModeFactory.times(1));
        }
    }

    @Test
    void testExtractLocaleItems() {
        try (MockedStatic<JrgCmsDataLoader> mockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            mockedStatic.when(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.anyMap()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.anyMap(), Mockito.anyBoolean()))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> JrgCmsDataLoader.extractLocaleItems(Map.of()));
            mockedStatic.verify(() -> JrgCmsDataLoader.extractLocaleItems(Mockito.anyMap(), Mockito.anyBoolean()),
                    VerificationModeFactory.times(1));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerName method tests")
    class GetSpeakerNameTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null),
                    arguments("", null, null),
                    arguments(null, "", null),
                    arguments("", "", null),
                    arguments(null, "Last", "Last"),
                    arguments("First", null, "First"),
                    arguments("Last", "First", "First Last")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerName(String lastName, String firstName, String expected) {
            assertEquals(expected, JrgCmsDataLoader.getSpeakerName(lastName, firstName));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSpeakerName() {
        try (MockedStatic<LocalizationUtils> localizationUtilsMockedStatic = Mockito.mockStatic(LocalizationUtils.class);
             MockedStatic<JrgCmsDataLoader> jrgCmsDataLoaderMockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
            jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.getSpeakerName(Mockito.anyList(), Mockito.anyList(), Mockito.nullable(Language.class)))
                    .thenCallRealMethod();
            jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.getSpeakerName(Mockito.nullable(String.class), Mockito.nullable(String.class)))
                    .thenReturn("");

            final String firstName = "First";
            final String lastName = "Last";
            localizationUtilsMockedStatic.when(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.nullable(Language.class)))
                    .thenAnswer(
                            (Answer<String>) invocation -> {
                                Object[] args = invocation.getArguments();
                                List<LocaleItem> localeItems = (List<LocaleItem>) args[0];

                                if (localeItems.isEmpty()) {
                                    return lastName;
                                } else if (localeItems.size() == 1) {
                                    return firstName;
                                } else {
                                    return null;
                                }
                            }
                    );

            assertDoesNotThrow(() -> JrgCmsDataLoader.getSpeakerName(Collections.emptyList(), List.of(new LocaleItem()), Language.ENGLISH));
            localizationUtilsMockedStatic.verify(() -> LocalizationUtils.getString(Mockito.anyList(), Mockito.nullable(Language.class)),
                    VerificationModeFactory.times(2));
            jrgCmsDataLoaderMockedStatic.verify(() -> JrgCmsDataLoader.getSpeakerName(lastName, firstName),
                    VerificationModeFactory.times(1));
            localizationUtilsMockedStatic.verifyNoMoreInteractions();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractLanguage method tests")
    class ExtractLanguageTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null),
                    arguments("", null),
                    arguments("un", null),
                    arguments("en", "en"),
                    arguments("EN", "en"),
                    arguments("ru", "ru"),
                    arguments("RU", "ru")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractLanguage(String language, String expected) {
            assertEquals(expected, JrgCmsDataLoader.extractLanguage(language));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractPresentationLinks method tests")
    class ExtractPresentationLinksTest {
        private Stream<Arguments> data() {
            final String content0 = "https://valid.com/fileName0.pdf";
            final String content1 = "https://valid.com/fileName1.pdf";

            JrgCmsLinks jrgCmsLinks0 = new JrgCmsLinks();
            jrgCmsLinks0.setContent(content0);

            JrgCmsLinks jrgCmsLinks1 = new JrgCmsLinks();
            jrgCmsLinks1.setContent(content1);

            JrgTalkPresentationFile jrgTalkPresentationFile0 = new JrgTalkPresentationFile();
            jrgTalkPresentationFile0.setLinks(jrgCmsLinks0);

            JrgTalkPresentationFile jrgTalkPresentationFile1 = new JrgTalkPresentationFile();
            jrgTalkPresentationFile1.setLinks(jrgCmsLinks1);

            JrgTalkPresentation jrgTalkPresentation0 = new JrgTalkPresentation();

            JrgTalkPresentation jrgTalkPresentation1 = new JrgTalkPresentation();
            jrgTalkPresentation1.setFiles(List.of(jrgTalkPresentationFile0));

            JrgTalkPresentation jrgTalkPresentation2 = new JrgTalkPresentation();
            jrgTalkPresentation2.setFiles(List.of(jrgTalkPresentationFile0, jrgTalkPresentationFile1));

            return Stream.of(
                    arguments(null, Collections.emptyList()),
                    arguments(jrgTalkPresentation0, Collections.emptyList()),
                    arguments(jrgTalkPresentation1, List.of(content0)),
                    arguments(jrgTalkPresentation2, List.of(content0, content1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractPresentationLinks(JrgTalkPresentation presentation, List<String> expected) {
            assertEquals(expected, JrgCmsDataLoader.extractPresentationLinks(presentation));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractPhoto method tests")
    class ExtractPhotoTest {
        private Stream<Arguments> data() {
            final UrlDates emptyUrlDates = new UrlDates(null, null, null);
            final String content = "https://valid.com/fileName0.jpg";
            final ZonedDateTime date0 = ZonedDateTime.of(2022, 5, 20, 21, 0, 0, 0, ZoneId.of("UTC"));
            final ZonedDateTime date1 = ZonedDateTime.of(2022, 5, 20, 22, 0, 0, 0, ZoneId.of("UTC"));
            final ZonedDateTime date2 = ZonedDateTime.of(2022, 5, 20, 22, 30, 0, 0, ZoneId.of("UTC"));

            JrgCmsLinks jrgCmsLinks0 = new JrgCmsLinks();
            jrgCmsLinks0.setContent(content);

            JrgCmsPhoto jrgCmsPhoto0 = new JrgCmsPhoto();
            jrgCmsPhoto0.setLinks(jrgCmsLinks0);
            jrgCmsPhoto0.setCreated(date0);
            jrgCmsPhoto0.setLastModified(date1);

            JrgCmsPhoto jrgCmsPhoto1 = new JrgCmsPhoto();
            jrgCmsPhoto1.setLinks(jrgCmsLinks0);
            jrgCmsPhoto1.setCreated(date0);
            jrgCmsPhoto1.setLastModified(date2);

            JrgCmsSpeaker jrgCmsSpeaker0 = new JrgCmsSpeaker();
            jrgCmsSpeaker0.setLastName(Collections.emptyMap());
            jrgCmsSpeaker0.setFirstName(Collections.emptyMap());

            JrgCmsSpeaker jrgCmsSpeaker1 = new JrgCmsSpeaker();
            jrgCmsSpeaker1.setLastName(Collections.emptyMap());
            jrgCmsSpeaker1.setFirstName(Collections.emptyMap());
            jrgCmsSpeaker1.setPhoto(Collections.emptyList());

            JrgCmsSpeaker jrgCmsSpeaker2 = new JrgCmsSpeaker();
            jrgCmsSpeaker2.setLastName(Collections.emptyMap());
            jrgCmsSpeaker2.setFirstName(Collections.emptyMap());
            jrgCmsSpeaker2.setPhoto(List.of(jrgCmsPhoto0));

            JrgCmsSpeaker jrgCmsSpeaker3 = new JrgCmsSpeaker();
            jrgCmsSpeaker3.setLastName(Collections.emptyMap());
            jrgCmsSpeaker3.setFirstName(Collections.emptyMap());
            jrgCmsSpeaker3.setPhoto(List.of(jrgCmsPhoto0, jrgCmsPhoto1));

            return Stream.of(
                    arguments(jrgCmsSpeaker0, emptyUrlDates),
                    arguments(jrgCmsSpeaker1, emptyUrlDates),
                    arguments(jrgCmsSpeaker2, new UrlDates(content, date0, date1)),
                    arguments(jrgCmsSpeaker3, new UrlDates(content, date0, date1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractPhoto(JrgCmsSpeaker jrgCmsSpeaker, UrlDates expected) {
            try (MockedStatic<JrgCmsDataLoader> jrgCmsDataLoaderMockedStatic = Mockito.mockStatic(JrgCmsDataLoader.class)) {
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.extractPhoto(Mockito.any(JrgCmsSpeaker.class)))
                        .thenCallRealMethod();
                jrgCmsDataLoaderMockedStatic.when(() -> JrgCmsDataLoader.getSpeakerName(Mockito.any(), Mockito.any()))
                        .thenReturn("");

                assertEquals(expected, JrgCmsDataLoader.extractPhoto(jrgCmsSpeaker));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractContactValue method tests")
    class ExtractContactValueTest {
        private Stream<Arguments> data() {
            final String twitterContactType = "twitter";
            final String facebookContactType = "facebook";
            final String githubContactType = "github";
            final String habrContactType = "habr";
            final String unknownContactType = "unknown";

            final String twitterContactValue = "user";
            final String habrContactValue = "";

            final JrgContact jrgContact0 = new JrgContact();
            jrgContact0.setType(twitterContactType);
            jrgContact0.setValue(twitterContactValue);

            final JrgContact jrgContact1 = new JrgContact();
            jrgContact1.setType(facebookContactType);
            jrgContact1.setValue(null);

            final JrgContact jrgContact2 = new JrgContact();
            jrgContact2.setType(habrContactType);
            jrgContact2.setValue(habrContactValue);

            UnaryOperator<String> extractionOperator = v -> v;

            Map<String, JrgContact> contactMap = new HashMap<>();
            contactMap.put(twitterContactType, jrgContact0);
            contactMap.put(githubContactType, null);
            contactMap.put(facebookContactType, jrgContact1);
            contactMap.put(habrContactType, jrgContact2);

            return Stream.of(
                    arguments(contactMap, unknownContactType, null, null),
                    arguments(contactMap, githubContactType, null, null),
                    arguments(contactMap, facebookContactType, null, null),
                    arguments(contactMap, habrContactType, null, null),
                    arguments(contactMap, twitterContactType, extractionOperator, extractionOperator.apply(twitterContactValue))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractContactValue(Map<String, JrgContact> contactMap, String type, UnaryOperator<String> extractionOperator,
                                 String expected) {
            assertEquals(expected, JrgCmsDataLoader.extractContactValue(contactMap, type, extractionOperator));
        }
    }
}
