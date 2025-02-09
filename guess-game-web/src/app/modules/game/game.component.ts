import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-game',
  imports: [RouterOutlet],
  templateUrl: './game.component.html',
  standalone: true
})
export class GameComponent {
}
