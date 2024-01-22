import { OlapCubeDimension } from '../olap-cube-dimension.model';
import { OlapCubeMetrics } from '../metrics/olap-cube-metrics.model';

export class OlapCubeStatistics {
  constructor(
    public dimensionValues1?: number[],
    public dimensionValues2?: OlapCubeDimension[],
    public metricsList?: OlapCubeMetrics[]
  ) {
  }
}
