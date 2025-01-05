import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import {
  ThreeDimensionsCubeOptions
} from '../../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-options.model';

@Component({
    selector: 'app-three-dimensions-zoom-in',
    templateUrl: './three-dimensions-zoom-in.component.html',
    standalone: false
})
export class ThreeDimensionsZoomInComponent implements AfterViewInit {
  public options!: ThreeDimensionsCubeOptions;

  @ViewChild('cubeDiv') cubeDiv!: ElementRef<HTMLDivElement>;

  constructor(public config: DynamicDialogConfig) {
    this.options = config.data.options;
  }

  ngAfterViewInit(): void {
    window.addEventListener('resize', this.onResize);
    this.fillOptions();
  }

  onResize = (): void => {
    this.fillOptions();
  }

  fillOptions() {
    if (this.cubeDiv) {
      const clientWidth = this.cubeDiv.nativeElement.clientWidth;
      const clientHeight = this.cubeDiv.nativeElement.clientHeight;

      if ((clientWidth > 0) && (clientHeight > 0)) {
      const aspectRatio = clientWidth / clientHeight;

      this.options = new ThreeDimensionsCubeOptions(aspectRatio, '100%');
      }
    }
  }
}
