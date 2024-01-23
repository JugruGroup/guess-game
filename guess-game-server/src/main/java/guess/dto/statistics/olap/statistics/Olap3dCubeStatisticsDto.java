package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.statistics.olap.OlapStatistics;
import guess.dto.statistics.olap.Olap3dCubeDimensionDto;
import guess.dto.statistics.olap.Olap3dCubeMeasureValueDto;
import guess.dto.statistics.olap.metrics.Olap3dCubeMetricsDto;

import java.util.List;

/**
 * OLAP 3D cube statistics DTO.
 */
public class Olap3dCubeStatisticsDto {
    private final List<Integer> dimensionValues1;
    private final List<Olap3dCubeDimensionDto> dimensionValues2;
    private final List<Olap3dCubeMetricsDto> metricsList;

    public Olap3dCubeStatisticsDto(List<Integer> dimensionValues1, List<Olap3dCubeDimensionDto> dimensionValues2,
                                   List<Olap3dCubeMetricsDto> metricsList) {
        this.dimensionValues1 = dimensionValues1;
        this.dimensionValues2 = dimensionValues2;
        this.metricsList = metricsList;
    }

    public List<Integer> getDimensionValues1() {
        return dimensionValues1;
    }

    public List<Olap3dCubeDimensionDto> getDimensionValues2() {
        return dimensionValues2;
    }

    public List<Olap3dCubeMetricsDto> getMetricsList() {
        return metricsList;
    }

    public static Olap3dCubeStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        //TODO: implement
        return new Olap3dCubeStatisticsDto(
                List.of(2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023),
                List.of(new Olap3dCubeDimensionDto(0, "Moscow"), new Olap3dCubeDimensionDto(1, "Novosibirsk"),
                        new Olap3dCubeDimensionDto(2, "Online"), new Olap3dCubeDimensionDto(3, "Saint Petersburg"),
                        new Olap3dCubeDimensionDto(4, "Helsinki")),
                List.of(new Olap3dCubeMetricsDto(0, "C++ Russia",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(1, "DevOops",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(2, "DotNext",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 2L, 2L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(4, List.of(0L, 0L, 0L, 0L, 1L))))
                )
        );
    }
}
