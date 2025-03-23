package guess.util;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.source.LocaleItem;
import guess.domain.source.Speaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("LocalizationUtils class tests")
public class LocalizationUtilsTest {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getString method tests")
    class GetStringTest {
        private Stream<Arguments> data() {
            final List<LocaleItem> standardLocaleItems = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "Text"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Текст"));
            final List<LocaleItem> emptyLocaleItems = Collections.emptyList();
            final List<LocaleItem> onlyEnglishLocaleItems = Collections.singletonList(
                    new LocaleItem(Language.ENGLISH.getCode(), "Text"));
            final List<LocaleItem> onlyRussianLocaleItems = Collections.singletonList(
                    new LocaleItem(Language.RUSSIAN.getCode(), "Текст"));

            return Stream.of(
                    arguments(null, null, null, ""),
                    arguments(null, null, Language.ENGLISH, ""),
                    arguments(null, Language.ENGLISH, null, ""),
                    arguments(null, Language.ENGLISH, Language.ENGLISH, ""),

                    arguments(standardLocaleItems, Language.ENGLISH, Language.ENGLISH, "Text"),
                    arguments(standardLocaleItems, Language.RUSSIAN, Language.ENGLISH, "Текст"),
                    arguments(standardLocaleItems, null, Language.ENGLISH, "Text"),
                    arguments(standardLocaleItems, Language.ENGLISH, null, "Text"),
                    arguments(standardLocaleItems, null, null, ""),

                    arguments(emptyLocaleItems, Language.ENGLISH, Language.ENGLISH, ""),
                    arguments(emptyLocaleItems, Language.RUSSIAN, Language.ENGLISH, ""),

                    arguments(onlyEnglishLocaleItems, Language.ENGLISH, Language.ENGLISH, "Text"),
                    arguments(onlyEnglishLocaleItems, Language.RUSSIAN, Language.ENGLISH, "Text"),
                    arguments(onlyEnglishLocaleItems, Language.ENGLISH, Language.RUSSIAN, "Text"),
                    arguments(onlyEnglishLocaleItems, Language.RUSSIAN, Language.RUSSIAN, ""),

                    arguments(onlyRussianLocaleItems, Language.ENGLISH, Language.ENGLISH, ""),
                    arguments(onlyRussianLocaleItems, Language.RUSSIAN, Language.ENGLISH, "Текст"),
                    arguments(onlyRussianLocaleItems, Language.ENGLISH, Language.RUSSIAN, "Текст"),
                    arguments(onlyRussianLocaleItems, Language.RUSSIAN, Language.RUSSIAN, "Текст")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getString(List<LocaleItem> localeItems, Language language, Language defaultLanguage, String expected) {
            assertEquals(expected, LocalizationUtils.getString(localeItems, language, defaultLanguage));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getResourceString method tests")
    class GetResourceStringTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments("allEventsOptionText", Language.ENGLISH, "All events of selected types"),
                    arguments("allEventsOptionText", Language.RUSSIAN, "Все события выбранных типов")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getResourceString(String key, Language language, String expected) {
            assertEquals(expected, LocalizationUtils.getResourceString(key, language));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerNamesWithLastNameFirst method tests")
    class GetSpeakerNamesWithLastNameFirstTest {
        private Stream<Arguments> data() {
            final List<LocaleItem> nameLocaleItems0 = List.of(
                    new LocaleItem("", "FirstName LastName"));

            final List<LocaleItem> sourceNameLocaleItems1 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "FirstName LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Имя Фамилия"));
            final List<LocaleItem> resultNameLocaleItems1 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "LastName FirstName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Фамилия Имя"));

            final List<LocaleItem> nameLocaleItems2 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Фамилия"));

            final List<LocaleItem> sourceNameLocaleItems3 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), " LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Фамилия "));
            final List<LocaleItem> resultNameLocaleItems3 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Фамилия"));

