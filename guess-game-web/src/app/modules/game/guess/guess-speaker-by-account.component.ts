import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AccountSpeakers } from '../../../shared/models/guess/account-speakers.model';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-speaker-by-account',
    templateUrl: './guess-speaker-by-account.component.html',
    standalone: false
})
export class GuessSpeakerByAccountComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public accountSpeakers: AccountSpeakers;
  public title: string;
  public logoImageSource: string;
  public imageSource0: string;
  public imageSource1: string;
  public imageSource2: string;
  public imageSource3: string;

  public language: string;
  private languageSubscription: Subscription;

  constructor(private stateService: StateService, private answerService: AnswerService, private router: Router,
              private translateService: TranslateService, private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.loadQuestion();

    this.languageSubscription = this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.loadQuestion();
      });
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  loadQuestion() {
    this.stateService.getAccountSpeakers(this.language)
      .subscribe(data => {
          if (data) {
            this.accountSpeakers = data;
            this.title =
              `${this.accountSpeakers.questionSetName} (${this.accountSpeakers.currentIndex + 1}/${this.accountSpeakers.totalNumber})`;
            this.imageSource0 = `${this.speakersImageDirectory}/${this.accountSpeakers.speakerPhotoFileName0}`;
            this.imageSource1 = `${this.speakersImageDirectory}/${this.accountSpeakers.speakerPhotoFileName1}`;
            this.imageSource2 = `${this.speakersImageDirectory}/${this.accountSpeakers.speakerPhotoFileName2}`;
            this.imageSource3 = `${this.speakersImageDirectory}/${this.accountSpeakers.speakerPhotoFileName3}`;

            if (this.accountSpeakers.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.accountSpeakers.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.accountSpeakers.currentIndex, id)
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
