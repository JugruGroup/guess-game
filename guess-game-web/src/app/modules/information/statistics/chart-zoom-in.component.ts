import { Component } from '@angular/core';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-chart-zoom-in',
  templateUrl: './chart-zoom-in.component.html'
})
export class ChartZoomInComponent {
  public type: string;
  public plugins: any[] = [];
  public data: any = {};
  public options: any = {};

  constructor(config: DynamicDialogConfig) {
    this.type = config.data.type;
    this.plugins = config.data.plugins;
    this.data = config.data.data;
    this.options = config.data.options;
  }
}
