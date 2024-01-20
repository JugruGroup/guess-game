import { OlapEntityStatistics } from './olap-entity-statistics.model';
import { OlapEventTypeMetrics } from '../metrics/olap-event-type-metrics.model';
import { OlapSpeakerMetrics } from '../metrics/olap-speaker-metrics.model';
import { OlapCompanyMetrics } from '../metrics/olap-company-metrics.model';
import { OlapCubeStatistics } from './olap-cube-statistics.model';
import { OlapEntityMetrics } from '../metrics/olap-entity-metrics.model';

export class OlapStatistics {
  constructor(
    public eventTypeStatistics?: OlapEntityStatistics<number, OlapEventTypeMetrics>,
    public speakerStatistics?: OlapEntityStatistics<number, OlapSpeakerMetrics>,
    public companyStatistics?: OlapEntityStatistics<number, OlapCompanyMetrics>,
    public topicStatistics?: OlapEntityStatistics<string, OlapEntityMetrics>,
    public cubeStatistics?: OlapCubeStatistics
  ) {
  }
}
