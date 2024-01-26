package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.statistics.olap.Olap3dCubeDimensionDto;
import guess.dto.statistics.olap.Olap3dCubeMeasureValueDto;
import guess.dto.statistics.olap.metrics.Olap3dCubeMetricsDto;

import java.util.List;

/**
 * OLAP 3D cube statistics DTO.
 */
public record Olap3dCubeStatisticsDto(List<Integer> dimensionValues1, List<Olap3dCubeDimensionDto> dimensionValues2,
                                      List<Olap3dCubeMetricsDto> metricsList) {

    public static Olap3dCubeStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        //TODO: implement
        return new Olap3dCubeStatisticsDto(
                List.of(2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024),
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
                                        new Olap3dCubeMeasureValueDto(4, List.of(0L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(3, "Heisenbug",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 2L, 2L, 2L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)))),
                        new Olap3dCubeMetricsDto(4, "HolyJS",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 2L, 2L, 2L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)))),
                        new Olap3dCubeMetricsDto(5, "Hydra",
                                List.of(new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(6, "JBreak",
                                List.of(new Olap3dCubeMeasureValueDto(1, List.of(0L, 0L, 0L, 0L, 1L, 1L, 1L)))),
                        new Olap3dCubeMetricsDto(7, "JPoint",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(8, "Joker",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(9, "Mobius",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 2L, 2L, 2L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 1L, 1L, 1L, 1L, 1L, 1L, 0L, 0L, 1L, 1L)))),
                        new Olap3dCubeMetricsDto(10, "SPTDC",
                                List.of(new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(11, "SmartData",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(12, "TechTrain",
                                List.of(new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 2L, 2L, 2L, 2L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L)))),
                        new Olap3dCubeMetricsDto(13, "JUG.MSK",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(1L, 4L, 5L, 3L, 7L, 5L, 8L, 11L, 0L, 0L, 1L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(14, "JUG.ru",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 3L, 3L, 2L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(12L, 15L, 14L, 10L, 10L, 10L, 6L, 7L)))),
                        new Olap3dCubeMetricsDto(15, "JUGNsk",
                                List.of(new Olap3dCubeMeasureValueDto(1, List.of(0L, 0L, 0L, 0L, 0L, 0L, 5L, 7L, 1L, 2L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 3L, 1L)))),
                        new Olap3dCubeMetricsDto(16, "SnowOne",
                                List.of(new Olap3dCubeMeasureValueDto(1, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 0L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(17, "VideoTech",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)))),
                        new Olap3dCubeMetricsDto(18, "PiterPy",
                                List.of(new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L)),
                                        new Olap3dCubeMeasureValueDto(3, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L)))),
                        new Olap3dCubeMetricsDto(19, "Flow",
                                List.of(new Olap3dCubeMeasureValueDto(0, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L)),
                                        new Olap3dCubeMeasureValueDto(2, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1L))))
                )
        );
    }
}
