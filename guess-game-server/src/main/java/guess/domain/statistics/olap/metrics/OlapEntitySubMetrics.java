package guess.domain.statistics.olap.metrics;

import java.util.List;
import java.util.Map;

/**
 * OLAP entity sub metrics.
 */
public record OlapEntitySubMetrics<T, S>(T entity, Map<S, List<Long>> measureValues) {
}
