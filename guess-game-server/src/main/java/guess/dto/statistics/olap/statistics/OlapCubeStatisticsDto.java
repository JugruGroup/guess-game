package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.statistics.olap.OlapStatistics;
import guess.dto.statistics.olap.OlapCubeDimensionDto;
import guess.dto.statistics.olap.OlapMeasureValueDto;
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
        return new OlapCubeStatisticsDto(
                List.of(2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023),
                List.of(new OlapCubeDimensionDto(0, "Moscow"), new OlapCubeDimensionDto(1, "Novosibirsk"),
                        new OlapCubeDimensionDto(2, "Online"), new OlapCubeDimensionDto(3, "Saint Petersburg"),
                        new OlapCubeDimensionDto(4, "Helsinki")),
                List.of(new OlapCubeMetricsDto(0, "C++ Russia",
                                List.of(new OlapMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 0L, 1L)),
                                        new OlapMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 1L, 1L, 1L)),
                                        new OlapMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 1L)))),
                        new OlapCubeMetricsDto(1, "DevOops",
                                List.of(new OlapMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new OlapMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 1L, 1L, 1L)),
                                        new OlapMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 0L, 0L, 1L)))),
                        new OlapCubeMetricsDto(2, "DotNext",
                                List.of(new OlapMeasureValueDto(0, List.of(0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)),
                                        new OlapMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 2L, 2L)),
                                        new OlapMeasureValueDto(3, List.of(0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 1L)),
                                        new OlapMeasureValueDto(4, List.of(0L, 0L, 0L, 0L, 1L))))
                )
        );
    }
}
