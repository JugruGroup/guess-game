import { EventPlaceMetrics } from './event-place-metrics.model';

export class EventPlaceStatistics {
  constructor(
    public eventPlaceMetricsList?: EventPlaceMetrics[],
    public totals?: EventPlaceMetrics
  ) {
  }
}
