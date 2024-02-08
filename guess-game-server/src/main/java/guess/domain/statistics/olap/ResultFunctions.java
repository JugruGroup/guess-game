package guess.domain.statistics.olap;

import guess.domain.function.QuadFunction;
import guess.domain.function.QuintFunction;
import guess.domain.function.TriFunction;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Result functions.
 */
public record ResultFunctions<T, S, U, W, X, Y, Z>(BiFunction<T, Map<U, List<Long>>, W> resultSubMetricsBiFunction,
                                                   QuadFunction<T, List<Long>, List<Long>, Long, X> resultMetricsQuadFunction,
                                                   TriFunction<List<Long>, List<Long>, Long, Y> totalsTriFunction,
                                                   QuintFunction<List<S>, List<U>, List<W>, List<X>, Y, Z> resultQuintFunction) {
}
