import { OlapEntityMetrics } from '../metrics/olap-entity-metrics.model';

export abstract class OlapEntityStatistics<T, S extends OlapEntityMetrics> {
  protected constructor(
    public dimensionValues?: T[],
    public metricsList?: S[],
    public totals?: OlapEntityMetrics
  ) {
  }
}
