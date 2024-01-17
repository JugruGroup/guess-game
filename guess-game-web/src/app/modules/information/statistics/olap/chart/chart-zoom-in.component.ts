import { Component } from '@angular/core';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-chart-zoom-in',
  templateUrl: './chart-zoom-in.component.html'
})
export class ChartZoomInComponent {
  constructor(public config: DynamicDialogConfig) {
  }
}
