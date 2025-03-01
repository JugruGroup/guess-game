import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { SpeakerAccounts } from '../../../shared/models/guess/speaker-accounts.model';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-account-by-speaker',
    templateUrl: './guess-account-by-speaker.component.html',
    standalone: false
})
export class GuessAccountBySpeakerComponent implements OnInit {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public speakerAccounts: SpeakerAccounts;
  public title: string;
  public logoImageSource: string;
  public language: string;

  constructor(private stateService: StateService, private answerService: AnswerService, private router: Router,
              private translateService: TranslateService, private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.loadQuestion();

    this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.loadQuestion();
      });
  }

  loadQuestion() {
    this.stateService.getSpeakerAccounts(this.language)
      .subscribe(data => {
          if (data) {
            this.speakerAccounts = data;
            this.title =
              `${this.speakerAccounts.questionSetName} (${this.speakerAccounts.currentIndex + 1}/${this.speakerAccounts.totalNumber})`;

            if (this.speakerAccounts.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.speakerAccounts.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.speakerAccounts.currentIndex, id)
      .subscribe(() => {
          this.loadQuestion();
        }
      );
  }

  result() {
    this.stateService.setState(GameState.ResultState)
      .subscribe(() => {
          this.router.navigateByUrl(`/${this.language}/game/result`);
        }
      );
  }

  cancel() {
    this.router.navigateByUrl(`/${this.language}/game/cancel`);
  }
}
