import { ThreeDimensionsTextAlignment } from './three-dimensions-text-alignment.model';

export class ThreeDimensionsTextGroupItem {
  constructor(
    public translateX: number,
    public translateY: number,
    public translateZ: number,
    public rotateX: number,
    public rotateY: number,
    public rotateZ: number,
    public alignX: ThreeDimensionsTextAlignment = ThreeDimensionsTextAlignment.Center,
    public alignY: ThreeDimensionsTextAlignment = ThreeDimensionsTextAlignment.Center,
    public alignZ: ThreeDimensionsTextAlignment = ThreeDimensionsTextAlignment.Center
  ) {
  }
}
