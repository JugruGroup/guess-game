import {AfterViewInit, Directive} from '@angular/core';
import {RaycasterService} from './raycaster.service';

@Directive({
    selector: '[atft-raycaster-enable]',
    standalone: false
})
export class RaycasterEnableDirective implements AfterViewInit {

  constructor(
    private raycasterService: RaycasterService
  ) {

  }

  ngAfterViewInit(): void {
    this.raycasterService.enable();
  }

}
