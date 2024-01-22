import { OlapCubeType } from '../olap-cube-type.model';
import { OlapMeasureType } from '../olap-measure-type.model';

export class OlapCommonParameters {
    constructor(
        public cubeType?: OlapCubeType,
        public measureType?: OlapMeasureType,
        public conferences?: boolean,
        public meetups?: boolean,
        public organizerId?: number,
        public eventTypeIds?: number[]
    ) {
    }
}
