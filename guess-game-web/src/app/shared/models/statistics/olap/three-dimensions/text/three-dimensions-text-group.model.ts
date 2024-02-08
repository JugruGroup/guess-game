import { ThreeDimensionsTextGroupItem } from './three-dimensions-text-group-item.model';

export class ThreeDimensionsTextGroup {
  constructor(
    public text: string,
    public size: number,
    public items: ThreeDimensionsTextGroupItem[]
  ) {
  }
}
