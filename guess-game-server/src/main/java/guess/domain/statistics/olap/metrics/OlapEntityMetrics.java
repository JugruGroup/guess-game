package guess.domain.statistics.olap.metrics;

import java.util.List;

/**
 * OLAP entity metrics.
 */
public record OlapEntityMetrics<T>(T entity, List<Long> measureValues, List<Long> cumulativeMeasureValues, long total) {
}
