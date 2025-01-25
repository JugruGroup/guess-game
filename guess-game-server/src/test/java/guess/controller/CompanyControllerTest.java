package guess.controller;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.source.LocaleItem;
import guess.domain.source.Speaker;
import guess.domain.statistics.company.CompanySearchResult;
import guess.dto.common.SelectedEntitiesDto;
import guess.dto.company.CompanySearchResultDto;
import guess.service.CompanyService;
import guess.service.LocaleService;
import guess.service.SpeakerService;
import guess.util.SearchUtils;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CompanyController class tests")
@WebMvcTest(CompanyController.class)
class CompanyControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private SpeakerService speakerService;

    @MockitoBean
    private LocaleService localeService;

    @Test
    void getCompaniesByFirstLetters() throws Exception {
        final Language language = Language.ENGLISH;
        final String firstLetters = "c";

        MockHttpSession httpSession = new MockHttpSession();

        Company company0 = new Company(0, List.of(new LocaleItem(language.getCode(), "Company0")));
        Company company1 = new Company(1, List.of(new LocaleItem(language.getCode(), "Company1")));
        Company company2 = new Company(2, List.of(new LocaleItem(language.getCode(), "Company2")));

        given(companyService.getCompaniesByFirstLetters(firstLetters, language)).willReturn(List.of(company2, company1, company0));
        given(localeService.getLanguage(httpSession)).willReturn(language);

        mvc.perform(get("/api/company/first-letters-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("firstLetters", firstLetters)
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(0)))
                .andExpect(jsonPath("$[0].name", is("Company0")))
                .andExpect(jsonPath("$[1].id", is(1)))
                .andExpect(jsonPath("$[1].name", is("Company1")))
                .andExpect(jsonPath("$[2].id", is(2)))
                .andExpect(jsonPath("$[2].name", is("Company2")));
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompaniesByFirstLetters(firstLetters, language);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }

    @Test
    void getSelectedCompanies() throws Exception {
        final Language language = Language.ENGLISH;
        final List<Long> ids = List.of(0L, 1L, 2L);

        MockHttpSession httpSession = new MockHttpSession();

        SelectedEntitiesDto selectedEntities = new SelectedEntitiesDto();
        selectedEntities.setIds(ids);

        Company company0 = new Company(0, List.of(new LocaleItem(language.getCode(), "Company0")));
        Company company1 = new Company(1, List.of(new LocaleItem(language.getCode(), "Company1")));
        Company company2 = new Company(2, List.of(new LocaleItem(language.getCode(), "Company2")));

        given(companyService.getCompaniesByIds(ids)).willReturn(List.of(company2, company1, company0));
        given(localeService.getLanguage(httpSession)).willReturn(language);

        mvc.perform(post("/api/company/selected-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(selectedEntities))
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(0)))
                .andExpect(jsonPath("$[0].name", is("Company0")))
                .andExpect(jsonPath("$[1].id", is(1)))
                .andExpect(jsonPath("$[1].name", is("Company1")))
                .andExpect(jsonPath("$[2].id", is(2)))
                .andExpect(jsonPath("$[2].name", is("Company2")));
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompaniesByIds(ids);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }

    @Test
    void getCompanyNamesByFirstLetters() throws Exception {
        final Language language = Language.ENGLISH;
        final String firstLetters = "c";

        MockHttpSession httpSession = new MockHttpSession();

        Company company0 = new Company(0, List.of(new LocaleItem(language.getCode(), "Company0")));
        Company company1 = new Company(1, List.of(new LocaleItem(language.getCode(), "Company1")));
        Company company2 = new Company(2, List.of(new LocaleItem(language.getCode(), "Company2")));

        given(companyService.getCompaniesByFirstLetters(firstLetters, language)).willReturn(List.of(company2, company1, company0));
        given(localeService.getLanguage(httpSession)).willReturn(language);

        mvc.perform(get("/api/company/first-letters-company-names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("firstLetters", firstLetters)
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Company0")))
                .andExpect(jsonPath("$[1]", is("Company1")))
                .andExpect(jsonPath("$[2]", is("Company2")));
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompaniesByFirstLetters(firstLetters, language);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
    }

    @Test
    void getCompany() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        Company company0 = new Company(0, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company0")));
        Company company1 = new Company(1, List.of(new LocaleItem(Language.ENGLISH.getCode(), "Company1")));

        Speaker speaker0 = new Speaker();
        speaker0.setId(0);
        speaker0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        speaker0.setCompanies(List.of(company0));

        Speaker speaker1 = new Speaker();
        speaker1.setId(1);
        speaker1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));
        speaker1.setCompanies(List.of(company0));

        Speaker speaker2 = new Speaker();
        speaker2.setId(2);
        speaker2.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));
        speaker2.setCompanies(List.of(company0, company1));

        given(companyService.getCompanyById(0)).willReturn(company0);
        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);
        given(speakerService.getSpeakersByCompanyId(0)).willReturn(List.of(speaker1, speaker0, speaker2));

        mvc.perform(get("/api/company/company/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.id", is(0)))
                .andExpect(jsonPath("$.speakers", hasSize(3)))
                .andExpect(jsonPath("$.speakers[0].id", is(0)))
                .andExpect(jsonPath("$.speakers[1].id", is(2)))
                .andExpect(jsonPath("$.speakers[2].id", is(1)));
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompanyById(0);
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
        Mockito.verify(speakerService, VerificationModeFactory.times(1)).getSpeakersByCompanyId(0);
    }

    @Test
    void getCompaniesByFirstLetter() throws Exception {
        final boolean isDigit = false;
        final String firstLetter = "a";
        final Language language = Language.ENGLISH;

        MockHttpSession httpSession = new MockHttpSession();

        Company company0 = new Company();
        company0.setId(0);
        company0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

        Company company1 = new Company();
        company1.setId(1);
        company1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        CompanySearchResult companySearchResult0 = new CompanySearchResult(company0, 1, 0, 0);
        CompanySearchResult companySearchResult1 = new CompanySearchResult(company1, 1, 0, 0);

        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);
        given(companyService.getCompaniesByFirstLetter(isDigit, firstLetter, language)).willReturn(List.of(company1, company0));
        given(companyService.getCompanySearchResults(Mockito.anyList())).willReturn(List.of(companySearchResult1, companySearchResult0));

        mvc.perform(get("/api/company/first-letter-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("digit", Boolean.toString(isDigit))
                        .param("firstLetter", firstLetter)
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(0)))
                .andExpect(jsonPath("$[1].id", is(1)));
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompaniesByFirstLetter(isDigit, firstLetter, language);
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompanySearchResults(Mockito.anyList());
    }

    @Test
    void getCompanies() throws Exception {
        final String name = "name";

        MockHttpSession httpSession = new MockHttpSession();

        Company company0 = new Company();
        company0.setId(0);
        company0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

        Company company1 = new Company();
        company1.setId(1);
        company1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

        CompanySearchResult companySearchResult0 = new CompanySearchResult(company0, 1, 0, 0);
        CompanySearchResult companySearchResult1 = new CompanySearchResult(company1, 1, 0, 0);

        given(localeService.getLanguage(httpSession)).willReturn(Language.ENGLISH);
        given(companyService.getCompanies(name)).willReturn(List.of(company1, company0));
        given(companyService.getCompanySearchResults(Mockito.anyList())).willReturn(List.of(companySearchResult1, companySearchResult0));

        mvc.perform(get("/api/company/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", name)
                        .session(httpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(0)))
                .andExpect(jsonPath("$[1].id", is(1)));
        Mockito.verify(localeService, VerificationModeFactory.times(1)).getLanguage(httpSession);
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompanies(name);
        Mockito.verify(companyService, VerificationModeFactory.times(1)).getCompanySearchResults(Mockito.anyList());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("calculateAndConvertToDtoAndSort method tests")
    class CalculateAndConvertToDtoAndSortTest {
        private Stream<Arguments> data() {
            Company company0 = new Company();
            company0.setId(0);
            company0.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name0")));

            Company company1 = new Company();
            company1.setId(1);
            company1.setName(List.of(new LocaleItem(Language.ENGLISH.getCode(), "Name1")));

            Comparator<CompanySearchResultDto> comparatorByName = Comparator.comparing(
                    CompanySearchResultDto::name, String.CASE_INSENSITIVE_ORDER);
            Comparator<CompanySearchResultDto> comparatorByNameWithFirstAlphaNumeric = Comparator.comparing(
                    c -> SearchUtils.getSubStringWithFirstAlphaNumeric(c.name()), String.CASE_INSENSITIVE_ORDER);

            CompanySearchResult companySearchResult0 = new CompanySearchResult(company0, 1, 0, 0);
            CompanySearchResult companySearchResult1 = new CompanySearchResult(company1, 1, 0, 0);

            CompanySearchResultDto companySearchResultDto0 = new CompanySearchResultDto(0, "Name0", 1, 0, 0);
            CompanySearchResultDto companySearchResultDto1 = new CompanySearchResultDto(1, "Name1", 1, 0, 0);

            return Stream.of(
                    arguments(List.of(company0, company1), Language.ENGLISH, comparatorByName,
                            List.of(companySearchResult1, companySearchResult0), List.of(companySearchResultDto0, companySearchResultDto1)),
                    arguments(List.of(company0, company1), Language.ENGLISH, comparatorByNameWithFirstAlphaNumeric,
                            List.of(companySearchResult1, companySearchResult0), List.of(companySearchResultDto0, companySearchResultDto1))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        void calculateAndConvertToDtoAndSort(List<Company> companies, Language language,
                                             Comparator<CompanySearchResultDto> comparator,
                                             List<CompanySearchResult> companySearchResults, List<CompanySearchResultDto> expected) {
            given(companyService.getCompanySearchResults(Mockito.anyList())).willReturn(companySearchResults);

            CompanyController companyController = new CompanyController(companyService, speakerService, localeService);

            assertEquals(expected, companyController.calculateAndConvertToDtoAndSort(companies, language, comparator));
        }
    }
}
