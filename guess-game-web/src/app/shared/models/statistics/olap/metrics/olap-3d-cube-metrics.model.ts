import { Olap3dCubeMeasureValue } from '../olap-3d-cube-measure-value.model';

export class Olap3dCubeMetrics {
  constructor(
    public id?: number,
    public name?: string,
    public measureValueList?: Olap3dCubeMeasureValue[]
  ) {
  }
}
