import { OlapCompanyMetrics } from '../metrics/olap-company-metrics.model';
import { Olap3dCubeStatistics } from './olap-3d-cube-statistics.model';
import { OlapEntityMetrics } from '../metrics/olap-entity-metrics.model';
import { OlapEntityStatistics } from './olap-entity-statistics.model';
import { OlapEventTypeMetrics } from '../metrics/olap-event-type-metrics.model';
import { OlapSpeakerMetrics } from '../metrics/olap-speaker-metrics.model';

export class OlapStatistics {
  constructor(
    public eventTypeStatistics?: OlapEntityStatistics<number, OlapEventTypeMetrics>,
    public speakerStatistics?: OlapEntityStatistics<number, OlapSpeakerMetrics>,
    public companyStatistics?: OlapEntityStatistics<number, OlapCompanyMetrics>,
    public topicStatistics?: OlapEntityStatistics<string, OlapEntityMetrics>,
    public cubeStatistics?: Olap3dCubeStatistics
  ) {
  }
}
