package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.source.Nameable;
import guess.domain.source.Topic;
import guess.domain.statistics.olap.metrics.OlapEntityMetrics;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.statistics.olap.metrics.OlapEntityMetricsDto;
import guess.util.LocalizationUtils;

import java.util.Collections;
import java.util.List;

/**
 * OLAP topic statistics DTO.
 */
public class OlapTopicStatisticsDto extends OlapEntityStatisticsDto<String, OlapEntityMetricsDto> {
    protected OlapTopicStatisticsDto(List<String> dimensionValues, List<OlapEntityMetricsDto> metricsList, OlapEntityMetricsDto totals) {
        super(dimensionValues, metricsList, totals);
    }

    private static OlapEntityStatistics<Topic, Void, ? extends Nameable> getFirstStatistics(OlapStatistics olapStatistics) {
        if (olapStatistics.topicEventTypeStatistics() != null) {
            return olapStatistics.topicEventTypeStatistics();
        } else if (olapStatistics.topicSpeakerStatistics() != null) {
            return olapStatistics.topicSpeakerStatistics();
        } else if (olapStatistics.topicCompanyStatistics() != null) {
            return olapStatistics.topicCompanyStatistics();
        } else {
            throw new IllegalArgumentException("Topic statistics not found");
        }
    }

    public static OlapTopicStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        OlapEntityStatistics<Topic, Void, ? extends Nameable> statistics = getFirstStatistics(olapStatistics);
        List<String> dimensionValues = statistics.getDimensionValues1().stream()
                .map(t -> LocalizationUtils.getString(t.getName(), language))
                .toList();
        List<OlapEntityMetricsDto> metricsList = statistics.getMetricsList().stream()
                .map(m -> new OlapEntityMetricsDto(
                        m.entity().getId(),
                        LocalizationUtils.getString(m.entity().getName(), language),
                        m.measureValues(),
                        Collections.emptyList(),
                        m.total()))
                .toList();

        OlapEntityMetrics<Void> totals = statistics.getTotals();
        OlapEntityMetrics<Void> totalsWithEmptyCumulativeMeasureValues = new OlapEntityMetrics<>(
                totals.entity(), totals.measureValues(), Collections.emptyList(), totals.total());

        return new OlapTopicStatisticsDto(
                dimensionValues,
                metricsList,
                OlapEntityMetricsDto.convertToDto(totalsWithEmptyCumulativeMeasureValues));
    }
}
