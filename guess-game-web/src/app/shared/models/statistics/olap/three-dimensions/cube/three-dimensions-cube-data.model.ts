import { ThreeDimensionsCubeDataset } from './three-dimensions-cube-dataset.model';

export class ThreeDimensionsCubeData {
  constructor(
    public labelsX: string[],
    public labelsY: string[],
    public datasets: ThreeDimensionsCubeDataset[]
  ) {
  }
}
