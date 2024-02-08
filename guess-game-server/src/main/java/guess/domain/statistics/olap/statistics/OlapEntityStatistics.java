package guess.domain.statistics.olap.statistics;

import guess.domain.statistics.olap.metrics.OlapEntityMetrics;
import guess.domain.statistics.olap.metrics.OlapEntitySubMetrics;

import java.util.List;
import java.util.Objects;

/**
 * OLAP entity statistics.
 */
public class OlapEntityStatistics<T, S, U> {
    private final List<T> dimensionValues1;
    private List<S> dimensionValues2;
    private List<OlapEntitySubMetrics<U, S>> subMetricsList;
    private List<OlapEntityMetrics<U>> metricsList;
    private final OlapEntityMetrics<Void> totals;

    public OlapEntityStatistics(List<T> dimensionValues1, List<S> dimensionValues2, List<OlapEntitySubMetrics<U, S>> subMetricsList,
                                List<OlapEntityMetrics<U>> metricsList, OlapEntityMetrics<Void> totals) {
        this.dimensionValues1 = dimensionValues1;
        this.dimensionValues2 = dimensionValues2;
        this.subMetricsList = subMetricsList;
        this.metricsList = metricsList;
        this.totals = totals;
    }

    public List<T> getDimensionValues1() {
        return dimensionValues1;
    }

    public List<S> getDimensionValues2() {
        return dimensionValues2;
    }

    public void setDimensionValues2(List<S> dimensionValues2) {
        this.dimensionValues2 = dimensionValues2;
    }

    public List<OlapEntitySubMetrics<U, S>> getSubMetricsList() {
        return subMetricsList;
    }

    public void setSubMetricsList(List<OlapEntitySubMetrics<U, S>> subMetricsList) {
        this.subMetricsList = subMetricsList;
    }

    public List<OlapEntityMetrics<U>> getMetricsList() {
        return metricsList;
    }

    public void setMetricsList(List<OlapEntityMetrics<U>> metricsList) {
        this.metricsList = metricsList;
    }

    public OlapEntityMetrics<Void> getTotals() {
        return totals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OlapEntityStatistics)) return false;
        OlapEntityStatistics<?, ?, ?> that = (OlapEntityStatistics<?, ?, ?>) o;
        return Objects.equals(getDimensionValues1(), that.getDimensionValues1()) &&
                Objects.equals(getDimensionValues2(), that.getDimensionValues2()) &&
                Objects.equals(getSubMetricsList(), that.getSubMetricsList()) &&
                Objects.equals(getMetricsList(), that.getMetricsList()) &&
                Objects.equals(getTotals(), that.getTotals());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDimensionValues1(), getDimensionValues2(), getSubMetricsList(), getMetricsList(), getTotals());
    }

    @Override
    public String toString() {
        return "OlapEntityStatistics{" +
                "dimensionValues1=" + dimensionValues1 +
                ", dimensionValues2=" + dimensionValues2 +
                ", subMetricsList=" + subMetricsList +
                ", metricsList=" + metricsList +
                ", totals=" + totals +
                '}';
    }
}
