package guess.dto.statistics.olap.metrics;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.statistics.olap.OlapEntityMetrics;
import guess.util.LocalizationUtils;

import java.util.List;

/**
 * OLAP company metrics DTO.
 */
public class OlapCompanyMetricsDto extends OlapEntityMetricsDto {
    public OlapCompanyMetricsDto(long id, String name, List<Long> measureValues, List<Long> cumulativeMeasureValues, Long total) {
        super(id, name, measureValues, cumulativeMeasureValues, total);
    }

    public static OlapCompanyMetricsDto convertToDto(OlapEntityMetrics<Company> companyMetrics, Language language) {
        var company = companyMetrics.entity();
        var name = LocalizationUtils.getString(company.getName(), language);

        return new OlapCompanyMetricsDto(
                company.getId(),
                name,
                companyMetrics.measureValues(),
                companyMetrics.cumulativeMeasureValues(),
                companyMetrics.total());
    }

    public static List<OlapCompanyMetricsDto> convertToDto(List<OlapEntityMetrics<Company>> companyMetricsList, Language language) {
        return companyMetricsList.stream()
                .map(cm -> convertToDto(cm, language))
                .toList();
    }

    @Override
    public String toString() {
        return "OlapCompanyMetricsDto{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }
}
