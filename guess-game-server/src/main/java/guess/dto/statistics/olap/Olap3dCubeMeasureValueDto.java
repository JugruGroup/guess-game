package guess.dto.statistics.olap;

import java.util.List;

/**
 * OLAP 3D cube measure value DTO.
 */
public record Olap3dCubeMeasureValueDto(long dimensionId, List<Long> measureValues) {
}
