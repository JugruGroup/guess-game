package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.source.Nameable;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.statistics.olap.Olap3dCubeDimensionDto;
import guess.dto.statistics.olap.Olap3dCubeMeasureValueDto;
import guess.dto.statistics.olap.metrics.Olap3dCubeMetricsDto;
import guess.util.LocalizationUtils;

import java.util.List;

/**
 * OLAP 3D cube statistics DTO.
 */
public record Olap3dCubeStatisticsDto(List<Integer> dimensionValues1, List<Olap3dCubeDimensionDto> dimensionValues2,
                                      List<Olap3dCubeMetricsDto> metricsList) {

    private static OlapEntityStatistics<Integer, ? extends Nameable, ? extends Nameable> getFirstStatistics(OlapStatistics olapStatistics) {
        if (olapStatistics.yearEventTypeStatistics() != null) {
            return olapStatistics.yearEventTypeStatistics();
        } else if (olapStatistics.yearSpeakerStatistics() != null) {
            return olapStatistics.yearSpeakerStatistics();
        } else if (olapStatistics.yearCompanyStatistics() != null) {
            return olapStatistics.yearCompanyStatistics();
        } else {
            throw new IllegalArgumentException("Year statistics not found");
        }
    }

    public static Olap3dCubeStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        OlapEntityStatistics<Integer, ? extends Nameable, ? extends Nameable> statistics = getFirstStatistics(olapStatistics);
        List<Olap3dCubeDimensionDto> dimensionValues2 = statistics.getDimensionValues2().stream()
                .map(v -> new Olap3dCubeDimensionDto(
                        v.getId(),
                        LocalizationUtils.getString(v.getName(), language)))
                .toList();
        List<Olap3dCubeMetricsDto> metricsList = statistics.getSubMetricsList().stream()
                .map(m -> {
                    List<Olap3dCubeMeasureValueDto> measureValueList = m.measureValues().entrySet().stream()
                            .map(e -> new Olap3dCubeMeasureValueDto(
                                    e.getKey().getId(),
                                    e.getValue())
                            )
                            .toList();

                    return new Olap3dCubeMetricsDto(
                            m.entity().getId(),
                            LocalizationUtils.getString(m.entity().getName(), language),
                            measureValueList);
                })
                .toList();

        return new Olap3dCubeStatisticsDto(
                statistics.getDimensionValues1(),
                dimensionValues2,
                metricsList);
    }
}
