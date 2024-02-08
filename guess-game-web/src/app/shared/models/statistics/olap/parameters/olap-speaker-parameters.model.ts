import { OlapCubeType } from '../olap-cube-type.model';
import { OlapMeasureType } from '../olap-measure-type.model';

export class OlapSpeakerParameters {
  constructor(
    public cubeType?: OlapCubeType,
    public measureType?: OlapMeasureType,
    public companyId?: number,
    public eventTypeId?: number
  ) {
  }
}
