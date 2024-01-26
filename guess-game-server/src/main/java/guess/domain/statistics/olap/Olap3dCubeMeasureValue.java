package guess.domain.statistics.olap;

import java.util.List;

/**
 * OLAP 3D cube measure value.
 */
public record Olap3dCubeMeasureValue(long dimensionId, List<Long> measureValues) {
}
