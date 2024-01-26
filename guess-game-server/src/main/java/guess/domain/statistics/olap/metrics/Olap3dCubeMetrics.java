package guess.domain.statistics.olap.metrics;

import guess.domain.Identifier;
import guess.domain.statistics.olap.Olap3dCubeMeasureValue;

import java.util.List;

/**
 * OLAP 3D cube metrics.
 */
public class Olap3dCubeMetrics extends Identifier {
    private final String name;
    private final List<Olap3dCubeMeasureValue> measureValueList;

    public Olap3dCubeMetrics(long id, String name, List<Olap3dCubeMeasureValue> measureValueList) {
        super(id);

        this.name = name;
        this.measureValueList = measureValueList;
    }

    public String getName() {
        return name;
    }

    public List<Olap3dCubeMeasureValue> getMeasureValueList() {
        return measureValueList;
    }
}
