package guess.util.load.contentful;

import guess.domain.Conference;
import guess.domain.Language;
import guess.domain.source.*;
import guess.domain.source.cms.contentful.ContentfulIncludes;
import guess.domain.source.cms.contentful.ContentfulLink;
import guess.domain.source.cms.contentful.ContentfulResponse;
import guess.domain.source.cms.contentful.ContentfulSys;
import guess.domain.source.cms.contentful.asset.ContentfulAsset;
import guess.domain.source.cms.contentful.asset.ContentfulAssetFields;
import guess.domain.source.cms.contentful.asset.ContentfulAssetFieldsFile;
import guess.domain.source.cms.contentful.city.ContentfulCity;
import guess.domain.source.cms.contentful.city.ContentfulCityFields;
import guess.domain.source.cms.contentful.error.ContentfulError;
import guess.domain.source.cms.contentful.error.ContentfulErrorDetails;
import guess.domain.source.cms.contentful.event.ContentfulEvent;
import guess.domain.source.cms.contentful.event.ContentfulEventFields;
import guess.domain.source.cms.contentful.event.ContentfulEventIncludes;
import guess.domain.source.cms.contentful.event.ContentfulEventResponse;
import guess.domain.source.cms.contentful.eventtype.ContentfulEventType;
import guess.domain.source.cms.contentful.eventtype.ContentfulEventTypeFields;
import guess.domain.source.cms.contentful.eventtype.ContentfulEventTypeResponse;
import guess.domain.source.cms.contentful.locale.ContentfulLocale;
import guess.domain.source.cms.contentful.locale.ContentfulLocaleResponse;
import guess.domain.source.cms.contentful.speaker.ContentfulSpeaker;
import guess.domain.source.cms.contentful.speaker.ContentfulSpeakerFields;
import guess.domain.source.cms.contentful.speaker.ContentfulSpeakerResponse;
import guess.domain.source.cms.contentful.talk.ContentfulTalk;
import guess.domain.source.cms.contentful.talk.ContentfulTalkIncludes;
import guess.domain.source.cms.contentful.talk.fields.ContentfulTalkFields;
import guess.domain.source.cms.contentful.talk.fields.ContentfulTalkFieldsCommon;
import guess.domain.source.cms.contentful.talk.response.ContentfulTalkResponse;
import guess.domain.source.cms.contentful.talk.response.ContentfulTalkResponseCommon;
import guess.domain.source.image.UrlDates;
import guess.util.load.CmsDataLoader;
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
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static guess.util.load.contentful.ContentfulDataLoader.IMAGE_PARAMETERS_TEMPLATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("ContentfulDataLoader class tests")
class ContentfulDataLoaderTest {
    private static ContentfulTalk<ContentfulTalkFieldsCommon> createContentfulTalk(String conference, String conferences) {
        ContentfulTalkFieldsCommon contentfulTalkFields = new ContentfulTalkFieldsCommon();
        if (conference != null) {
            contentfulTalkFields.setConference(List.of(conference));
        }
        if (conferences != null) {
            contentfulTalkFields.setConferences(List.of(conferences));
        }

        ContentfulTalk<ContentfulTalkFieldsCommon> contentfulTalk = new ContentfulTalk<>();
        contentfulTalk.setFields(contentfulTalkFields);

        return contentfulTalk;
    }

    @Test
    void getRestTemplate() {
        assertNotNull(ContentfulDataLoader.getRestTemplate());
    }

