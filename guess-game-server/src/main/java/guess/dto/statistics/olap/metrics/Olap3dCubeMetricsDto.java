package guess.dto.statistics.olap.metrics;

import guess.dto.statistics.olap.Olap3dCubeMeasureValueDto;

import java.util.List;

/**
 * OLAP 3D cube metrics DTO.
 */
public record Olap3dCubeMetricsDto(long id, String name, List<Olap3dCubeMeasureValueDto> measureValueList) {
}
