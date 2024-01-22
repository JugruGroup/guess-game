package guess.dto.statistics.olap.metrics;

import guess.dto.statistics.olap.OlapMeasureValueDto;

import java.util.List;

/**
 * OLAP cube metrics DTO.
 */
public record OlapCubeMetricsDto(long id, String name, List<OlapMeasureValueDto> measureValueList) {
}
