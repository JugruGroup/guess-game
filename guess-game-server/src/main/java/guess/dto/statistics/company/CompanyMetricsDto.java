package guess.dto.statistics.company;

import guess.domain.Language;
import guess.domain.statistics.company.AbstractCompanyMetrics;
import guess.domain.statistics.company.CompanyMetrics;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Objects;

/**
 * Company metrics DTO.
 */
public class CompanyMetricsDto extends AbstractCompanyMetrics {
    private final long id;
    private final String name;

    public CompanyMetricsDto(long id, String name, CompanyMetrics metrics) {
        super(metrics.getTalksQuantity(), metrics.getEventsQuantity(), metrics.getEventTypesQuantity(),
                metrics.getJavaChampionsQuantity(), metrics.getMvpsQuantity(), metrics.getSpeakersQuantity());

        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static CompanyMetricsDto convertToDto(CompanyMetrics companyMetrics, Language language) {
        var company = companyMetrics.getCompany();
        var name = LocalizationUtils.getString(company.getName(), language);

        return new CompanyMetricsDto(
                company.getId(),
                name,
                companyMetrics);
    }

    public static List<CompanyMetricsDto> convertToDto(List<CompanyMetrics> companyMetricsList, Language language) {
        return companyMetricsList.stream()
                .map(cm -> convertToDto(cm, language))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyMetricsDto that)) return false;
        if (!super.equals(o)) return false;
        return id == that.id &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name);
    }
}
