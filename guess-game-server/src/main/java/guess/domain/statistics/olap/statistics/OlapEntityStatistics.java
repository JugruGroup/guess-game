package guess.domain.statistics.olap.statistics;

import guess.domain.statistics.olap.metrics.OlapEntityMetrics;

import java.util.List;
import java.util.Objects;

/**
 * OLAP entity statistics.
 */
public class OlapEntityStatistics<T, S> {
    private final List<T> dimensionValues1;
    private List<OlapEntityMetrics<S>> metricsList;
    private final OlapEntityMetrics<Void> totals;

    public OlapEntityStatistics(List<T> dimensionValues1, List<OlapEntityMetrics<S>> metricsList, OlapEntityMetrics<Void> totals) {
        this.dimensionValues1 = dimensionValues1;
        this.metricsList = metricsList;
        this.totals = totals;
    }

    public List<T> getDimensionValues1() {
        return dimensionValues1;
    }

    public List<OlapEntityMetrics<S>> getMetricsList() {
        return metricsList;
    }

    public void setMetricsList(List<OlapEntityMetrics<S>> metricsList) {
        this.metricsList = metricsList;
    }

    public OlapEntityMetrics<Void> getTotals() {
        return totals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OlapEntityStatistics)) return false;
        OlapEntityStatistics<?, ?> that = (OlapEntityStatistics<?, ?>) o;
        return Objects.equals(getDimensionValues1(), that.getDimensionValues1()) && Objects.equals(getMetricsList(), that.getMetricsList()) && Objects.equals(getTotals(), that.getTotals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDimensionValues1(), getMetricsList(), getTotals());
    }

    @Override
    public String toString() {
        return "OlapEntityStatistics{" +
                "dimensionValues=" + dimensionValues1 +
                ", metricsList=" + metricsList +
                ", totals=" + totals +
                '}';
    }
}
