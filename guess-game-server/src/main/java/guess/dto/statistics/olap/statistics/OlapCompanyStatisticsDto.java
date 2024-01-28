package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.source.Company;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.dto.statistics.olap.metrics.OlapCompanyMetricsDto;
import guess.dto.statistics.olap.metrics.OlapEntityMetricsDto;

import java.util.List;

/**
 * OLAP company statistics DTO.
 */
public class OlapCompanyStatisticsDto extends OlapEntityStatisticsDto<Integer, OlapCompanyMetricsDto> {
    public OlapCompanyStatisticsDto(List<Integer> dimensionValues, List<OlapCompanyMetricsDto> metricsList,
                                    OlapEntityMetricsDto totals) {
        super(dimensionValues, metricsList, totals);
    }

    public static OlapCompanyStatisticsDto convertToDto(OlapEntityStatistics<Integer, Company> companyStatistics, Language language) {
        return new OlapCompanyStatisticsDto(
                companyStatistics.getDimensionValues1(),
                OlapCompanyMetricsDto.convertToDto(companyStatistics.getMetricsList(), language),
                OlapEntityMetricsDto.convertToDto(companyStatistics.getTotals()));
    }
}
