import { Olap3dCubeDimension } from '../olap-3d-cube-dimension.model';
import { Olap3dCubeMetrics } from '../metrics/olap-3d-cube-metrics.model';

export class Olap3dCubeStatistics {
  constructor(
    public dimensionValues1?: number[],
    public dimensionValues2?: Olap3dCubeDimension[],
    public metricsList?: Olap3dCubeMetrics[]
  ) {
  }
}
