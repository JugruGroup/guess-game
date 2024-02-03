package guess.dto.statistics.olap.parameters;

import guess.domain.statistics.olap.CubeType;
import guess.domain.statistics.olap.MeasureType;

/**
 * OLAP basic parameters DTO.
 */
public abstract class OlapBasicParametersDto {
    private CubeType cubeType;
    private MeasureType measureType;

    public CubeType getCubeType() {
        return cubeType;
    }

    public void setCubeType(CubeType cubeType) {
        this.cubeType = cubeType;
    }

    public MeasureType getMeasureType() {
        return measureType;
    }

    public void setMeasureType(MeasureType measureType) {
        this.measureType = measureType;
    }
}