    @Test
    void getTags() {
        final String code1 = "code1";
        final String code2 = "code2";
        final String code3 = "code3";
        final String code4 = "code4";

        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            ContentfulTalkResponse<ContentfulTalkFieldsCommon> response = new ContentfulTalkResponseCommon();
            response.setItems(List.of(
                    createContentfulTalk(code2, null),
                    createContentfulTalk(null, code1),
                    createContentfulTalk(code4, null),
                    createContentfulTalk(null, code3),
                    createContentfulTalk(null, code2)
            ));

            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.any()))
                    .thenReturn(response);

            mockedStatic.when(ContentfulDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);

            Map<String, List<String>> expected = Map.of(
                    ConferenceSpaceInfo.COMMON_SPACE_INFO.toString(), List.of(code1, code2, code3, code4),
                    ConferenceSpaceInfo.HOLY_JS_SPACE_INFO.toString(), List.of(code1, code2, code3, code4),
                    ConferenceSpaceInfo.DOT_NEXT_SPACE_INFO.toString(), List.of(code1, code2, code3, code4),
                    ConferenceSpaceInfo.HEISENBUG_SPACE_INFO.toString(), List.of(code1, code2, code3, code4),
                    ConferenceSpaceInfo.MOBIUS_SPACE_INFO.toString(), List.of(code1, code2, code3, code4));

            ContentfulDataLoader contentfulDataLoader = Mockito.mock(ContentfulDataLoader.class);
            Mockito.when(contentfulDataLoader.getTags(Mockito.nullable(String.class)))
                    .thenCallRealMethod();

            assertEquals(expected, contentfulDataLoader.getTags("2021"));
            assertEquals(expected, contentfulDataLoader.getTags(""));
            assertEquals(expected, contentfulDataLoader.getTags(null));
        }
    }

    @Test
    void getLocales() {
        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            mockedStatic.when(ContentfulDataLoader::getLocales)
                    .thenCallRealMethod();

            ContentfulLocale locale0 = new ContentfulLocale();
            locale0.setCode("en");

            ContentfulLocale locale1 = new ContentfulLocale();
            locale1.setCode("ru-RU");

            ContentfulLocaleResponse response = new ContentfulLocaleResponse();
            response.setItems(List.of(locale0, locale1));

            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.any()))
                    .thenReturn(response);

            mockedStatic.when(ContentfulDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);

            assertEquals(List.of("en", "ru-RU"), ContentfulDataLoader.getLocales());
        }
    }

    @Test
    void getEventTypes() {
        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            mockedStatic.when(() -> ContentfulDataLoader.createEventType(Mockito.any(ContentfulEventType.class), Mockito.any(AtomicLong.class)))
                    .thenReturn(new EventType());

            ContentfulEventTypeResponse response = new ContentfulEventTypeResponse();
            response.setItems(List.of(new ContentfulEventType(), new ContentfulEventType()));

            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.any()))
                    .thenReturn(response);

            mockedStatic.when(ContentfulDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);

            ContentfulDataLoader contentfulDataLoader = new ContentfulDataLoader();

            assertEquals(2, contentfulDataLoader.getEventTypes().size());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createEventType method tests")
    class CreateEventTypeTest {
        private Stream<Arguments> data() {
            final String vkLink = "https://vk.com";
            final String twitterLink = "https://twitter.com";
            final String facebookLink = "https://twitter.com";
            final String youtubeLink = "https://youtube.com";
            final String telegramLink = "https://telegram.org";

            ContentfulEventTypeFields contentfulEventTypeFields0 = new ContentfulEventTypeFields();
            contentfulEventTypeFields0.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name0"));
            contentfulEventTypeFields0.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields0.setSiteLink(Collections.emptyMap());

            ContentfulEventTypeFields contentfulEventTypeFields1 = new ContentfulEventTypeFields();
            contentfulEventTypeFields1.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name1"));
            contentfulEventTypeFields1.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields1.setSiteLink(Collections.emptyMap());
            contentfulEventTypeFields1.setVkLink(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, vkLink));

            ContentfulEventTypeFields contentfulEventTypeFields2 = new ContentfulEventTypeFields();
            contentfulEventTypeFields2.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name2"));
            contentfulEventTypeFields2.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields2.setSiteLink(Collections.emptyMap());
            contentfulEventTypeFields2.setTwLink(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, twitterLink));

            ContentfulEventTypeFields contentfulEventTypeFields3 = new ContentfulEventTypeFields();
            contentfulEventTypeFields3.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name3"));
            contentfulEventTypeFields3.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields3.setSiteLink(Collections.emptyMap());
            contentfulEventTypeFields3.setFbLink(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, facebookLink));

            ContentfulEventTypeFields contentfulEventTypeFields4 = new ContentfulEventTypeFields();
            contentfulEventTypeFields4.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name4"));
            contentfulEventTypeFields4.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields4.setSiteLink(Collections.emptyMap());
            contentfulEventTypeFields4.setYoutubeLink(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, youtubeLink));

            ContentfulEventTypeFields contentfulEventTypeFields5 = new ContentfulEventTypeFields();
            contentfulEventTypeFields5.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name5"));
            contentfulEventTypeFields5.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields5.setSiteLink(Collections.emptyMap());
            contentfulEventTypeFields5.setTelegramLink(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, telegramLink));

            ContentfulEventTypeFields contentfulEventTypeFields6 = new ContentfulEventTypeFields();
            contentfulEventTypeFields6.setEventName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Name6"));
            contentfulEventTypeFields6.setEventDescriptions(Collections.emptyMap());
            contentfulEventTypeFields6.setSiteLink(Collections.emptyMap());

            ContentfulEventType contentfulEventType0 = new ContentfulEventType();
            contentfulEventType0.setFields(contentfulEventTypeFields0);

            ContentfulEventType contentfulEventType1 = new ContentfulEventType();
            contentfulEventType1.setFields(contentfulEventTypeFields1);

            ContentfulEventType contentfulEventType2 = new ContentfulEventType();
            contentfulEventType2.setFields(contentfulEventTypeFields2);

            ContentfulEventType contentfulEventType3 = new ContentfulEventType();
            contentfulEventType3.setFields(contentfulEventTypeFields3);

            ContentfulEventType contentfulEventType4 = new ContentfulEventType();
            contentfulEventType4.setFields(contentfulEventTypeFields4);

            ContentfulEventType contentfulEventType5 = new ContentfulEventType();
            contentfulEventType5.setFields(contentfulEventTypeFields5);

            ContentfulEventType contentfulEventType6 = new ContentfulEventType();
            contentfulEventType6.setFields(contentfulEventTypeFields6);

            EventType eventType0 = new EventType();
            eventType0.setId(-1);

            EventType eventType1 = new EventType();
            eventType1.setId(-1);
            eventType1.setVkLink(vkLink);

            EventType eventType2 = new EventType();
            eventType2.setId(-1);
            eventType2.setTwitterLink(twitterLink);

            EventType eventType3 = new EventType();
            eventType3.setId(-1);
            eventType3.setFacebookLink(facebookLink);

            EventType eventType4 = new EventType();
            eventType4.setId(-1);
            eventType4.setYoutubeLink(youtubeLink);

            EventType eventType5 = new EventType();
            eventType5.setId(-1);
            eventType5.setTelegramChannelLink(telegramLink);

            EventType eventType6 = new EventType();
            eventType6.setId(-1);
            eventType6.setSpeakerdeckLink(null);

            EventType eventType7 = new EventType();
            eventType7.setId(-1);
            eventType7.setHabrLink(null);

            return Stream.of(
                    arguments(contentfulEventType0, new AtomicLong(-1), eventType0),
                    arguments(contentfulEventType1, new AtomicLong(-1), eventType1),
                    arguments(contentfulEventType2, new AtomicLong(-1), eventType2),
                    arguments(contentfulEventType3, new AtomicLong(-1), eventType3),
                    arguments(contentfulEventType4, new AtomicLong(-1), eventType4),
                    arguments(contentfulEventType5, new AtomicLong(-1), eventType5),
                    arguments(contentfulEventType6, new AtomicLong(-1), eventType6),
                    arguments(contentfulEventType6, new AtomicLong(-1), eventType7)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createEventType(ContentfulEventType contentfulEventType, AtomicLong id, EventType expected) {
            EventType actual = ContentfulDataLoader.createEventType(contentfulEventType, id);

            assertEquals(expected, actual);
            assertEquals(expected.getVkLink(), actual.getVkLink());
            assertEquals(expected.getTwitterLink(), actual.getTwitterLink());
            assertEquals(expected.getFacebookLink(), actual.getFacebookLink());
            assertEquals(expected.getYoutubeLink(), actual.getYoutubeLink());
            assertEquals(expected.getTelegramChannelLink(), actual.getTelegramChannelLink());
            assertEquals(expected.getSpeakerdeckLink(), actual.getSpeakerdeckLink());
            assertEquals(expected.getHabrLink(), actual.getHabrLink());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getFirstMapValue method tests")
    class GetFirstMapValueTest {
        private Stream<Arguments> data() {
            Map<String, String> map0 = Map.of("key1", "value1");

            Map<String, String> map1 = new LinkedHashMap<>();
            map1.put("key1", "value1");
            map1.put("key2", "value2");

            Map<String, String> map2 = new LinkedHashMap<>();
            map2.put("key2", "value2");
            map2.put("key1", "value1");

            return Stream.of(
                    arguments(map0, "value1"),
                    arguments(map1, "value1"),
                    arguments(map2, "value2")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getFirstMapValue(Map<String, String> map, String expected) {
            assertEquals(expected, ContentfulDataLoader.getFirstMapValue(map));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEvents method tests")
    class GetEventsTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments("JPoint", LocalDate.of(2020, 6, 29), null),
                    arguments(null, LocalDate.of(2020, 6, 29), null),
                    arguments("", LocalDate.of(2020, 6, 29), null),
                    arguments("JPoint", null, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void getEvents(String eventName, LocalDate startDate, String timeZone) {
            try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
                mockedStatic.when(() -> ContentfulDataLoader.getEvents(Mockito.nullable(String.class),
                                Mockito.nullable(LocalDate.class), Mockito.nullable(String.class)))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ContentfulDataLoader.createEvent(Mockito.any(ContentfulEvent.class), Mockito.anyMap(),
                                Mockito.anySet(), Mockito.nullable(String.class)))
                        .thenReturn(new Event());
                mockedStatic.when(() -> ContentfulDataLoader.createUtcZonedDateTime(Mockito.any(LocalDate.class),
                                Mockito.nullable(String.class)))
                        .thenReturn(ZonedDateTime.now());
                mockedStatic.when(() -> ContentfulDataLoader.getCityMap(Mockito.any(ContentfulEventResponse.class)))
                        .thenReturn(Collections.emptyMap());
                mockedStatic.when(() -> ContentfulDataLoader.getErrorSet(Mockito.any(ContentfulResponse.class), Mockito.anyString()))
                        .thenReturn(Collections.emptySet());

                ContentfulEventResponse response = new ContentfulEventResponse();
                response.setItems(List.of(new ContentfulEvent(), new ContentfulEvent()));

                RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
                Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.any()))
                        .thenReturn(response);

                mockedStatic.when(ContentfulDataLoader::getRestTemplate)
                        .thenReturn(restTemplateMock);

                assertEquals(2, ContentfulDataLoader.getEvents(eventName, startDate, timeZone).size());
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createUtcZonedDateTime method tests")
    class CreateUtcZonedDateTimeTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(LocalDate.of(2020, 1, 1), "Europe/Moscow",
                            ZonedDateTime.of(2019, 12, 31, 21, 0, 0, 0,
                                    ZoneId.of("UTC"))),
                    arguments(LocalDate.of(2020, 12, 31), "Europe/Moscow",
                            ZonedDateTime.of(2020, 12, 30, 21, 0, 0, 0,
                                    ZoneId.of("UTC"))),
                    arguments(LocalDate.of(2020, 1, 1), "UTC",
                            ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0,
                                    ZoneId.of("UTC"))),
                    arguments(LocalDate.of(2020, 12, 31), "UTC",
                            ZonedDateTime.of(2020, 12, 31, 0, 0, 0, 0,
                                    ZoneId.of("UTC")))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createUtcZonedDateTime(LocalDate localDate, String timeZone, ZonedDateTime expected) {
            assertEquals(expected, ContentfulDataLoader.createUtcZonedDateTime(localDate, timeZone));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createEvent method tests")
    class CreateEventTest {
        private Stream<Arguments> data() {
            final String youtubePlayList = "https://youtube.com";
            final String mapCoordinates = "59.762236, 30.356121";
            final String cityName0 = "City Name0";
            final String cityName1 = "City Name1";
            final String cityName2 = "City Name2";
            final String cityName3 = "City Name3";
            final String cityName4 = "City Name4";

            ContentfulSys contentfulSys0 = new ContentfulSys();
            contentfulSys0.setId("sys0");

            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("sys1");

            ContentfulSys contentfulSys2 = new ContentfulSys();
            contentfulSys2.setId("sys2");

            ContentfulSys contentfulSys3 = new ContentfulSys();
            contentfulSys3.setId("sys3");

            ContentfulSys contentfulSys4 = new ContentfulSys();
            contentfulSys4.setId("sys4");

            ContentfulLink contentfulLink0 = new ContentfulLink();
            contentfulLink0.setSys(contentfulSys0);

            ContentfulLink contentfulLink1 = new ContentfulLink();
            contentfulLink1.setSys(contentfulSys1);

            ContentfulLink contentfulLink2 = new ContentfulLink();
            contentfulLink2.setSys(contentfulSys2);

            ContentfulLink contentfulLink3 = new ContentfulLink();
            contentfulLink3.setSys(contentfulSys3);

            ContentfulLink contentfulLink4 = new ContentfulLink();
            contentfulLink4.setSys(contentfulSys4);

            ContentfulEventFields contentfulEventFields0 = new ContentfulEventFields();
            contentfulEventFields0.setConferenceName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Event Name0"));
            contentfulEventFields0.setEventStart(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, "2020-01-01T00:00+03:00"));
            contentfulEventFields0.setEventCity(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, contentfulLink0));

            ContentfulEventFields contentfulEventFields1 = new ContentfulEventFields();
            contentfulEventFields1.setConferenceName(Map.of(
                    ContentfulDataLoader.RUSSIAN_LOCALE, "Наименование события1"));
            contentfulEventFields1.setEventStart(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, "2020-01-01T00:00+03:00"));
            contentfulEventFields1.setEventCity(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, contentfulLink1));

            ContentfulEventFields contentfulEventFields2 = new ContentfulEventFields();
            contentfulEventFields2.setConferenceName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Event Name2"));
            contentfulEventFields2.setEventStart(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, "2020-01-01T00:00+03:00"));
            contentfulEventFields2.setEventEnd(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, "2020-01-02T00:00+03:00"));
            contentfulEventFields2.setEventCity(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, contentfulLink2));

            ContentfulEventFields contentfulEventFields3 = new ContentfulEventFields();
            contentfulEventFields3.setConferenceName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Event Name3"));
            contentfulEventFields3.setEventStart(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, "2020-01-01T00:00+03:00"));
            contentfulEventFields3.setEventCity(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, contentfulLink3));
            contentfulEventFields3.setYoutubePlayList(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, youtubePlayList));

            ContentfulEventFields contentfulEventFields4 = new ContentfulEventFields();
            contentfulEventFields4.setConferenceName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, "Event Name4"));
            contentfulEventFields4.setEventStart(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, "2020-01-01T00:00+03:00"));
            contentfulEventFields4.setEventCity(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, contentfulLink4));
            contentfulEventFields4.setAddressLink(Map.of(ContentfulDataLoader.ENGLISH_LOCALE, mapCoordinates));

            ContentfulEvent contentfulEvent0 = new ContentfulEvent();
            contentfulEvent0.setFields(contentfulEventFields0);

            ContentfulEvent contentfulEvent1 = new ContentfulEvent();
            contentfulEvent1.setFields(contentfulEventFields1);

            ContentfulEvent contentfulEvent2 = new ContentfulEvent();
            contentfulEvent2.setFields(contentfulEventFields2);

            ContentfulEvent contentfulEvent3 = new ContentfulEvent();
            contentfulEvent3.setFields(contentfulEventFields3);

            ContentfulEvent contentfulEvent4 = new ContentfulEvent();
            contentfulEvent4.setFields(contentfulEventFields4);

            // Events
            Event event0 = new Event();
            event0.setId(-1);
            event0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Event Name0")));
            event0.setDays(List.of(new EventDays(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2020, 1, 1),
                    new Place(
                            -1,
                            List.of(new LocaleItem(Language.ENGLISH.getCode(), cityName0)),
                            Collections.emptyList(),
                            null
                    )
            )));

            Event event1 = new Event();
            event1.setId(-1);
            event1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Наименование события1")));
            event1.setDays(List.of(new EventDays(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2020, 1, 1),
                    new Place(
                            -1,
                            List.of(new LocaleItem(Language.ENGLISH.getCode(), cityName1)),
                            Collections.emptyList(),
                            null
                    )
            )));

            Event event2 = new Event();
            event2.setId(-1);
            event2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Event Name2")));
            event2.setDays(List.of(new EventDays(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2020, 1, 2),
                    new Place(
                            -1,
                            List.of(new LocaleItem(Language.ENGLISH.getCode(), cityName2)),
                            Collections.emptyList(),
                            null
                    )
            )));

            Event event3 = new Event();
            event3.setId(-1);
            event3.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Event Name3")));
            event3.setDays(List.of(new EventDays(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2020, 1, 1),
                    new Place(
                            -1,
                            List.of(new LocaleItem(Language.ENGLISH.getCode(), cityName3)),
                            Collections.emptyList(),
                            null
                    )
            )));
            event3.setYoutubeLink(youtubePlayList);

            Event event4 = new Event();
            event4.setId(-1);
            event4.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Event Name4")));
            event4.setDays(List.of(new EventDays(
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2020, 1, 1),
                    new Place(
                            -1,
                            List.of(new LocaleItem(Language.ENGLISH.getCode(), cityName4)),
                            Collections.emptyList(),
                            mapCoordinates
                    )
            )));

            // Cities
            ContentfulCityFields contentfulCityFields0 = new ContentfulCityFields();
            contentfulCityFields0.setCityName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, cityName0));

            ContentfulCityFields contentfulCityFields1 = new ContentfulCityFields();
            contentfulCityFields1.setCityName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, cityName1));

            ContentfulCityFields contentfulCityFields2 = new ContentfulCityFields();
            contentfulCityFields2.setCityName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, cityName2));

            ContentfulCityFields contentfulCityFields3 = new ContentfulCityFields();
            contentfulCityFields3.setCityName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, cityName3));

            ContentfulCityFields contentfulCityFields4 = new ContentfulCityFields();
            contentfulCityFields4.setCityName(Map.of(
                    ContentfulDataLoader.ENGLISH_LOCALE, cityName4));

            ContentfulCity contentfulCity0 = new ContentfulCity();
            contentfulCity0.setSys(contentfulSys0);
            contentfulCity0.setFields(contentfulCityFields0);

            ContentfulCity contentfulCity1 = new ContentfulCity();
            contentfulCity1.setSys(contentfulSys1);
            contentfulCity1.setFields(contentfulCityFields1);

            ContentfulCity contentfulCity2 = new ContentfulCity();
            contentfulCity2.setSys(contentfulSys1);
            contentfulCity2.setFields(contentfulCityFields2);

            ContentfulCity contentfulCity3 = new ContentfulCity();
            contentfulCity3.setSys(contentfulSys1);
            contentfulCity3.setFields(contentfulCityFields3);

            ContentfulCity contentfulCity4 = new ContentfulCity();
            contentfulCity4.setSys(contentfulSys1);
            contentfulCity4.setFields(contentfulCityFields4);

            Map<String, ContentfulCity> cityMap = Map.of("sys0", contentfulCity0, "sys1", contentfulCity1,
                    "sys2", contentfulCity2, "sys3", contentfulCity3, "sys4", contentfulCity4);
            Set<String> entryErrorSet = Collections.emptySet();
            String timeZone = "Europe/Moscow";

            return Stream.of(
                    arguments(contentfulEvent0, cityMap, entryErrorSet, timeZone, event0),
                    arguments(contentfulEvent1, cityMap, entryErrorSet, timeZone, event1),
                    arguments(contentfulEvent2, cityMap, entryErrorSet, timeZone, event2),
                    arguments(contentfulEvent3, cityMap, entryErrorSet, timeZone, event3),
                    arguments(contentfulEvent4, cityMap, entryErrorSet, timeZone, event4)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createEvent(ContentfulEvent contentfulEvent, Map<String, ContentfulCity> cityMap, Set<String> entryErrorSet,
                         String timeZone, Event expected) {
            Event actual = ContentfulDataLoader.createEvent(contentfulEvent, cityMap, entryErrorSet, timeZone);

            assertEquals(expected, actual);
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getDays(), actual.getDays());
            assertEquals(expected.getYoutubeLink(), actual.getYoutubeLink());

            String expectedMapCoordinates =
                    (!expected.getDays().isEmpty() && (expected.getDays().get(0).getPlace()) != null) ?
                            expected.getDays().get(0).getPlace().getMapCoordinates() :
                            null;
            String actualMapCoordinates = (!actual.getDays().isEmpty() && (actual.getDays().get(0).getPlace() != null)) ?
                    actual.getDays().get(0).getPlace().getMapCoordinates() :
                    null;
            assertEquals(expectedMapCoordinates, actualMapCoordinates);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getEvent method tests")
    class GetEventTest {
        private Stream<Arguments> data() {
            Event event0 = new Event(
                    new Nameable(
                            -1L,
                            List.of(
                                    new LocaleItem(Language.ENGLISH.getCode(), "Event Name0"),
                                    new LocaleItem(Language.RUSSIAN.getCode(), "Наименование события0"))
                    ),
                    null,
                    List.of(
                            new EventDays(
                                    LocalDate.of(2016, 12, 7),
                                    LocalDate.of(2016, 12, 7),
                                    new Place()
                            )
                    ),
                    new Event.EventLinks(Collections.emptyList(), null),
                    null,
                    Collections.emptyList());

            Event event1 = new Event(
                    new Nameable(
                            -1L,
                            List.of(
                                    new LocaleItem(Language.ENGLISH.getCode(), "Event Name1"),
                                    new LocaleItem(Language.RUSSIAN.getCode(), "Наименование события1"))
                    ),
                    null,
                    List.of(
                            new EventDays(
                                    LocalDate.of(2017, 12, 7),
                                    LocalDate.of(2017, 12, 7),
                                    new Place()
                            )
                    ),
                    new Event.EventLinks(Collections.emptyList(), null),
                    null,
                    Collections.emptyList());

            return Stream.of(
                    arguments(Conference.DOT_NEXT, LocalDate.of(2016, 12, 7), null, null, null,
                            Collections.emptyList(), null, event0),
                    arguments(Conference.DOT_NEXT, LocalDate.of(2017, 12, 7), null, null, null,
                            Collections.emptyList(), IllegalStateException.class, null),
                    arguments(Conference.DOT_NEXT, LocalDate.of(2017, 12, 7), null, null, null,
                            List.of(event0, event1), IllegalStateException.class, null),
                    arguments(Conference.DOT_NEXT, LocalDate.of(2017, 12, 7), null, null, null,
                            List.of(event0), null, event0)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getEvent(Conference conference, LocalDate startDate, String conferenceCode, Event eventTemplate,
                      String timeZone, List<Event> events, Class<? extends Throwable> expectedException, Event expectedEvent) {
            try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
                mockedStatic.when(() -> ContentfulDataLoader.getEvents(Mockito.anyString(), Mockito.any(LocalDate.class),
                                Mockito.nullable(String.class)))
                        .thenReturn(events);
                mockedStatic.when(() -> ContentfulDataLoader.fixNonexistentEventError(Mockito.any(Conference.class),
                                Mockito.any(LocalDate.class)))
                        .thenAnswer(
                                (Answer<Event>) invocation -> {
                                    Object[] args = invocation.getArguments();
                                    Conference localConference = (Conference) args[0];
                                    LocalDate localStartDate = (LocalDate) args[1];

                                    if (Conference.DOT_NEXT.equals(localConference) &&
                                            LocalDate.of(2016, 12, 7).equals(localStartDate)) {
                                        return new Event(
                                                new Nameable(
                                                        -1L,
                                                        List.of(
                                                                new LocaleItem("en", "Event Name0"),
                                                                new LocaleItem("ru", "Наименование события0"))
                                                ),
                                                null,
                                                List.of(
                                                        new EventDays(
                                                                LocalDate.of(2016, 12, 7),
                                                                LocalDate.of(2016, 12, 7),
                                                                new Place()
                                                        )
                                                ),
                                                new Event.EventLinks(Collections.emptyList(), null),
                                                null,
                                                Collections.emptyList());
                                    } else {
                                        return null;
                                    }
                                }
                        );

                ContentfulDataLoader contentfulDataLoader = new ContentfulDataLoader();

                if (expectedException == null) {
                    Event event = contentfulDataLoader.getEvent(conference, startDate, conferenceCode, eventTemplate, timeZone);

                    assertEquals(expectedEvent, event);
                    assertEquals(expectedEvent.getName(), event.getName());
                    assertEquals(expectedEvent.getDays(), event.getDays());
                } else {
                    assertThrows(expectedException, () -> contentfulDataLoader.getEvent(conference, startDate,
                            conferenceCode, eventTemplate, timeZone));
                }
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSpeakersByConferenceSpaceInfo() {
        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            mockedStatic.when(() -> ContentfulDataLoader.getSpeakers(Mockito.any(ConferenceSpaceInfo.class), Mockito.nullable(String.class)))
                    .thenCallRealMethod();
            mockedStatic.when(() -> ContentfulDataLoader.createSpeaker(
                            Mockito.any(ContentfulSpeaker.class), Mockito.anyMap(), Mockito.anySet(), Mockito.any(AtomicLong.class), Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                    .thenReturn(new Speaker());
            mockedStatic.when(() -> ContentfulDataLoader.getAssetMap(Mockito.any(ContentfulResponse.class)))
                    .thenReturn(Collections.emptyMap());
            mockedStatic.when(() -> ContentfulDataLoader.getErrorSet(Mockito.any(ContentfulResponse.class), Mockito.anyString()))
                    .thenReturn(Collections.emptySet());

            ContentfulSpeakerResponse response = new ContentfulSpeakerResponse();
            response.setItems(List.of(new ContentfulSpeaker(), new ContentfulSpeaker()));

            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.any()))
                    .thenReturn(response);

            mockedStatic.when(ContentfulDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);

            assertEquals(2, ContentfulDataLoader.getSpeakers(ConferenceSpaceInfo.COMMON_SPACE_INFO, "code").size());
            assertEquals(2, ContentfulDataLoader.getSpeakers(ConferenceSpaceInfo.HOLY_JS_SPACE_INFO, "code").size());
            assertEquals(2, ContentfulDataLoader.getSpeakers(ConferenceSpaceInfo.COMMON_SPACE_INFO, null).size());
            assertEquals(2, ContentfulDataLoader.getSpeakers(ConferenceSpaceInfo.COMMON_SPACE_INFO, "").size());
        }
    }

    @Test
    void createSpeaker() {
        try (MockedStatic<ContentfulDataLoader> contentfulDataLoaderMockedStatic = Mockito.mockStatic(ContentfulDataLoader.class);
             MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.createSpeaker(
                            Mockito.any(ContentfulSpeaker.class), Mockito.anyMap(), Mockito.anySet(), Mockito.any(AtomicLong.class),
                            Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                    .thenCallRealMethod();
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.extractPhoto(Mockito.nullable(ContentfulLink.class), Mockito.anyMap(), Mockito.anySet(), Mockito.nullable(String.class)))
                    .thenReturn(new UrlDates(null, null, null));
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.extractBoolean(Mockito.nullable(Boolean.class)))
                    .thenReturn(true);
            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractTwitter(Mockito.nullable(String.class)))
                    .thenReturn(null);
            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractGitHub(Mockito.nullable(String.class)))
                    .thenReturn(null);
            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractLocaleItems(Mockito.nullable(String.class), Mockito.nullable(String.class), Mockito.anyBoolean()))
                    .thenReturn(Collections.emptyList());

            ContentfulSpeaker contentfulSpeaker = new ContentfulSpeaker();
            contentfulSpeaker.setFields(new ContentfulSpeakerFields());

            Map<String, ContentfulAsset> assetMap = Collections.emptyMap();
            Set<String> assetErrorSet = Collections.emptySet();
            AtomicLong speakerId = new AtomicLong(42);
            AtomicLong companyId = new AtomicLong(42);

            Speaker speaker = new Speaker();
            speaker.setId(42);

            assertEquals(42, ContentfulDataLoader.createSpeaker(contentfulSpeaker, assetMap, assetErrorSet, speakerId, companyId, true).getId());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractPhoto method tests")
    class ExtractPhotoTest {
        private static final String ASSET_URL = "https://valid.com";
        private final ZonedDateTime now = ZonedDateTime.now();

        private Stream<Arguments> data() {
            ContentfulSys contentfulSys0 = new ContentfulSys();
            contentfulSys0.setId("id0");

            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulSys contentfulSys2 = new ContentfulSys();
            contentfulSys2.setId("id2");

            ContentfulSys contentfulSys3 = new ContentfulSys();
            contentfulSys3.setId("id3");
            contentfulSys3.setCreatedAt(now);
            contentfulSys3.setUpdatedAt(now);

            ContentfulAssetFields contentfulAssetFields2 = new ContentfulAssetFields();
            contentfulAssetFields2.setFile(new ContentfulAssetFieldsFile());

            ContentfulAsset contentfulAsset2 = new ContentfulAsset();
            contentfulAsset2.setFields(contentfulAssetFields2);
            contentfulAsset2.setSys(contentfulSys3);

            Map<String, ContentfulAsset> assetMap2 = Map.of("id2", contentfulAsset2);

            ContentfulLink link0 = new ContentfulLink();
            link0.setSys(contentfulSys0);

            ContentfulLink link1 = new ContentfulLink();
            link1.setSys(contentfulSys1);

            ContentfulLink link2 = new ContentfulLink();
            link2.setSys(contentfulSys2);

            return Stream.of(
                    arguments(link0, Collections.emptyMap(), Set.of("id0"), "Name0", null, new UrlDates(null, null, null)),
                    arguments(link1, Collections.emptyMap(), Collections.emptySet(), "Name1", NullPointerException.class, null),
                    arguments(link2, assetMap2, Collections.emptySet(), "Name2", null, new UrlDates(ASSET_URL, now, now))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractPhoto(ContentfulLink link, Map<String, ContentfulAsset> assetMap, Set<String> assetErrorSet,
                          String speakerNameEn, Class<? extends Throwable> expectedException, UrlDates expectedValue) {
            try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
                mockedStatic.when(() -> ContentfulDataLoader.extractPhoto(Mockito.any(ContentfulLink.class), Mockito.anyMap(), Mockito.anySet(), Mockito.anyString()))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ContentfulDataLoader.extractAssetUrl(Mockito.nullable(String.class)))
                        .thenReturn(ASSET_URL);

                if (expectedException == null) {
                    assertEquals(expectedValue, ContentfulDataLoader.extractPhoto(link, assetMap, assetErrorSet, speakerNameEn));
                } else {
                    assertThrows(expectedException, () -> ContentfulDataLoader.extractPhoto(link, assetMap, assetErrorSet, speakerNameEn));
                }
            }
        }
    }

    @Test
    void getSpeakersByConference() {
        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            mockedStatic.when(() -> ContentfulDataLoader.getSpeakers(Mockito.any(Conference.class), Mockito.anyString()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> ContentfulDataLoader.getSpeakers(Mockito.any(ConferenceSpaceInfo.class), Mockito.anyString()))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> ContentfulDataLoader.getSpeakers(Conference.JPOINT, "code"));
        }
    }

    private static ContentfulTalk<ContentfulTalkFieldsCommon> createContentfulTalk(Long talkDay, LocalTime trackTime,
                                                                                   Long track, Boolean sdTrack, Boolean demoStage) {
        ContentfulTalkFieldsCommon contentfulTalkFieldsCommon = new ContentfulTalkFieldsCommon();
        contentfulTalkFieldsCommon.setTalkDay(talkDay);
        contentfulTalkFieldsCommon.setTrackTime(trackTime);
        contentfulTalkFieldsCommon.setTrack(track);
        contentfulTalkFieldsCommon.setSdTrack(sdTrack);
        contentfulTalkFieldsCommon.setDemoStage(demoStage);

        ContentfulTalk<ContentfulTalkFieldsCommon> contentfulTalk = new ContentfulTalk<>();
        contentfulTalk.setFields(contentfulTalkFieldsCommon);

        return contentfulTalk;
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTalks() {
        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            mockedStatic.when(() -> ContentfulDataLoader.getTalks(Mockito.any(ConferenceSpaceInfo.class), Mockito.nullable(String.class), Mockito.anyBoolean()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> ContentfulDataLoader.createTalk(
                            Mockito.any(ContentfulTalk.class), Mockito.anyMap(), Mockito.anySet(), Mockito.anySet(), Mockito.anyMap(), Mockito.any(AtomicLong.class)))
                    .thenReturn(new Talk());
            mockedStatic.when(() -> ContentfulDataLoader.getSpeakerMap(Mockito.any(ContentfulTalkResponse.class), Mockito.anyMap(), Mockito.anySet()))
                    .thenReturn(Collections.emptyMap());
            mockedStatic.when(() -> ContentfulDataLoader.getAssetMap(Mockito.any(ContentfulResponse.class)))
                    .thenReturn(Collections.emptyMap());
            mockedStatic.when(() -> ContentfulDataLoader.getErrorSet(Mockito.any(ContentfulResponse.class), Mockito.anyString()))
                    .thenReturn(Collections.emptySet());
            mockedStatic.when(() -> ContentfulDataLoader.isValidTalk(Mockito.any(ContentfulTalk.class), Mockito.anyBoolean()))
                    .thenCallRealMethod();

            final Long talkDay = 1L;
            final LocalTime trackTime = LocalTime.now();
            final Long track = 1L;

            ContentfulTalkResponse<ContentfulTalkFieldsCommon> response = new ContentfulTalkResponseCommon();
            response.setItems(List.of(
                    createContentfulTalk(talkDay, trackTime, track, null, null),
                    createContentfulTalk(talkDay, trackTime, track, null, Boolean.TRUE),
                    createContentfulTalk(talkDay, trackTime, track, null, Boolean.FALSE),
                    createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, null),
                    createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, Boolean.TRUE),
                    createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, Boolean.FALSE),
                    createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, null),
                    createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, Boolean.TRUE),
                    createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, Boolean.FALSE)
            ));

            RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
            Mockito.when(restTemplateMock.getForObject(Mockito.any(URI.class), Mockito.any()))
                    .thenReturn(response);

            mockedStatic.when(ContentfulDataLoader::getRestTemplate)
                    .thenReturn(restTemplateMock);

            assertEquals(4, ContentfulDataLoader.getTalks(ConferenceSpaceInfo.COMMON_SPACE_INFO, "code", true).size());
            assertEquals(4, ContentfulDataLoader.getTalks(ConferenceSpaceInfo.COMMON_SPACE_INFO, null, true).size());
            assertEquals(4, ContentfulDataLoader.getTalks(ConferenceSpaceInfo.COMMON_SPACE_INFO, "", true).size());

            assertEquals(9, ContentfulDataLoader.getTalks(ConferenceSpaceInfo.COMMON_SPACE_INFO, "code", false).size());
            assertEquals(9, ContentfulDataLoader.getTalks(ConferenceSpaceInfo.COMMON_SPACE_INFO, null, false).size());
            assertEquals(9, ContentfulDataLoader.getTalks(ConferenceSpaceInfo.COMMON_SPACE_INFO, "", false).size());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("isValidTalk method tests")
    class IsValidTalkTest {
        private Stream<Arguments> data() {
            final Long talkDay = 1L;
            final LocalTime trackTime = LocalTime.now();
            final Long track = 1L;

            return Stream.of(
                    arguments(createContentfulTalk(null, trackTime, track, null, null), false, false),
                    arguments(createContentfulTalk(null, trackTime, track, null, null), true, false),
                    arguments(createContentfulTalk(talkDay, null, track, null, null), false, false),
                    arguments(createContentfulTalk(talkDay, null, track, null, null), true, false),
                    arguments(createContentfulTalk(talkDay, trackTime, null, null, null), false, false),
                    arguments(createContentfulTalk(talkDay, trackTime, null, null, null), true, false),

                    arguments(createContentfulTalk(talkDay, trackTime, track, null, null), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, null, Boolean.TRUE), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, null, Boolean.FALSE), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, null), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, Boolean.TRUE), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, Boolean.FALSE), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, null), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, Boolean.TRUE), false, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, Boolean.FALSE), false, true),

                    arguments(createContentfulTalk(talkDay, trackTime, track, null, null), true, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, null, Boolean.TRUE), true, false),
                    arguments(createContentfulTalk(talkDay, trackTime, track, null, Boolean.FALSE), true, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, null), true, false),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, Boolean.TRUE), true, false),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.TRUE, Boolean.FALSE), true, false),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, null), true, true),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, Boolean.TRUE), true, false),
                    arguments(createContentfulTalk(talkDay, trackTime, track, Boolean.FALSE, Boolean.FALSE), true, true)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void isValidTalk(ContentfulTalk<? extends ContentfulTalkFields> talk, boolean ignoreDemoStage, boolean expected) {
            assertEquals(expected, ContentfulDataLoader.isValidTalk(talk, ignoreDemoStage));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTalk() {
        try (MockedStatic<ContentfulDataLoader> contentfulDataLoaderMockedStatic = Mockito.mockStatic(ContentfulDataLoader.class);
             MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.createTalk(
                            Mockito.any(ContentfulTalk.class), Mockito.anyMap(), Mockito.anySet(), Mockito.anySet(), Mockito.anyMap(), Mockito.any(AtomicLong.class)))
                    .thenCallRealMethod();
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.extractLanguage(Mockito.anyBoolean()))
                    .thenReturn(null);
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.extractPresentationLinks(Mockito.anyList(), Mockito.anyMap(), Mockito.anySet(), Mockito.anyString()))
                    .thenReturn(Collections.emptyList());
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.combineContentfulLinks(Mockito.anyList(), Mockito.any(ContentfulLink.class)))
                    .thenReturn(Collections.emptyList());
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.extractVideoLinks(Mockito.anyString()))
                    .thenReturn(Collections.emptyList());
            cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractLocaleItems(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
                    .thenReturn(Collections.emptyList());

            ContentfulSys contentfulSys0 = new ContentfulSys();
            contentfulSys0.setId("id0");

            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulSys contentfulSys2 = new ContentfulSys();
            contentfulSys2.setId("id2");

            ContentfulLink contentfulLink0 = new ContentfulLink();
            contentfulLink0.setSys(contentfulSys0);

            ContentfulLink contentfulLink1 = new ContentfulLink();
            contentfulLink1.setSys(contentfulSys1);

            ContentfulLink contentfulLink2 = new ContentfulLink();
            contentfulLink2.setSys(contentfulSys2);

            ContentfulTalkFieldsCommon contentfulTalkFieldsCommon0 = new ContentfulTalkFieldsCommon();
            contentfulTalkFieldsCommon0.setSpeakers(List.of(contentfulLink0));

            ContentfulTalkFieldsCommon contentfulTalkFieldsCommon1 = new ContentfulTalkFieldsCommon();
            contentfulTalkFieldsCommon1.setSpeakers(List.of(contentfulLink1));

            ContentfulTalkFieldsCommon contentfulTalkFieldsCommon2 = new ContentfulTalkFieldsCommon();
            contentfulTalkFieldsCommon2.setSpeakers(List.of(contentfulLink2));

            ContentfulTalk<ContentfulTalkFieldsCommon> contentfulTalk0 = new ContentfulTalk<>();
            contentfulTalk0.setFields(contentfulTalkFieldsCommon0);

            ContentfulTalk<ContentfulTalkFieldsCommon> contentfulTalk1 = new ContentfulTalk<>();
            contentfulTalk1.setFields(contentfulTalkFieldsCommon1);

            ContentfulTalk<ContentfulTalkFieldsCommon> contentfulTalk2 = new ContentfulTalk<>();
            contentfulTalk2.setFields(contentfulTalkFieldsCommon2);

            Map<String, ContentfulAsset> assetMap = Collections.emptyMap();
            Set<String> entryErrorSet = Set.of("id0");
            Set<String> assetErrorSet = Collections.emptySet();
            Map<String, Speaker> speakerMap = Map.of("id2", new Speaker());

            AtomicLong id0 = new AtomicLong(42);
            AtomicLong id1 = new AtomicLong(43);
            AtomicLong id2 = new AtomicLong(44);

            assertThrows(IllegalArgumentException.class, () -> ContentfulDataLoader.createTalk(contentfulTalk0, assetMap, entryErrorSet, assetErrorSet, speakerMap, id0));
            assertThrows(NullPointerException.class, () -> ContentfulDataLoader.createTalk(contentfulTalk1, assetMap, entryErrorSet, assetErrorSet, speakerMap, id1));
            assertEquals(44, ContentfulDataLoader.createTalk(contentfulTalk2, assetMap, entryErrorSet, assetErrorSet, speakerMap, id2).getId());
        }
    }

    @Test
    void testGetTalks() {
        try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            mockedStatic.when(() -> ContentfulDataLoader.getTalks(Mockito.any(ConferenceSpaceInfo.class), Mockito.anyString(), Mockito.anyBoolean()))
                    .thenReturn(Collections.emptyList());

            ContentfulDataLoader contentfulDataLoader = new ContentfulDataLoader();

            assertDoesNotThrow(() -> contentfulDataLoader.getTalks(Conference.JPOINT,
                    LocalDate.of(2022, 6, 14), "code", true, null));
            assertDoesNotThrow(() -> contentfulDataLoader.getTalks(Conference.JPOINT,
                    LocalDate.of(2022, 6, 14), "code", false, null));
        }
    }

    @Test
    void getImageParametersTemplate() {
        assertEquals(IMAGE_PARAMETERS_TEMPLATE, new ContentfulDataLoader().getImageParametersTemplate());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerMap method tests")
    class GetSpeakerMapTest {
        private final Speaker speaker;

        public GetSpeakerMapTest() {
            speaker = new Speaker();
            speaker.setId(42);
        }

        private Stream<Arguments> data() {
            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulSpeaker contentfulSpeaker1 = new ContentfulSpeaker();
            contentfulSpeaker1.setSys(contentfulSys1);

            ContentfulTalkIncludes contentfulTalkIncludes1 = new ContentfulTalkIncludes();
            contentfulTalkIncludes1.setEntry(List.of(contentfulSpeaker1));

            ContentfulTalkResponseCommon contentfulTalkResponseCommon0 = new ContentfulTalkResponseCommon();

            ContentfulTalkResponseCommon contentfulTalkResponseCommon1 = new ContentfulTalkResponseCommon();
            contentfulTalkResponseCommon1.setIncludes(contentfulTalkIncludes1);

            return Stream.of(
                    arguments(contentfulTalkResponseCommon0, null, null, Collections.emptyMap()),
                    arguments(contentfulTalkResponseCommon1, null, null, Map.of("id1", speaker))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void getSpeakerMap(ContentfulTalkResponse<? extends ContentfulTalkFields> response,
                           Map<String, ContentfulAsset> assetMap, Set<String> assetErrorSet,
                           Map<String, Speaker> expected) {
            try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
                mockedStatic.when(() -> ContentfulDataLoader.getSpeakerMap(Mockito.any(ContentfulTalkResponse.class), Mockito.nullable(Map.class), Mockito.nullable(Set.class)))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ContentfulDataLoader.createSpeaker(
                                Mockito.any(ContentfulSpeaker.class), Mockito.nullable(Map.class), Mockito.nullable(Set.class), Mockito.any(AtomicLong.class), Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                        .thenReturn(speaker);

                assertEquals(expected, ContentfulDataLoader.getSpeakerMap(response, assetMap, assetErrorSet));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getAssetMap method tests")
    class GetAssetMapTest {
        private Stream<Arguments> data() {
            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulAsset contentfulAsset1 = new ContentfulAsset();
            contentfulAsset1.setSys(contentfulSys1);

            ContentfulIncludes contentfulIncludes1 = new ContentfulIncludes();
            contentfulIncludes1.setAsset(List.of(contentfulAsset1));

            ContentfulSpeakerResponse response0 = new ContentfulSpeakerResponse();

            ContentfulSpeakerResponse response1 = new ContentfulSpeakerResponse();
            response1.setIncludes(contentfulIncludes1);

            return Stream.of(
                    arguments(response0, Collections.emptyMap()),
                    arguments(response1, Map.of("id1", contentfulAsset1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getAssetMap(ContentfulResponse<?, ? extends ContentfulIncludes> response,
                         Map<String, ContentfulAsset> expected) {
            assertEquals(expected, ContentfulDataLoader.getAssetMap(response));
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getCityMap method tests")
    class GetCityMapTest {
        private Stream<Arguments> data() {
            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulCity contentfulCity1 = new ContentfulCity();
            contentfulCity1.setSys(contentfulSys1);

            ContentfulEventIncludes contentfulIncludes1 = new ContentfulEventIncludes();
            contentfulIncludes1.setEntry(List.of(contentfulCity1));

            ContentfulEventResponse response0 = new ContentfulEventResponse();

            ContentfulEventResponse response1 = new ContentfulEventResponse();
            response1.setIncludes(contentfulIncludes1);

            return Stream.of(
                    arguments(response0, Collections.emptyMap()),
                    arguments(response1, Map.of("id1", contentfulCity1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getCityMap(ContentfulEventResponse response, Map<String, ContentfulCity> expected) {
            assertEquals(expected, ContentfulDataLoader.getCityMap(response));
        }
    }

    private static ContentfulError createContentfulError(boolean isSysNotNull, boolean isDetailsNotNull, String sysId,
                                                         String sysType, String detailsType, String detailsLinkType, String detailsId) {
        ContentfulError contentfulError = new ContentfulError();

        if (isSysNotNull) {
            ContentfulSys sys = new ContentfulSys();
            sys.setId(sysId);
            sys.setType(sysType);

            contentfulError.setSys(sys);
        }

        if (isDetailsNotNull) {
            ContentfulErrorDetails details = new ContentfulErrorDetails();
            details.setType(detailsType);
            details.setLinkType(detailsLinkType);
            details.setId(detailsId);

            contentfulError.setDetails(details);
        }

        return contentfulError;
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getErrorSet method tests")
    class GetErrorSetTest {
        private Stream<Arguments> data() {
            ContentfulEventResponse response0 = new ContentfulEventResponse();

            ContentfulEventResponse response1 = new ContentfulEventResponse();
            response1.setErrors(List.of(
                    createContentfulError(false, false, null, null, null, null, "id0"),
                    createContentfulError(true, false, null, null, null, null, "id1"),
                    createContentfulError(true, false, "notResolvable", null, null, null, "id2"),
                    createContentfulError(true, false, null, "error", null, null, "id3"),
                    createContentfulError(true, false, "notResolvable", "error", null, null, "id4"),
                    createContentfulError(false, true, null, null, null, null, "id5"),
                    createContentfulError(false, true, null, null, "Link", null, "id6"),
                    createContentfulError(false, true, null, null, null, ContentfulDataLoader.ENTRY_LINK_TYPE, "id7"),
                    createContentfulError(false, true, null, null, "Link", ContentfulDataLoader.ENTRY_LINK_TYPE, "id8"),
                    createContentfulError(true, true, null, null, null, null, "id10"),
                    createContentfulError(true, true, null, null, null, ContentfulDataLoader.ENTRY_LINK_TYPE, "id10"),
                    createContentfulError(true, true, null, null, "Link", null, "id11"),
                    createContentfulError(true, true, null, null, "Link", ContentfulDataLoader.ENTRY_LINK_TYPE, "id12"),
                    createContentfulError(true, true, null, "error", null, null, "id13"),
                    createContentfulError(true, true, null, "error", null, ContentfulDataLoader.ENTRY_LINK_TYPE, "id14"),
                    createContentfulError(true, true, null, "error", "Link", null, "id15"),
                    createContentfulError(true, true, null, "error", "Link", ContentfulDataLoader.ENTRY_LINK_TYPE, "id16"),
                    createContentfulError(true, true, "notResolvable", null, null, null, "id17"),
                    createContentfulError(true, true, "notResolvable", null, null, ContentfulDataLoader.ENTRY_LINK_TYPE, "id18"),
                    createContentfulError(true, true, "notResolvable", null, "Link", null, "id19"),
                    createContentfulError(true, true, "notResolvable", null, "Link", ContentfulDataLoader.ENTRY_LINK_TYPE, "id20"),
                    createContentfulError(true, true, "notResolvable", "error", null, null, "id21"),
                    createContentfulError(true, true, "notResolvable", "error", null, ContentfulDataLoader.ENTRY_LINK_TYPE, "id22"),
                    createContentfulError(true, true, "notResolvable", "error", "Link", null, "id23"),
                    createContentfulError(true, true, "notResolvable", "error", "Link", ContentfulDataLoader.ENTRY_LINK_TYPE, "id24")
            ));

            return Stream.of(
                    arguments(response0, null, Collections.emptySet()),
                    arguments(response1, "", Collections.emptySet()),
                    arguments(response1, ContentfulDataLoader.ENTRY_LINK_TYPE, Set.of("id24"))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getErrorSet(ContentfulResponse<?, ? extends ContentfulIncludes> response, String linkType, Set<String> expected) {
            assertEquals(expected, ContentfulDataLoader.getErrorSet(response, linkType));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractBoolean method tests")
    class ExtractBooleanTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, false),
                    arguments(Boolean.TRUE, true),
                    arguments(Boolean.FALSE, false)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractBoolean(Boolean value, boolean expected) {
            assertEquals(expected, ContentfulDataLoader.extractBoolean(value));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractLanguage method tests")
    class ExtractLanguageTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null),
                    arguments(Boolean.TRUE, Language.RUSSIAN.getCode()),
                    arguments(Boolean.FALSE, Language.ENGLISH.getCode())
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractLanguage(Boolean value, String expected) {
            assertEquals(expected, ContentfulDataLoader.extractLanguage(value));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("combineContentfulLinks method tests")
    class CombineContentfulLinksTest {
        private Stream<Arguments> data() {
            ContentfulSys contentfulSys0 = new ContentfulSys();
            contentfulSys0.setId("a");
            ContentfulLink contentfulLink0 = new ContentfulLink();
            contentfulLink0.setSys(contentfulSys0);

            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("b");
            ContentfulLink contentfulLink1 = new ContentfulLink();
            contentfulLink1.setSys(contentfulSys1);

            ContentfulSys contentfulSys2 = new ContentfulSys();
            contentfulSys2.setId("c");
            ContentfulLink contentfulLink2 = new ContentfulLink();
            contentfulLink2.setSys(contentfulSys2);

            return Stream.of(
                    arguments(null, null, Collections.emptyList()),
                    arguments(Collections.emptyList(), null, Collections.emptyList()),
                    arguments(List.of(contentfulLink0), null, List.of(contentfulLink0)),
                    arguments(List.of(contentfulLink0, contentfulLink1), null, List.of(contentfulLink0, contentfulLink1)),
                    arguments(List.of(contentfulLink0), contentfulLink1, List.of(contentfulLink0, contentfulLink1)),
                    arguments(List.of(contentfulLink0), contentfulLink0, List.of(contentfulLink0)),
                    arguments(List.of(contentfulLink0, contentfulLink1), contentfulLink2, List.of(contentfulLink0, contentfulLink1, contentfulLink2)),
                    arguments(List.of(contentfulLink0, contentfulLink0), contentfulLink0, List.of(contentfulLink0))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void combineContentfulLinks(List<ContentfulLink> presentations, ContentfulLink presentation, List<ContentfulLink> expected) {
            assertEquals(expected, ContentfulDataLoader.combineContentfulLinks(presentations, presentation));
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractPresentationLinks method tests")
    class ExtractPresentationLinksTest {
        private static final String ASSET_URL = "https://valid.com";

        private Stream<Arguments> data() {
            ContentfulSys contentfulSys0 = new ContentfulSys();
            contentfulSys0.setId("id0");

            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulSys contentfulSys2 = new ContentfulSys();
            contentfulSys2.setId("id2");

            ContentfulLink contentfulLink0 = new ContentfulLink();
            contentfulLink0.setSys(contentfulSys0);

            ContentfulLink contentfulLink1 = new ContentfulLink();
            contentfulLink1.setSys(contentfulSys1);

            ContentfulLink contentfulLink2 = new ContentfulLink();
            contentfulLink2.setSys(contentfulSys2);

            ContentfulAssetFields contentfulAssetFields1 = new ContentfulAssetFields();
            contentfulAssetFields1.setFile(new ContentfulAssetFieldsFile());

            ContentfulAsset contentfulAsset1 = new ContentfulAsset();
            contentfulAsset1.setFields(contentfulAssetFields1);

            Map<String, ContentfulAsset> assetMap = Map.of("id1", contentfulAsset1);
            Set<String> assetErrorSet = Set.of("id0");

            return Stream.of(
                    arguments(null, null, null, null, null, Collections.emptyList()),
                    arguments(List.of(contentfulLink0, contentfulLink1), assetMap, assetErrorSet, "talkNameEn", null, List.of(ASSET_URL)),
                    arguments(List.of(contentfulLink2), assetMap, assetErrorSet, "talkNameEn", NullPointerException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        @SuppressWarnings("unchecked")
        void extractPresentationLinks(List<ContentfulLink> links, Map<String, ContentfulAsset> assetMap,
                                      Set<String> assetErrorSet, String talkNameEn, Class<? extends Throwable> expectedException,
                                      List<String> expectedValue) {
            try (MockedStatic<ContentfulDataLoader> mockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
                mockedStatic.when(() -> ContentfulDataLoader.extractPresentationLinks(Mockito.nullable(List.class), Mockito.nullable(Map.class), Mockito.nullable(Set.class), Mockito.nullable(String.class)))
                        .thenCallRealMethod();
                mockedStatic.when(() -> ContentfulDataLoader.extractAssetUrl(Mockito.nullable(String.class)))
                        .thenReturn(ASSET_URL);

                if (expectedException == null) {
                    assertEquals(expectedValue, ContentfulDataLoader.extractPresentationLinks(links, assetMap, assetErrorSet, talkNameEn));
                } else {
                    assertThrows(expectedException, () -> ContentfulDataLoader.extractPresentationLinks(links, assetMap, assetErrorSet, talkNameEn));
                }
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractMaterialLinks method tests")
    class ExtractMaterialLinksTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null),
                    arguments("https://valid.com", List.of("https://valid.com"))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractMaterialLinks(String material, List<String> expected) {
            assertEquals(expected, ContentfulDataLoader.extractMaterialLinks(material));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractVideoLinks method tests")
    class ExtractVideoLinksTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, Collections.emptyList()),
                    arguments("value", List.of("value"))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractVideoLinks(String videoLink, List<String> expected) {
            assertEquals(expected, ContentfulDataLoader.extractVideoLinks(videoLink));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractAssetUrl method tests")
    class ExtractAssetUrlTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null),
                    arguments("", null, ""),
                    arguments(" ", null, ""),
                    arguments("//assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf",
                            null, "https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf"),
                    arguments(" //assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf",
                            null, "https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf"),
                    arguments("//assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf ",
                            null, "https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf"),
                    arguments(" //assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf ",
                            null, "https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf"),
                    arguments("http://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf",
                            null, "https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf"),
                    arguments("https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf",
                            null, "https://assets.ctfassets.net/oxjq45e8ilak/6sKzieda7fGIQrNXZsR0cZ/bf48435803b5cac81cb4e3c729a581d6/2019_Azul_HTM.pdf"),
                    arguments("abc", IllegalArgumentException.class, null),
                    arguments("42", IllegalArgumentException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractAssetUrl(String value, Class<? extends Throwable> expectedException, String expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, ContentfulDataLoader.extractAssetUrl(value));
            } else {
                assertThrows(expectedException, () -> ContentfulDataLoader.extractAssetUrl(value));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractEventName method tests")
    class ExtractEventNameTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null, null),
                    arguments(null, "", null, null),
                    arguments(null, "abc", null, null),
                    arguments("abc", "en", null, "abc"),
                    arguments("Moscow", "en", null, " Msc"),
                    arguments("Moscow ", "en", null, " Msc"),
                    arguments(" Moscow ", "en", null, " Msc"),
                    arguments("abc Moscow", "en", null, "abc Msc"),
                    arguments("abc Moscow ", "en", null, "abc Msc"),
                    arguments("Moscow cde", "en", null, "Moscow cde"),
                    arguments(" Moscow cde", "en", null, " Moscow cde"),
                    arguments("abc Moscow cde", "en", null, "abc Moscow cde"),
                    arguments("Piter", "en", null, " SPb"),
                    arguments("Piter ", "en", null, " SPb"),
                    arguments(" Piter ", "en", null, " SPb"),
                    arguments("abc Piter", "en", null, "abc SPb"),
                    arguments("abc Piter ", "en", null, "abc SPb"),
                    arguments("Piter cde", "en", null, "Piter cde"),
                    arguments(" Piter cde", "en", null, " Piter cde"),
                    arguments("abc Piter cde", "en", null, "abc Piter cde"),
                    arguments("Moscow", "ru-RU", null, " Мск"),
                    arguments("Moscow ", "ru-RU", null, " Мск"),
                    arguments(" Moscow ", "ru-RU", null, " Мск"),
                    arguments("abc Moscow", "ru-RU", null, "abc Мск"),
                    arguments("abc Moscow ", "ru-RU", null, "abc Мск"),
                    arguments("Moscow cde", "ru-RU", null, "Moscow cde"),
                    arguments(" Moscow cde", "ru-RU", null, " Moscow cde"),
                    arguments("abc Moscow cde", "ru-RU", null, "abc Moscow cde"),
                    arguments("Piter", "ru-RU", null, " СПб"),
                    arguments("Piter ", "ru-RU", null, " СПб"),
                    arguments(" Piter ", "ru-RU", null, " СПб"),
                    arguments("abc Piter", "ru-RU", null, "abc СПб"),
                    arguments("abc Piter ", "ru-RU", null, "abc СПб"),
                    arguments("Piter cde", "ru-RU", null, "Piter cde"),
                    arguments(" Piter cde", "ru-RU", null, " Piter cde"),
                    arguments("abc Piter cde", "ru-RU", null, "abc Piter cde"),
                    arguments("abc", "", IllegalArgumentException.class, null),
                    arguments("abc", "unknown", IllegalArgumentException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractEventName(String name, String locale, Class<? extends Throwable> expectedException, String expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, ContentfulDataLoader.extractEventName(name, locale));
            } else {
                assertThrows(expectedException, () -> ContentfulDataLoader.extractEventName(name, locale));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractLocaleValue method tests")
    class ExtractLocaleValueTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null),
                    arguments(Map.of("en", "value"), "en", "value"),
                    arguments(Map.of("en", "value"), "ru", null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractLocaleValue(Map<String, String> map, String locale, String expected) {
            assertEquals(expected, ContentfulDataLoader.extractLocaleValue(map, locale));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractCity method tests")
    class ExtractCityTest {
        private Stream<Arguments> data() {
            ContentfulSys contentfulSys0 = new ContentfulSys();
            contentfulSys0.setId("id0");

            ContentfulSys contentfulSys1 = new ContentfulSys();
            contentfulSys1.setId("id1");

            ContentfulSys contentfulSys2 = new ContentfulSys();
            contentfulSys2.setId("id2");

            ContentfulLink contentfulLink0 = new ContentfulLink();
            contentfulLink0.setSys(contentfulSys0);

            ContentfulLink contentfulLink1 = new ContentfulLink();
            contentfulLink1.setSys(contentfulSys1);

            ContentfulLink contentfulLink2 = new ContentfulLink();
            contentfulLink2.setSys(contentfulSys2);

            ContentfulCityFields contentfulCityFields1 = new ContentfulCityFields();
            contentfulCityFields1.setCityName(Map.of("en", "Name1"));

            ContentfulCity contentfulCity1 = new ContentfulCity();
            contentfulCity1.setFields(contentfulCityFields1);

            Map<String, ContentfulCity> cityMap = Map.of("id1", contentfulCity1);
            Set<String> entryErrorSet = Set.of("id0");

            return Stream.of(
                    arguments(contentfulLink0, cityMap, entryErrorSet, null, "eventName", IllegalArgumentException.class, null),
                    arguments(contentfulLink1, cityMap, entryErrorSet, "en", "eventName", null, "Name1"),
                    arguments(contentfulLink2, cityMap, entryErrorSet, null, "eventName", NullPointerException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractCity(ContentfulLink link, Map<String, ContentfulCity> cityMap, Set<String> entryErrorSet, String locale,
                         String eventName, Class<? extends Throwable> expectedException, String expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, ContentfulDataLoader.extractCity(link, cityMap, entryErrorSet, locale, eventName));
            } else {
                assertThrows(expectedException, () -> ContentfulDataLoader.extractCity(link, cityMap, entryErrorSet, locale, eventName));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fixNonexistentEventError method tests")
    class FixNonexistentEventErrorTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null),
                    arguments(null, LocalDate.of(2016, 12, 7), null),
                    arguments(Conference.DOT_NEXT, null, null),
                    arguments(Conference.JPOINT, LocalDate.of(2016, 12, 7), null),
                    arguments(Conference.DOT_NEXT, LocalDate.of(2016, 12, 8), null),
                    arguments(Conference.DOT_NEXT, LocalDate.of(2016, 12, 7), new Event(
                            new Nameable(
                                    -1L,
                                    List.of(
                                            new LocaleItem("en", "DotNext 2016 Helsinki"),
                                            new LocaleItem("ru", "DotNext 2016 Хельсинки"))
                            ),
                            null,
                            List.of(
                                    new EventDays(
                                            LocalDate.of(2016, 12, 7),
                                            LocalDate.of(2016, 12, 7),
                                            new Place(
                                                    15,
                                                    List.of(
                                                            new LocaleItem("en", "Helsinki"),
                                                            new LocaleItem("ru", "Хельсинки")),
                                                    List.of(
                                                            new LocaleItem("en", "Microsoft Talo, Keilalahdentie 2-4, 02150 Espoo")),
                                                    "60.1704769, 24.8279349")
                                    )
                            ),
                            new Event.EventLinks(
                                    List.of(
                                            new LocaleItem("en", "https://dotnext-helsinki.com"),
                                            new LocaleItem("ru", "https://dotnext-helsinki.com")),
                                    "https://www.youtube.com/playlist?list=PLtWrKx3nUGBcaA5j9UT6XMnoGM6a2iCE5"
                            ),
                            "Europe/Helsinki",
                            Collections.emptyList()))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fixNonexistentEventError(Conference conference, LocalDate startDate, Event expected) {
            Event event = ContentfulDataLoader.fixNonexistentEventError(conference, startDate);

            assertEquals(expected, event);

            if ((expected != null) && (event != null)) {
                assertEquals(expected.getName(), event.getName());
                assertEquals(expected.getDays(), event.getDays());
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("fixEntryNotResolvableError method tests")
    class FixEntryNotResolvableErrorTest {
        private Stream<Arguments> createStream(ConferenceSpaceInfo existingConferenceSpaceInfo, String existingEntryId) {
            Speaker speaker0 = new Speaker();
            speaker0.setId(0);

            Speaker speaker1 = new Speaker();
            speaker1.setId(-1);

            return Stream.of(
                    arguments(
                            null,
                            new HashSet<>(),
                            new HashMap<>(),
                            Collections.emptySet(),
                            Collections.emptyMap()),
                    arguments(
                            null,
                            new HashSet<>(),
                            new HashMap<>(Map.of(existingEntryId, speaker0)),
                            Collections.emptySet(),
                            Map.of(existingEntryId, speaker0)),
                    arguments(
                            null,
                            new HashSet<>(Set.of(existingEntryId)),
                            new HashMap<>(),
                            Set.of(existingEntryId),
                            Collections.emptyMap()),
                    arguments(
                            null,
                            new HashSet<>(Set.of(existingEntryId)),
                            new HashMap<>(Map.of(existingEntryId, speaker0)),
                            Set.of(existingEntryId),
                            Map.of(existingEntryId, speaker0)),
                    arguments(
                            existingConferenceSpaceInfo,
                            new HashSet<>(),
                            new HashMap<>(),
                            Collections.emptySet(),
                            Collections.emptyMap()),
                    arguments(
                            existingConferenceSpaceInfo,
                            new HashSet<>(),
                            new HashMap<>(Map.of(existingEntryId, speaker0)),
                            Collections.emptySet(),
                            Map.of(existingEntryId, speaker0)),
                    arguments(
                            existingConferenceSpaceInfo,
                            new HashSet<>(Set.of(existingEntryId)),
                            new HashMap<>(),
                            Collections.emptySet(),
                            Map.of(existingEntryId, speaker1)),
                    arguments(
                            existingConferenceSpaceInfo,
                            new HashSet<>(Set.of(existingEntryId)),
                            new HashMap<>(Map.of(existingEntryId, speaker0)),
                            Set.of(existingEntryId),
                            Map.of(existingEntryId, speaker0))
            );
        }

        private Stream<Arguments> data() {
            return Stream.concat(
                    Stream.concat(
                            Stream.concat(
                                    Stream.concat(
                                            Stream.concat(
                                                    Stream.concat(
                                                            createStream(ConferenceSpaceInfo.COMMON_SPACE_INFO, "6yIC7EpG1EhejCEJDEsuqA"),
                                                            createStream(ConferenceSpaceInfo.COMMON_SPACE_INFO, "2i2OfmHelyMCiK2sCUoGsS")
                                                    ),
                                                    createStream(ConferenceSpaceInfo.COMMON_SPACE_INFO, "1FDbCMYfsEkiQG6s8CWQwS")
                                            ),
                                            createStream(ConferenceSpaceInfo.COMMON_SPACE_INFO, "MPZSTxFNbbjBdf5M5uoOZ")
                                    ),
                                    createStream(ConferenceSpaceInfo.HOLY_JS_SPACE_INFO, "3YSoYRePW0OIeaAAkaweE6")
                            ),
                            createStream(ConferenceSpaceInfo.HOLY_JS_SPACE_INFO, "2UddvLNyXmy4YaukAuE4Ao")
                    ),
                    createStream(ConferenceSpaceInfo.MOBIUS_SPACE_INFO, "33qzWXnXYsgyCsSiwK0EOy")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void fixEntryNotResolvableError(ConferenceSpaceInfo conferenceSpaceInfo,
                                        Set<String> entryErrorSet, Map<String, Speaker> speakerMap,
                                        Set<String> expectedEntryErrorSet, Map<String, Speaker> expectedSpeakerMap) {
            assertDoesNotThrow(() -> ContentfulDataLoader.fixEntryNotResolvableError(conferenceSpaceInfo, entryErrorSet, speakerMap));
            assertEquals(expectedEntryErrorSet, entryErrorSet);
            assertEquals(expectedSpeakerMap, speakerMap);
        }
    }

    @Test
    void iterateAllEntities() {
        try (MockedStatic<ContentfulDataLoader> contentfulDataLoaderMockedStatic = Mockito.mockStatic(ContentfulDataLoader.class)) {
            contentfulDataLoaderMockedStatic.when(ContentfulDataLoader::getLocales)
                    .thenReturn(Collections.emptyList());
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.getEvents(Mockito.anyString(),
                            Mockito.any(LocalDate.class), Mockito.anyString()))
                    .thenReturn(Collections.emptyList());
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.getSpeakers(
                            Mockito.any(ConferenceSpaceInfo.class), Mockito.anyString()))
                    .thenReturn(Collections.emptyList());
            contentfulDataLoaderMockedStatic.when(() -> ContentfulDataLoader.getTalks(
                            Mockito.any(ConferenceSpaceInfo.class), Mockito.anyString(), Mockito.anyBoolean()))
                    .thenReturn(Collections.emptyList());

            ContentfulDataLoader contentfulDataLoader = Mockito.mock(ContentfulDataLoader.class);
            Mockito.doCallRealMethod().when(contentfulDataLoader).iterateAllEntities();
            Mockito.when(contentfulDataLoader.getEventTypes()).thenReturn(Collections.emptyList());

            assertDoesNotThrow(contentfulDataLoader::iterateAllEntities);
        }
    }
}
