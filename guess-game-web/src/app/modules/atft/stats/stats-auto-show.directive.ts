import {AfterViewInit, Directive} from '@angular/core';
import {StatsService} from './stats.service';

@Directive({
    selector: '[atft-stats-auto-show]',
    standalone: false
})
export class StatsAutoShowDirective implements AfterViewInit {

  constructor(
    private statsService: StatsService
  ) {

  }

  ngAfterViewInit(): void {
    this.statsService.create();
  }

}
