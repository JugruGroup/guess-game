import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-companies',
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>',
  standalone: true
})
export class CompaniesComponent {
}
