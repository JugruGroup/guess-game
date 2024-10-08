import { OlapCubeType } from '../olap-cube-type.model';
import { OlapCommonParameters } from './olap-common-parameters.model';
import { OlapMeasureType } from '../olap-measure-type.model';

export class OlapParameters extends OlapCommonParameters {
    constructor(
        public cubeType?: OlapCubeType,
        public measureType?: OlapMeasureType,
        public conferences?: boolean,
        public meetups?: boolean,
        public organizerId?: number,
        public eventTypeIds?: number[],
        public speakerIds?: number[],
        public companyIds?: number[]
    ) {
        super(cubeType, measureType, conferences, meetups, organizerId, eventTypeIds);
    }
}
