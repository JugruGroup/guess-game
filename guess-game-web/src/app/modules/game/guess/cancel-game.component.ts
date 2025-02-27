import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { StateService } from '../../../shared/services/state.service';

@Component({
  selector: 'app-cancel-game',
  templateUrl: './cancel-game.component.html',
  standalone: false
})
export class CancelGameComponent implements OnInit {
  public language: string;

  constructor(private stateService: StateService, private router: Router, private translateService: TranslateService,
              private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
      });
  }

  cancel() {
    this.stateService.setState(GameState.ResultState)
      .subscribe(() => {
          this.router.navigateByUrl(`/${this.language}/game/result`);
        }
      );
  }
}
