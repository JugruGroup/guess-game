import { OlapMeasureValue } from '../olap-measure-value.model';

export class OlapCubeMetrics {
  constructor(
    public id?: number,
    public name?: string,
    public measureValueList?: OlapMeasureValue[]
  ) {
  }
}
