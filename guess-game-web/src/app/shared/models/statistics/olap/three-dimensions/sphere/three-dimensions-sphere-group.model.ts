import { ThreeDimensionsSphereGroupItem } from './three-dimensions-sphere-group-item.model';

export class ThreeDimensionsSphereGroup {
  constructor(
    public color: string | number,
    public items: ThreeDimensionsSphereGroupItem[]
  ) {
  }
}
