package guess.domain.statistics.olap.statistics;

import guess.domain.statistics.olap.Olap3dCubeDimension;
import guess.domain.statistics.olap.metrics.Olap3dCubeMetrics;

import java.util.List;

/**
 * OLAP 3D cube statistics.
 */
public record Olap3dCubeStatistics(List<Integer> dimensionValues1, List<Olap3dCubeDimension> dimensionValues2,
                                   List<Olap3dCubeMetrics> metricsList) {
}
