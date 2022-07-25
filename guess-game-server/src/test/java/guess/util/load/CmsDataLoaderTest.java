package guess.util.load;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.source.LocaleItem;
import guess.domain.source.extract.ExtractPair;
import guess.domain.source.extract.ExtractSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("CmsDataLoader class tests")
class CmsDataLoaderTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractString method tests")
    class ExtractStringTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, false, null),
                    arguments(null, true, null),
                    arguments("", false, ""),
                    arguments("", true, ""),
                    arguments(" value0", false, "value0"),
                    arguments(" value0", true, "value0"),
                    arguments("value1 ", false, "value1"),
                    arguments("value1 ", true, "value1"),
                    arguments(" value2 ", false, "value2"),
                    arguments(" value2 ", true, "value2"),
                    arguments("value3  value4", false, "value3  value4"),
                    arguments("value3  value4", true, "value3 value4")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractBoolean(String value, boolean removeDuplicateWhiteSpaces, String expected) {
            assertEquals(expected, CmsDataLoader.extractString(value, removeDuplicateWhiteSpaces));
        }
    }

    @Test
    void extractString() {
        final String SOURCE = "source";

        try (MockedStatic<CmsDataLoader> mockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
            mockedStatic.when(() -> CmsDataLoader.extractString(Mockito.anyString()))
                    .thenCallRealMethod();
            mockedStatic.when(() -> CmsDataLoader.extractString(Mockito.anyString(), Mockito.anyBoolean()))
                    .thenReturn("42");

            CmsDataLoader.extractString(SOURCE);

            mockedStatic.verify(() -> CmsDataLoader.extractString(SOURCE), VerificationModeFactory.times(1));
            mockedStatic.verify(() -> CmsDataLoader.extractString(SOURCE, false), VerificationModeFactory.times(1));
            mockedStatic.verifyNoMoreInteractions();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractLocaleItems method tests")
    class ExtractLocaleItemsTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, true, false, Collections.emptyList()),
                    arguments(null, "", true, false, Collections.emptyList()),
                    arguments("", null, true, false, Collections.emptyList()),
                    arguments("", "", true, false, Collections.emptyList()),
                    arguments("value0", null, true, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"))),
                    arguments("value0", "", true, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"))),
                    arguments("value0", "value0", true, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"))),
                    arguments("value0", "value1", true, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"),
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1"))),
                    arguments(null, "value1", true, false, List.of(
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1"))),
                    arguments("", "value1", true, false, List.of(
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1"))),
                    arguments(null, null, false, false, Collections.emptyList()),
                    arguments(null, "", false, false, Collections.emptyList()),
                    arguments("", null, false, false, Collections.emptyList()),
                    arguments("", "", false, false, Collections.emptyList()),
                    arguments("value0", null, false, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"))),
                    arguments("value0", "", false, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"))),
                    arguments("value0", "value0", false, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"))),
                    arguments("value0", "value1", false, false, List.of(
                            new LocaleItem(
                                    Language.ENGLISH.getCode(),
                                    "value0"),
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1"))),
                    arguments(null, "value1", false, false, List.of(
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1"))),
                    arguments("", "value1", false, false, List.of(
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1"))),
                    arguments("", "value1  value2", false, false, List.of(
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1  value2"))),
                    arguments("", "value1  value2", false, true, List.of(
                            new LocaleItem(
                                    Language.RUSSIAN.getCode(),
                                    "value1 value2")))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractLocaleItems(String enText, String ruText, boolean checkEnTextExistence, boolean removeDuplicateWhiteSpaces,
                                List<LocaleItem> expected) {
            assertEquals(expected, CmsDataLoader.extractLocaleItems(enText, ruText, checkEnTextExistence, removeDuplicateWhiteSpaces));

            if (!removeDuplicateWhiteSpaces) {
                assertEquals(expected, CmsDataLoader.extractLocaleItems(enText, ruText, checkEnTextExistence));
                assertEquals(expected, CmsDataLoader.extractLocaleItems(enText, ruText));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerFixedName method tests")
    class GetSpeakerFixedNameTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null),
                    arguments("", ""),
                    arguments("Alexey Fyodorov", "Alexey Fyodorov"),
                    arguments("Алексей Федоров", "Алексей Фёдоров"),
                    arguments("Алексей Фёдоров", "Алексей Фёдоров"),
                    arguments("Федор Алексеев", "Фёдор Алексеев"),
                    arguments("Фёдор Алексеев", "Фёдор Алексеев"),
                    arguments("Федор Федоров", "Фёдор Фёдоров"),
                    arguments("Фёдор Фёдоров", "Фёдор Фёдоров")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerFixedName(String name, String expected) {
            assertEquals(expected, ContentfulDataLoader.getSpeakerFixedName(name));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractProperty method tests")
    class ExtractPropertyTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments("abc", new ExtractSet(
                                    List.of(new ExtractPair("([a-z]+)", 1)),
                                    "Invalid property: %s"),
                            null, "abc"),
                    arguments("abc", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            null, "abc"),
                    arguments(" abc", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            null, "abc"),
                    arguments("abc ", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            null, "abc"),
                    arguments(" abc ", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            null, "abc"),
                    arguments("42", new ExtractSet(
                                    List.of(new ExtractPair("([a-z]+)", 1)),
                                    "Invalid property: %s"),
                            IllegalArgumentException.class, null),
                    arguments("42", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            IllegalArgumentException.class, null),
                    arguments(" 42", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            IllegalArgumentException.class, null),
                    arguments("42 ", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            IllegalArgumentException.class, null),
                    arguments(" 42 ", new ExtractSet(
                                    List.of(new ExtractPair("^[\\s]*([a-z]+)[\\s]*$", 1)),
                                    "Invalid property: %s"),
                            IllegalArgumentException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractProperty(String value, ExtractSet extractSet, Class<? extends Throwable> expectedException, String expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, ContentfulDataLoader.extractProperty(value, extractSet));
            } else {
                assertThrows(expectedException, () -> ContentfulDataLoader.extractProperty(value, extractSet));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractTwitter method tests")
    class ExtractTwitterTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null),
                    arguments("", null, ""),
                    arguments(" ", null, ""),
                    arguments("arungupta", null, "arungupta"),
                    arguments(" arungupta", null, "arungupta"),
                    arguments("arungupta ", null, "arungupta"),
                    arguments(" arungupta ", null, "arungupta"),
                    arguments("tagir_valeev", null, "tagir_valeev"),
                    arguments("kuksenk0", null, "kuksenk0"),
                    arguments("DaschnerS", null, "DaschnerS"),
                    arguments("@dougqh", null, "dougqh"),
                    arguments("42", null, "42"),
                    arguments("@42", null, "42"),
                    arguments("https://twitter.com/_bravit", null, "_bravit"),
                    arguments("%", IllegalArgumentException.class, null),
                    arguments("%42", IllegalArgumentException.class, null),
                    arguments("%dougqh", IllegalArgumentException.class, null),
                    arguments("dougqh%", IllegalArgumentException.class, null),
                    arguments("dou%gqh", IllegalArgumentException.class, null)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractTwitter(String value, Class<? extends Throwable> expectedException, String expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, ContentfulDataLoader.extractTwitter(value));
            } else {
                assertThrows(expectedException, () -> ContentfulDataLoader.extractTwitter(value));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("extractGitHub method tests")
    class ExtractGitHubTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, null),
                    arguments("", null, ""),
                    arguments(" ", null, ""),
                    arguments("cloudkserg", null, "cloudkserg"),
                    arguments(" cloudkserg", null, "cloudkserg"),
                    arguments("cloudkserg ", null, "cloudkserg"),
                    arguments(" cloudkserg ", null, "cloudkserg"),
                    arguments("pjBooms", null, "pjBooms"),
                    arguments("andre487", null, "andre487"),
                    arguments("Marina-Miranovich", null, "Marina-Miranovich"),
                    arguments("https://github.com/inponomarev", null, "inponomarev"),
                    arguments("http://github.com/inponomarev", null, "inponomarev"),
                    arguments("https://niquola.github.io/blog/", null, "niquola"),
                    arguments("http://niquola.github.io/blog/", null, "niquola"),
                    arguments("https://github.com/Drill4J/realworld-java-and-js-coverage", null, "Drill4J"),
                    arguments("%", IllegalArgumentException.class, null),
                    arguments("%42", IllegalArgumentException.class, null),
                    arguments("%dougqh", IllegalArgumentException.class, null),
                    arguments("dougqh%", IllegalArgumentException.class, null),
                    arguments("dou%gqh", IllegalArgumentException.class, null),
                    arguments("anton.okolelov", null, "anton-okolelov")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void extractGitHub(String value, Class<? extends Throwable> expectedException, String expectedValue) {
            if (expectedException == null) {
                assertEquals(expectedValue, ContentfulDataLoader.extractGitHub(value));
            } else {
                assertThrows(IllegalArgumentException.class, () -> ContentfulDataLoader.extractGitHub(value));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createCompanies method tests")
    class CreateCompaniesTest {
        Company company0 = new Company(0, Collections.emptyList());

        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, new AtomicLong(), false, Collections.emptyList()),
                    arguments(null, "", new AtomicLong(), false, Collections.emptyList()),
                    arguments("", null, new AtomicLong(), false, Collections.emptyList()),
                    arguments("", "", new AtomicLong(), false, Collections.emptyList()),
                    arguments("Company", null, new AtomicLong(), false, List.of(company0)),
                    arguments("Company", "", new AtomicLong(), false, List.of(company0)),
                    arguments(null, "Компания", new AtomicLong(), false, List.of(company0)),
                    arguments("", "Компания", new AtomicLong(), false, List.of(company0)),
                    arguments("Company", "Компания", new AtomicLong(), false, List.of(company0))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createCompanies(String enName, String ruName, AtomicLong companyId, boolean checkEnTextExistence,
                             List<Company> expected) {
            try (MockedStatic<CmsDataLoader> cmsDataLoaderMockedStatic = Mockito.mockStatic(CmsDataLoader.class)) {
                cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.createCompanies(Mockito.nullable(String.class),
                                Mockito.nullable(String.class), Mockito.any(AtomicLong.class), Mockito.anyBoolean()))
                        .thenCallRealMethod();
                cmsDataLoaderMockedStatic.when(() -> CmsDataLoader.extractLocaleItems(Mockito.nullable(String.class),
                                Mockito.nullable(String.class), Mockito.anyBoolean()))
                        .thenReturn(Collections.emptyList());

                assertEquals(expected, CmsDataLoader.createCompanies(enName, ruName, companyId, checkEnTextExistence));
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createEventLocalDate method tests")
    class CreateEventLocalDateTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments("2020-01-01T00:00+03:00", LocalDate.of(2020, 1, 1)),
                    arguments("2020-12-31T00:00+03:00", LocalDate.of(2020, 12, 31))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createEventLocalDate(String zonedDateTimeString, LocalDate expected) {
            assertEquals(expected, ContentfulDataLoader.createEventLocalDate(zonedDateTimeString));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("createEventLocalTime method tests")
    class CreateEventLocalTimeTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments("2020-01-01T08:45+03:00", LocalTime.of(8, 45)),
                    arguments("2020-12-31T10:00Z", LocalTime.of(13, 0))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void createEventLocalTime(String zonedDateTimeString, LocalTime expected) {
            assertEquals(expected, ContentfulDataLoader.createEventLocalTime(zonedDateTimeString));
        }
    }
}
