package guess.service;

import guess.dao.CompanyDao;
import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.source.LocaleItem;
import guess.util.LocalizationUtils;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("CompanyServiceImpl class tests")
@ExtendWith(SpringExtension.class)
class CompanyServiceImplTest {
    private static final Company company0 = new Company(0, Collections.emptyList());
    private static final Company company1 = new Company(1, Collections.emptyList());
    private static final Company company2 = new Company(2, Collections.emptyList());

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        CompanyDao companyDao() {
            CompanyDao companyDao = Mockito.mock(CompanyDao.class);

            Mockito.when(companyDao.getCompanies()).thenReturn(List.of(company0, company1, company2));

            return companyDao;
        }

        @Bean
        CompanyService companyService() {
            return new CompanyServiceImpl(companyDao());
        }
    }

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private CompanyService companyService;

    @Test
    void getCompanies() {
        companyService.getCompanies();
        Mockito.verify(companyDao, VerificationModeFactory.times(1)).getCompanies();
        Mockito.verifyNoMoreInteractions(companyDao);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getCompaniesByFirstLetters method tests")
    class GetCompaniesByFirstLettersTest {
        private Stream<Arguments> data() {
            return Stream.of(
                    arguments(null, null, Collections.emptyList(), null, Collections.emptyList()),
                    arguments("", null, Collections.emptyList(), null, Collections.emptyList()),
                    arguments("", Language.ENGLISH, List.of(company0), "", List.of(company0)),
                    arguments("", Language.ENGLISH, List.of(company0), "N", List.of(company0)),
                    arguments("na", Language.ENGLISH, List.of(company0), "Name", List.of(company0)),
                    arguments("na", Language.ENGLISH, List.of(company0, company1), "Name", List.of(company0, company1)),
                    arguments("an", Language.ENGLISH, List.of(company0), "Name", Collections.emptyList()),
                    arguments("an", Language.ENGLISH, List.of(company0, company1), "Name", Collections.emptyList())
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void getCompaniesByFirstLetters(String firstLetters, Language language, List<Company> companies,
                                        String localizationString, List<Company> expected,
                                        @Mocked CompanyDao companyDaoMock) {
            new MockUp<LocalizationUtils>() {
                @Mock
                String getString(List<LocaleItem> localeItems, Language language) {
                    return localizationString;
                }
            };

            new Expectations() {{
                companyDaoMock.getCompanies();
                result = companies;
            }};

            CompanyService companyService = new CompanyServiceImpl(companyDaoMock);

            assertEquals(expected, companyService.getCompaniesByFirstLetters(firstLetters, language));
        }
    }
}
