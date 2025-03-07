package guess.controller;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.source.Speaker;
import guess.domain.statistics.company.CompanySearchResult;
import guess.dto.common.SelectedEntitiesDto;
import guess.dto.company.CompanyBriefDto;
import guess.dto.company.CompanyDetailsDto;
import guess.dto.company.CompanySearchResultDto;
import guess.dto.speaker.SpeakerBriefDto;
import guess.service.CompanyService;
import guess.service.LocaleService;
import guess.service.SpeakerService;
import guess.util.LocalizationUtils;
import guess.util.SearchUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Company controller.
 */
@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService companyService;
    private final SpeakerService speakerService;
    private final LocaleService localeService;

    @Autowired
    public CompanyController(CompanyService companyService, SpeakerService speakerService, LocaleService localeService) {
        this.companyService = companyService;
        this.speakerService = speakerService;
        this.localeService = localeService;
    }

    @GetMapping("/first-letters-companies")
    public List<CompanyBriefDto> getCompaniesByFirstLetters(@RequestParam String firstLetters, HttpSession httpSession) {
        var language = localeService.getLanguage(httpSession);
        List<Company> companies = companyService.getCompaniesByFirstLetters(firstLetters, language);

        return CompanyBriefDto.convertToBriefDto(companies, language).stream()
                .sorted(Comparator.comparing(CompanyBriefDto::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @PostMapping("/selected-companies")
    public List<CompanyBriefDto> getSelectedCompanies(@RequestBody SelectedEntitiesDto selectedEntities, HttpSession httpSession) {
        var language = localeService.getLanguage(httpSession);
        List<Company> companies = companyService.getCompaniesByIds(selectedEntities.getIds());

        return CompanyBriefDto.convertToBriefDto(companies, language).stream()
                .sorted(Comparator.comparing(CompanyBriefDto::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @GetMapping("/first-letters-company-names")
    public List<String> getCompanyNamesByFirstLetters(@RequestParam String firstLetters, HttpSession httpSession) {
        var language = localeService.getLanguage(httpSession);
        List<Company> companies = companyService.getCompaniesByFirstLetters(firstLetters, language);

        return companies.stream()
                .map(c -> LocalizationUtils.getString(c.getName(), language))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @GetMapping("/company/{id}")
    public CompanyDetailsDto getCompany(@PathVariable long id, HttpSession httpSession) {
        var company = companyService.getCompanyById(id);
        var language = localeService.getLanguage(httpSession);
        List<Speaker> speakers = speakerService.getSpeakersByCompanyId(id);
        var companyDetailsDto = CompanyDetailsDto.convertToDto(company, speakers, language);

        Comparator<SpeakerBriefDto> comparatorByName = Comparator.comparing(SpeakerBriefDto::getDisplayName, String.CASE_INSENSITIVE_ORDER);
        Comparator<SpeakerBriefDto> comparatorByCompany = Comparator.comparing(
                s -> s.getCompanies().stream()
                        .map(CompanyBriefDto::getName)
                        .collect(Collectors.joining(", ")), String.CASE_INSENSITIVE_ORDER);
        List<SpeakerBriefDto> sortedSpeakers = companyDetailsDto.speakers().stream()
                .sorted(comparatorByName.thenComparing(comparatorByCompany))
                .toList();

        return new CompanyDetailsDto(companyDetailsDto.company(), sortedSpeakers);
    }

    @GetMapping("/first-letter-companies")
    public List<CompanySearchResultDto> getCompaniesByFirstLetter(@RequestParam boolean digit,
                                                                  @RequestParam(required = false) String firstLetter,
                                                                  @RequestParam String language) {
        var languageEnum = Language.getLanguageByCode(language);
        List<Company> companies = companyService.getCompaniesByFirstLetter(digit, firstLetter, languageEnum);
        Comparator<CompanySearchResultDto> comparatorByNameWithFirstAlphaNumeric = Comparator.comparing(
                c -> SearchUtils.getSubStringWithFirstAlphaNumeric(c.name()), String.CASE_INSENSITIVE_ORDER);

        return calculateAndConvertToDtoAndSort(companies, languageEnum, comparatorByNameWithFirstAlphaNumeric);
    }

    @GetMapping("/companies")
    public List<CompanySearchResultDto> getCompanies(@RequestParam(required = false) String name, HttpSession httpSession) {
        var language = localeService.getLanguage(httpSession);
        List<Company> companies = companyService.getCompanies(name);
        Comparator<CompanySearchResultDto> comparatorByName = Comparator.comparing(CompanySearchResultDto::name, String.CASE_INSENSITIVE_ORDER);

        return calculateAndConvertToDtoAndSort(companies, language, comparatorByName);
    }

    List<CompanySearchResultDto> calculateAndConvertToDtoAndSort(List<Company> companies, Language language,
                                                                 Comparator<CompanySearchResultDto> comparator) {
        List<CompanySearchResult> companySearchResults = companyService.getCompanySearchResults(companies);

        return companySearchResults.stream()
                .map(c -> CompanySearchResultDto.convertToDto(c, language))
                .sorted(comparator)
                .toList();
    }
}
