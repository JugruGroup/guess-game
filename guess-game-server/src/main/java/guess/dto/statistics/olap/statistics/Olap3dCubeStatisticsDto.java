package guess.dto.statistics.olap.statistics;

import guess.domain.Language;
import guess.domain.source.EventType;
import guess.domain.source.Nameable;
import guess.domain.statistics.olap.dimension.City;
import guess.domain.statistics.olap.statistics.OlapEntityStatistics;
import guess.domain.statistics.olap.statistics.OlapStatistics;
import guess.dto.eventtype.EventTypeBriefDto;
import guess.dto.statistics.olap.Olap3dCubeDimensionDto;
import guess.dto.statistics.olap.Olap3dCubeMeasureValueDto;
import guess.dto.statistics.olap.metrics.Olap3dCubeMetricsDto;
import guess.util.LocalizationUtils;
import guess.util.OlapUtils;

import java.util.Comparator;
import java.util.List;

/**
 * OLAP 3D cube statistics DTO.
 */
public record Olap3dCubeStatisticsDto(List<Integer> dimensionValues1, List<Olap3dCubeDimensionDto> dimensionValues2,
                                      List<Olap3dCubeMetricsDto> metricsList) {

    private static List<Olap3dCubeDimensionDto> getCityDimensionValues2(OlapEntityStatistics<?, City, ?> statistics,
                                                                        Language language) {
        return statistics.getDimensionValues2().stream()
                .map(v -> new Olap3dCubeDimensionDto(
                        v.getId(),
                        LocalizationUtils.getString(v.getName(), language)))
                .sorted(Comparator.comparing(Olap3dCubeDimensionDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static List<Olap3dCubeDimensionDto> getEventTypeDimensionValues2(OlapEntityStatistics<?, EventType, ?> statistics,
                                                                             Language language) {
        Comparator<EventTypeBriefDto> comparatorByIsConference = Comparator.comparing(EventTypeBriefDto::isConference).reversed();
        Comparator<EventTypeBriefDto> comparatorByOrganizerName = Comparator.comparing(EventTypeBriefDto::getOrganizerName, String.CASE_INSENSITIVE_ORDER);
        Comparator<EventTypeBriefDto> comparatorByName = Comparator.comparing(EventTypeBriefDto::getName, String.CASE_INSENSITIVE_ORDER);

        return statistics.getDimensionValues2().stream()
                .map(et -> EventTypeBriefDto.convertToBriefDto(et, language))
                .sorted(comparatorByIsConference.thenComparing(comparatorByOrganizerName).thenComparing(comparatorByName))
                .map(et -> new Olap3dCubeDimensionDto(
                        et.getId(),
                        et.getName()))
                .toList();
    }

    public static Olap3dCubeStatisticsDto convertToDto(OlapStatistics olapStatistics, Language language) {
        OlapEntityStatistics<Integer, ? extends Nameable, ? extends Nameable> statistics;
        List<Olap3dCubeDimensionDto> dimensionValues2;

        if (olapStatistics.yearEventTypeStatistics() != null) {
            statistics = olapStatistics.yearEventTypeStatistics();
            dimensionValues2 = getCityDimensionValues2(olapStatistics.yearEventTypeStatistics(), language);
        } else if (olapStatistics.yearSpeakerStatistics() != null) {
            statistics = olapStatistics.yearSpeakerStatistics();
            dimensionValues2 = getEventTypeDimensionValues2(olapStatistics.yearSpeakerStatistics(), language);
        } else if (olapStatistics.yearCompanyStatistics() != null) {
            statistics = olapStatistics.yearCompanyStatistics();
            dimensionValues2 = getEventTypeDimensionValues2(olapStatistics.yearCompanyStatistics(), language);
        } else {
            throw new IllegalArgumentException("Year statistics not found");
        }

        List<Olap3dCubeMetricsDto> metricsList = statistics.getSubMetricsList().stream()
                .map(m -> {
                    List<Olap3dCubeMeasureValueDto> measureValueList = m.measureValues().entrySet().stream()
                            .map(e -> new Olap3dCubeMeasureValueDto(
                                    e.getKey().getId(),
                                    OlapUtils.removeTrailingZeroes(e.getValue()))
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
