package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.statistics.olap.OlapStatistics;
import guess.dto.statistics.olap.OlapCubeDimensionDto;
import guess.dto.statistics.olap.metrics.OlapCubeMetricsDto;

import java.util.List;

/**
 * OLAP cube statistics DTO.
 */
public class OlapCubeStatisticsDto {
    private final List<Integer> dimensionValues1;
    private final List<OlapCubeDimensionDto> dimensionValues2;
    private final List<OlapCubeMetricsDto> metricsList;

    public OlapCubeStatisticsDto(List<Integer> dimensionValues1, List<OlapCubeDimensionDto> dimensionValues2,
                                 List<OlapCubeMetricsDto> metricsList) {
        this.dimensionValues1 = dimensionValues1;
        this.dimensionValues2 = dimensionValues2;
        this.metricsList = metricsList;
    }

    public List<Integer> getDimensionValues1() {
        return dimensionValues1;
    }

    public List<OlapCubeDimensionDto> getDimensionValues2() {
        return dimensionValues2;
    }

    public List<OlapCubeMetricsDto> getMetricsList() {
        return metricsList;
    }

    public static OlapCubeStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        //TODO: implement
        return null;
    }
}
