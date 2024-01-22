package guess.dto.statistics.olap;

import java.util.List;

/**
 * OLAP measure value DTO.
 */
public record OlapMeasureValueDto(long dimensionId, List<Long> measureValues) {
}
