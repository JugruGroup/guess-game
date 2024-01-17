import { AfterViewChecked, Component, Input, OnChanges, SimpleChange, SimpleChanges, ViewChild } from '@angular/core';
// import { OrbitControlsComponent, RendererCanvasComponent } from 'atft';
import {
  ThreeDimensionsCubeDataset
} from '../../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-dataset.model';
import {
  ThreeDimensionsCubeData
} from '../../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-data.model';
import {
  ThreeDimensionsPointLight
} from '../../../../../shared/models/statistics/olap/three-dimensions/three-dimensions-point-light.model';
import {
  ThreeDimensionsCubeInternalData
} from '../../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-internal-data.model';
import {
  ThreeDimensionsCubeOptions
} from '../../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-options.model';
import {
  ThreeDimensionsSphereGroup
} from '../../../../../shared/models/statistics/olap/three-dimensions/sphere/three-dimensions-sphere-group.model';
import {
  ThreeDimensionsSphereGroupItem
} from '../../../../../shared/models/statistics/olap/three-dimensions/sphere/three-dimensions-sphere-group-item.model';
import {
  ThreeDimensionsTextGroup
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-group.model';
import {
  ThreeDimensionsTextGroupItem
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-group-item.model';
import {
  ThreeDimensionsTextAlignment
} from '../../../../../shared/models/statistics/olap/three-dimensions/text/three-dimensions-text-alignment.model';

@Component({
  selector: 'app-three-dimensions-cube',
  templateUrl: './three-dimensions-cube.component.html'
})
export class ThreeDimensionsCubeComponent implements OnChanges, AfterViewChecked {
  public readonly ORBIT_CONTROLS_ROTATE_SPEED = 1;
  public readonly ORBIT_CONTROLS_ZOOM_SPEED = 1.2;

  public readonly PERSPECTIVE_CAMERA_FOV_DEGREES = 30;
  public readonly PERSPECTIVE_CAMERA_NEAR = 1;
  public readonly PERSPECTIVE_CAMERA_FAR = 1100;
  public readonly PERSPECTIVE_CAMERA_POSITION_X = 0;
  public readonly PERSPECTIVE_CAMERA_POSITION_Y = 0;

  public readonly POINT_LIGHT_INTENSITY = 50000;
  public readonly POINT_LIGHT_DISTANCE = 1000;
  private readonly DEFAULT_POINT_LIGHT_FACTOR = 60;

  private readonly GRID_STEP = 10;

  private readonly MAX_SPHERE_RADIUS = 4;
  public readonly SPHERE_SEGMENTS = 16;

  public readonly DATASET_LABEL_TEXT_SIZE = 3;
  public readonly X_LABEL_TEXT_SIZE = 2;
  public readonly Y_LABEL_TEXT_SIZE = 2;
  public readonly VALUE_LABEL_TEXT_SIZE = 2;
  public readonly RECT_TEXT_INDENT = this.MAX_SPHERE_RADIUS + 2;
  public readonly DIAG_TEXT_INDENT = this.MAX_SPHERE_RADIUS;
  public readonly TEXT_MATERIAL_COLOR = '#888888';
  public readonly TEXT_BEVEL_ENABLED = true;
  public readonly TEXT_CURVE_SEGMENTS = 2;

  @Input() public options!: ThreeDimensionsCubeOptions;
  @Input() public data!: ThreeDimensionsCubeData;

  // @ViewChild('orbitControls') orbitControls!: OrbitControlsComponent;
  // @ViewChild('rendererCanvas') rendererCanvas!: RendererCanvasComponent;

  private heightUpdated = false;

  public perspectiveCameraPositionZ: number = 0;
  public internalData: ThreeDimensionsCubeInternalData = new ThreeDimensionsCubeInternalData(
    [], 100, 10, 100, 10, 100, 10, [], []);

  public ngOnChanges(changes: SimpleChanges) {
    const optionsChange: SimpleChange = changes['options'];

    if (optionsChange) {
      const currentOptions: ThreeDimensionsCubeOptions = optionsChange.currentValue;

      if (currentOptions) {
        this.applyOptions(currentOptions);
      }
    }

    const dataChange: SimpleChange = changes['data'];

    if (dataChange) {
      const currentData: ThreeDimensionsCubeData = dataChange.currentValue;

      if (currentData) {
        this.applyData(currentData);
      }
    }
  }

  applyOptions(options: ThreeDimensionsCubeOptions) {
    if (options?.height) {
      this.perspectiveCameraPositionZ = this.getPositionZ(this.internalData.gridSizeX, this.internalData.gridSizeY, this.internalData.gridSizeZ);
      this.heightUpdated = true;
    }
  }

  ngAfterViewChecked() {
    if (this.heightUpdated) {
      // if (this.rendererCanvas) {
      //   this.rendererCanvas.onResize(null as unknown as Event);
      // }

      this.heightUpdated = false;
    }
  }

  getPositionZ(gridSizeX: number, gridSizeY: number, gridSizeZ: number): number {
    const sizeX = gridSizeX + this.GRID_STEP * 4;
    const sizeY = gridSizeY + (this.RECT_TEXT_INDENT + this.X_LABEL_TEXT_SIZE) * 2;
    const sizeZ = gridSizeZ / 2;
    const aspectRatio = sizeX / sizeY;
    const size = (this.options.aspectRatio >= aspectRatio) ? sizeY : sizeX / this.options.aspectRatio;
    const halfSize = size / 2;

    return halfSize / Math.tan(this.deg2rad(this.PERSPECTIVE_CAMERA_FOV_DEGREES / 2)) + sizeZ;
  }

  applyData(data: ThreeDimensionsCubeData): void {
    if (data?.datasets) {
      const countX = (data.labelsX) ? data.labelsX.length : 0;
      const countY = (data.labelsY) ? data.labelsY.length : 0;
      const countZ = (data.datasets) ? data.datasets.length : 0;

      // Point lights
      const plfX = this.DEFAULT_POINT_LIGHT_FACTOR / 10 * countX;
      const plfY = this.DEFAULT_POINT_LIGHT_FACTOR / 10 * countY;
      const plfZ = this.DEFAULT_POINT_LIGHT_FACTOR / 10 * countZ;
      const pointLights = [
        new ThreeDimensionsPointLight(-plfX, plfY, plfZ),
        new ThreeDimensionsPointLight(-plfX, -plfY, -plfZ),
        new ThreeDimensionsPointLight(plfX, plfY, -plfZ),
        new ThreeDimensionsPointLight(plfX, -plfY, plfZ)];

      // Grid
      const gridDivisionsX = Math.max(countX - 1, 0);
      const gridSizeX = gridDivisionsX * this.GRID_STEP;
      const gridDivisionsY = Math.max(countY - 1, 0);
      const gridSizeY = gridDivisionsY * this.GRID_STEP;
      const gridDivisionsZ = Math.max(countZ - 1, 0);
      const gridSizeZ = gridDivisionsZ * this.GRID_STEP;

      // Internal data
      let sphereGroupItems: ThreeDimensionsSphereGroupItem[] = [];
      const sphereGroups: ThreeDimensionsSphereGroup[] = [];
      const textGroups: ThreeDimensionsTextGroup[] = [];
      const maxValue = this.getMaxValue(data.datasets, countX, countY, countZ);
      const offsetX = this.getOffset(countX);
      const offsetY = this.getOffset(countY);
      const offsetZ = this.getOffset(countZ);
      const sphereOffsetX = -offsetX;
      const sphereOffsetY = offsetY;
      const sphereOffsetZ = offsetZ;
      const notSingleDataset = (data.datasets.length > 1);

      this.iterateCubeDatasets(data.datasets, countX, countY, countZ,
        (value, dataset, index1, index2, index3) => {
          const radius = this.getRadius(value / maxValue);

          // Fill spheres
          sphereGroupItems.push(new ThreeDimensionsSphereGroupItem(
            radius,
            sphereOffsetX + index3 * this.GRID_STEP,
            sphereOffsetY - index2 * this.GRID_STEP,
            sphereOffsetZ - index1 * this.GRID_STEP));

          // Fill texts (values)
          textGroups.push(new ThreeDimensionsTextGroup(
            value.toString(), this.VALUE_LABEL_TEXT_SIZE,
            [
              new ThreeDimensionsTextGroupItem(
                sphereOffsetX + index3 * this.GRID_STEP,
                sphereOffsetY - index2 * this.GRID_STEP,
                sphereOffsetZ - index1 * this.GRID_STEP + radius,
                0, 0, 0),
              new ThreeDimensionsTextGroupItem(
                sphereOffsetX + index3 * this.GRID_STEP,
                sphereOffsetY - index2 * this.GRID_STEP,
                sphereOffsetZ - index1 * this.GRID_STEP - radius,
                0, this.deg2rad(180), 0)
            ]
          ));
        },
        (dataset: ThreeDimensionsCubeDataset, index1) => {
          // Fill spheres
          sphereGroups.push(new ThreeDimensionsSphereGroup(dataset.color, sphereGroupItems));
          sphereGroupItems = [];

          // Fill texts (first dataset labels)
          const items: ThreeDimensionsTextGroupItem[] = [];

          items.push(new ThreeDimensionsTextGroupItem(
            -offsetX - this.RECT_TEXT_INDENT, -offsetY, offsetZ - index1 * this.GRID_STEP,
            this.deg2rad(-90), 0, 0,
            ThreeDimensionsTextAlignment.Left));

          if (notSingleDataset) {
            // Fill texts (second dataset labels)
            items.push(new ThreeDimensionsTextGroupItem(
              offsetX + this.RECT_TEXT_INDENT, -offsetY, offsetZ - index1 * this.GRID_STEP,
              this.deg2rad(-90), 0, this.deg2rad(180),
              ThreeDimensionsTextAlignment.Left));
          }

          textGroups.push(new ThreeDimensionsTextGroup(dataset.label, this.DATASET_LABEL_TEXT_SIZE, items));
        });

      for (let i = 0; i < data.labelsX.length; i++) {
        const items: ThreeDimensionsTextGroupItem[] = [];

        // Fill texts (first X labels)
        items.push(new ThreeDimensionsTextGroupItem(
          -offsetX + i * this.GRID_STEP, -offsetY - this.RECT_TEXT_INDENT, offsetZ,
          0, 0, 0));

        // Fill texts (second X labels)
        if (notSingleDataset) {
          items.push(new ThreeDimensionsTextGroupItem(
            -offsetX + i * this.GRID_STEP, -offsetY - this.RECT_TEXT_INDENT, -offsetZ,
            0, this.deg2rad(180), 0));
        }

        textGroups.push(new ThreeDimensionsTextGroup(data.labelsX[i], this.X_LABEL_TEXT_SIZE, items));
      }

      for (let i = 0; i < data.labelsY.length; i++) {
        const items: ThreeDimensionsTextGroupItem[] = [];

        // Fill texts (first Y labels)
        items.push(new ThreeDimensionsTextGroupItem(
          offsetX + this.DIAG_TEXT_INDENT, offsetY - i * this.GRID_STEP, offsetZ + this.DIAG_TEXT_INDENT,
          0, this.deg2rad(-45), 0,
          ThreeDimensionsTextAlignment.Right));

        // Fill texts (second Y labels)
        if (notSingleDataset) {
          items.push(new ThreeDimensionsTextGroupItem(
            -offsetX - this.DIAG_TEXT_INDENT, offsetY - i * this.GRID_STEP, -offsetZ - this.DIAG_TEXT_INDENT,
            0, this.deg2rad(135), 0,
            ThreeDimensionsTextAlignment.Right));
        }

        textGroups.push(new ThreeDimensionsTextGroup(data.labelsY[i], this.Y_LABEL_TEXT_SIZE, items));
      }

      this.perspectiveCameraPositionZ = this.getPositionZ(gridSizeX, gridSizeY, gridSizeZ);
      this.internalData = new ThreeDimensionsCubeInternalData(
        pointLights,
        gridSizeX, gridDivisionsX, gridSizeY, gridDivisionsY, gridSizeZ, gridDivisionsZ,
        sphereGroups, textGroups);

      // if (this.orbitControls) {
      //   this.orbitControls.reset();
      // }
    }
  }

  getOffset(count: number): number {
    return (Math.max(count, 1) - 1) * this.GRID_STEP / 2;
  }

  iterateCubeDatasets(datasets: ThreeDimensionsCubeDataset[], xCount: number, yCount: number, zCount: number,
                      valueCallback: (value: number, dataset: ThreeDimensionsCubeDataset,
                                      index1: number, index2: number, index3: number) => void,
                      datasetCallback: (dataset: ThreeDimensionsCubeDataset, index1: number) => void = () => {
                      }) {
    for (let i = 0; i < Math.min(zCount, datasets.length); i++) {
      const dataset = datasets[i];

      if (dataset.data) {
        for (let j = 0; j < Math.min(yCount, dataset.data.length); j++) {
          const values = dataset.data[j];

          if (values) {
            for (let k = 0; k < Math.min(xCount, values.length); k++) {
              const value = values[k];

              if (value && value > 0) {
                valueCallback(value, dataset, i, j, k);
              }
            }
          }
        }
      }

      datasetCallback(dataset, i);
    }
  }

  getMaxValue(datasets: ThreeDimensionsCubeDataset[], xCount: number, yCount: number, zCount: number): number {
    let result = 0;

    this.iterateCubeDatasets(datasets, xCount, yCount, zCount, value => {
      if (value > result) {
        result = value;
      }
    });

    return result;
  }

  getRadius(factor: number): number {
    const maxVolume = 4 * Math.PI / 3 * Math.pow(this.MAX_SPHERE_RADIUS, 3);
    const volume = maxVolume * factor;

    return Math.cbrt(volume * 3 / 4 / Math.PI);
  }

  deg2rad(degrees: number) {
    return (degrees / 180) * Math.PI;
  }
}
