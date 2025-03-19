import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-information',
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>',
  standalone: true
})
export class InformationComponent {
}
