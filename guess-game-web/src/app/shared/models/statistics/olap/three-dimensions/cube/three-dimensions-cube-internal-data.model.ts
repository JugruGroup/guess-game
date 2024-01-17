import { ThreeDimensionsPointLight } from '../three-dimensions-point-light.model';
import { ThreeDimensionsSphereGroup } from '../sphere/three-dimensions-sphere-group.model';
import { ThreeDimensionsTextGroup } from '../text/three-dimensions-text-group.model';

export class ThreeDimensionsCubeInternalData {
  constructor(
    public pointLights: ThreeDimensionsPointLight[],
    public gridSizeX: number,
    public gridDivisionsX: number,
    public gridSizeY: number,
    public gridDivisionsY: number,
    public gridSizeZ: number,
    public gridDivisionsZ: number,
    public sphereGroups: ThreeDimensionsSphereGroup[],
    public textGroups: ThreeDimensionsTextGroup[]
  ) {
  }
}