            final List<LocaleItem> sourceNameLocaleItems4 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "FirstName MiddleName LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Имя Отчество Фамилия"));
            final List<LocaleItem> resultNameLocaleItems4 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "LastName FirstName MiddleName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Фамилия Имя Отчество"));

            final List<LocaleItem> sourceNameLocaleItems5 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), " FirstName MiddleName LastName "),
                    new LocaleItem(Language.RUSSIAN.getCode(), " Имя Отчество Фамилия "));
            final List<LocaleItem> resultNameLocaleItems5 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "LastName FirstName MiddleName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Фамилия Имя Отчество"));

            final List<LocaleItem> sourceNameLocaleItems6 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), " "),
                    new LocaleItem(Language.RUSSIAN.getCode(), "  "));
            final List<LocaleItem> resultNameLocaleItems6 = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), ""),
                    new LocaleItem(Language.RUSSIAN.getCode(), ""));

            Speaker speaker0 = new Speaker();

            Speaker speaker1 = new Speaker();
            speaker1.setName(Collections.emptyList());

            Speaker speaker2 = new Speaker();
            speaker2.setName(nameLocaleItems0);

            Speaker speaker3 = new Speaker();
            speaker3.setName(sourceNameLocaleItems1);

            Speaker speaker4 = new Speaker();
            speaker4.setName(nameLocaleItems2);

            Speaker speaker5 = new Speaker();
            speaker5.setName(sourceNameLocaleItems3);

            Speaker speaker6 = new Speaker();
            speaker6.setName(sourceNameLocaleItems4);

            Speaker speaker7 = new Speaker();
            speaker7.setName(sourceNameLocaleItems5);

            Speaker speaker8 = new Speaker();
            speaker8.setName(sourceNameLocaleItems6);

            return Stream.of(
                    arguments(speaker0, null),
                    arguments(speaker1, Collections.emptyList()),
                    arguments(speaker2, nameLocaleItems0),
                    arguments(speaker3, resultNameLocaleItems1),
                    arguments(speaker4, nameLocaleItems2),
                    arguments(speaker5, resultNameLocaleItems3),
                    arguments(speaker6, resultNameLocaleItems4),
                    arguments(speaker7, resultNameLocaleItems5),
                    arguments(speaker8, resultNameLocaleItems6)
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerNamesWithLastNameFirst(Speaker speaker, List<LocaleItem> expected) {
            assertEquals(expected, LocalizationUtils.getSpeakerNamesWithLastNameFirst(speaker));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerCompanies method tests")
    class GetSpeakerCompaniesTest {
        private Stream<Arguments> data() {
            Company company0 = new Company(0, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company0"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания0")));
            Company company1 = new Company(1, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company1"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания1")
            ));

            Speaker speaker0 = new Speaker();
            speaker0.setId(0);
            speaker0.setCompanies(Collections.emptyList());

            Speaker speaker1 = new Speaker();
            speaker1.setId(1);
            speaker1.setCompanies(List.of(company0));

            Speaker speaker2 = new Speaker();
            speaker2.setId(2);
            speaker2.setCompanies(List.of(company0, company1));

            return Stream.of(
                    arguments(speaker0, Language.ENGLISH, ""),
                    arguments(speaker1, Language.ENGLISH, "Company0"),
                    arguments(speaker1, Language.RUSSIAN, "Компания0"),
                    arguments(speaker2, Language.ENGLISH, "Company0, Company1"),
                    arguments(speaker2, Language.RUSSIAN, "Компания0, Компания1")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerCompanies(Speaker speaker, Language language, String expected) {
            assertEquals(expected, LocalizationUtils.getSpeakerCompanies(speaker, language));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerNameWithCompanies method tests")
    class GetSpeakerNameWithCompaniesTest {
        private Stream<Arguments> data() {
            final List<LocaleItem> fullNameLocaleItems = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "Name"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Имя"));
            final List<LocaleItem> englishNameLocaleItems = Collections.singletonList(
                    new LocaleItem(Language.ENGLISH.getCode(), "Name"));

            Company company0 = new Company(0, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company0"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания0")));
            Company company1 = new Company(1, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company1")));

            final List<Company> emptyCompanyList = Collections.emptyList();
            final List<Company> fullLanguageCompanyList = List.of(company0);
            final List<Company> englishLanguageCompanyList = List.of(company1);
            final List<Company> someCompanyList = List.of(company0, company1);
            final List<Company> someCompanyReverseList = List.of(company1, company0);

            return Stream.of(
                    arguments(new Speaker(
                                    0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    null,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, ""),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    null,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, ""),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "Name"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Имя"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    emptyCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "Name"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    emptyCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Имя"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    fullLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "Name (Company0)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    fullLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Имя (Компания0)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    englishLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Имя (Company1)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    fullLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Name (Компания0)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    englishLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Name (Company1)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    someCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "Name (Company0, Company1)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    someCompanyReverseList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "Name (Company0, Company1)")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerNameWithCompanies(Speaker speaker, Language language, String expected) {
            assertEquals(expected, LocalizationUtils.getSpeakerNameWithCompanies(speaker, language));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerNameWithLastNameFirstWithCompanies method tests")
    class GetSpeakerNameWithLastNameFirstWithCompaniesTest {
        private Stream<Arguments> data() {
            final List<LocaleItem> fullNameLocaleItems = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "FirstName LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Имя Фамилия"));
            final List<LocaleItem> englishNameLocaleItems = Collections.singletonList(
                    new LocaleItem(Language.ENGLISH.getCode(), "FirstName LastName"));

            Company company0 = new Company(0, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company0"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания0")));
            Company company1 = new Company(1, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company1")));

            final List<Company> emptyCompanyList = Collections.emptyList();
            final List<Company> fullLanguageCompanyList = List.of(company0);
            final List<Company> englishLanguageCompanyList = List.of(company1);
            final List<Company> someCompanyList = List.of(company0, company1);
            final List<Company> someCompanyReverseList = List.of(company1, company0);

            return Stream.of(
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    null,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, ""),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    null,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, ""),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "LastName FirstName"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    new ArrayList<>(),
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Фамилия Имя"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    emptyCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "LastName FirstName"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    emptyCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Фамилия Имя"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    fullLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "LastName FirstName (Company0)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    fullLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Фамилия Имя (Компания0)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    fullNameLocaleItems,
                                    englishLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "Фамилия Имя (Company1)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    fullLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "LastName FirstName (Компания0)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    englishLanguageCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.RUSSIAN, "LastName FirstName (Company1)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    someCompanyList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "LastName FirstName (Company0, Company1)"),
                    arguments(new Speaker(0L,
                                    new Speaker.SpeakerPhoto(
                                            "00000.jpg",
                                            null
                                    ),
                                    englishNameLocaleItems,
                                    someCompanyReverseList,
                                    null,
                                    new Speaker.SpeakerSocials(
                                            null,
                                            null,
                                            null
                                    ),
                                    new Speaker.SpeakerDegrees(
                                            false,
                                            false,
                                            false
                                    )
                            ),
                            Language.ENGLISH, "LastName FirstName (Company0, Company1)")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerNameWithLastNameFirstWithCompanies(Speaker speaker, Language language, String expected) {
            assertEquals(expected, LocalizationUtils.getSpeakerNameWithLastNameFirstWithCompanies(speaker, language));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerName method tests")
    class GetSpeakerNameTest {
        private Stream<Arguments> data() {
            final List<LocaleItem> fullNameLocaleItems = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "FirstName LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Имя Фамилия"));

            Company company = new Company(0, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания")));

            Speaker speaker0 = new Speaker(
                    0L,
                    new Speaker.SpeakerPhoto(
                            "00000.jpg",
                            null
                    ),
                    fullNameLocaleItems,
                    List.of(company),
                    null,
                    new Speaker.SpeakerSocials(
                            null,
                            null,
                            null
                    ),
                    new Speaker.SpeakerDegrees(
                            false,
                            false,
                            false
                    )
            );

            Set<Speaker> emptySpeakerDuplicates = Collections.emptySet();
            Set<Speaker> fullSpeakerDuplicates = Set.of(speaker0);

            return Stream.of(
                    arguments(speaker0, Language.ENGLISH, emptySpeakerDuplicates, "FirstName LastName"),
                    arguments(speaker0, Language.ENGLISH, fullSpeakerDuplicates, "FirstName LastName (Company)")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerName(Speaker speaker, Language language, Set<Speaker> speakerDuplicates, String expected) {
            assertEquals(expected, LocalizationUtils.getSpeakerName(speaker, language, speakerDuplicates));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getSpeakerNameWithLastNameFirst method tests")
    class GetSpeakerNameWithLastNameFirstTest {
        private Stream<Arguments> data() {
            final List<LocaleItem> fullNameLocaleItems = Arrays.asList(
                    new LocaleItem(Language.ENGLISH.getCode(), "FirstName LastName"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Имя Фамилия"));

            Company company0 = new Company(0, List.of(
                    new LocaleItem(Language.ENGLISH.getCode(), "Company"),
                    new LocaleItem(Language.RUSSIAN.getCode(), "Компания")));

            Speaker speaker0 = new Speaker(0L,
                    new Speaker.SpeakerPhoto(
                            "00000.jpg",
                            null
                    ),
                    fullNameLocaleItems,
                    List.of(company0),
                    null,
                    new Speaker.SpeakerSocials(
                            null,
                            null,
                            null
                    ),
                    new Speaker.SpeakerDegrees(
                            false,
                            false,
                            false
                    )
            );

            Set<Speaker> emptySpeakerDuplicates = Collections.emptySet();
            Set<Speaker> fullSpeakerDuplicates = Set.of(speaker0);

            return Stream.of(
                    arguments(speaker0, Language.ENGLISH, emptySpeakerDuplicates, "LastName FirstName"),
                    arguments(speaker0, Language.RUSSIAN, emptySpeakerDuplicates, "Фамилия Имя"),
                    arguments(speaker0, Language.ENGLISH, fullSpeakerDuplicates, "LastName FirstName (Company)"),
                    arguments(speaker0, Language.RUSSIAN, fullSpeakerDuplicates, "Фамилия Имя (Компания)")
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getSpeakerNameWithLastNameFirst(Speaker speaker, Language language, Set<Speaker> speakerDuplicates, String expected) {
            assertEquals(expected, LocalizationUtils.getSpeakerNameWithLastNameFirst(speaker, language, speakerDuplicates));
        }
    }
}
