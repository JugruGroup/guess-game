import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { CompanySpeakers } from '../../../shared/models/guess/company-speakers.model';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-speaker-by-company',
    templateUrl: './guess-speaker-by-company.component.html',
    standalone: false
})
export class GuessSpeakerByCompanyComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  private speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public companySpeakers: CompanySpeakers;
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
    this.stateService.getCompanySpeakers(this.language)
      .subscribe(data => {
          if (data) {
            this.companySpeakers = data;
            this.title = `${this.companySpeakers.questionSetName} (${this.companySpeakers.currentIndex + 1}/${this.companySpeakers.totalNumber})`;
            this.imageSource0 = `${this.speakersImageDirectory}/${this.companySpeakers.speakerPhotoFileName0}`;
            this.imageSource1 = `${this.speakersImageDirectory}/${this.companySpeakers.speakerPhotoFileName1}`;
            this.imageSource2 = `${this.speakersImageDirectory}/${this.companySpeakers.speakerPhotoFileName2}`;
            this.imageSource3 = `${this.speakersImageDirectory}/${this.companySpeakers.speakerPhotoFileName3}`;

            if (this.companySpeakers.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.companySpeakers.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.companySpeakers.currentIndex, id)
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
