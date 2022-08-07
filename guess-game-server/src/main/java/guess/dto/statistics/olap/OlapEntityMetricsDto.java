package guess.dto.statistics.olap;

import guess.domain.statistics.olap.OlapEntityMetrics;

import java.util.List;
import java.util.Objects;

/**
 * OLAP entity metrics DTO.
 */
public class OlapEntityMetricsDto {
    private final long id;
    private final String name;
    private final List<Long> measureValues;
    private final List<Long> cumulativeMeasureValues;
    private final Long total;

    public OlapEntityMetricsDto(long id, String name, List<Long> measureValues, List<Long> cumulativeMeasureValues, Long total) {
        this.id = id;
        this.name = name;
        this.measureValues = measureValues;
        this.cumulativeMeasureValues = cumulativeMeasureValues;
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Long> getMeasureValues() {
        return measureValues;
    }

    public List<Long> getCumulativeMeasureValues() {
        return cumulativeMeasureValues;
    }

    public Long getTotal() {
        return total;
    }

    public static OlapEntityMetricsDto convertToDto(OlapEntityMetrics<?> entityMetrics) {
        return new OlapEntityMetricsDto(0, null, entityMetrics.measureValues(), entityMetrics.cumulativeMeasureValues(),
                entityMetrics.total());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OlapEntityMetricsDto that)) return false;
        return getId() == that.getId() && Objects.equals(getName(), that.getName()) && Objects.equals(getMeasureValues(), that.getMeasureValues()) && Objects.equals(getCumulativeMeasureValues(), that.getCumulativeMeasureValues()) && Objects.equals(getTotal(), that.getTotal());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getMeasureValues(), getCumulativeMeasureValues(), getTotal());
    }

    @Override
    public String toString() {
        return "OlapEntityMetricsDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", measureValues=" + measureValues +
                ", cumulativeMeasureValues=" + cumulativeMeasureValues +
                ", total=" + total +
                '}';
    }
}
