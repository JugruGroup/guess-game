import { OlapCubeType } from '../olap-cube-type.model';
import { OlapMeasureType } from '../olap-measure-type.model';

export class OlapCityParameters {
  constructor(
    public cubeType?: OlapCubeType,
    public measureType?: OlapMeasureType,
    public eventTypeId?: number
  ) {
  }
}
