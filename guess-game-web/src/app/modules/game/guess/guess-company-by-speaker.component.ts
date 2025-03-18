import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { SpeakerCompanies } from '../../../shared/models/guess/speaker-companies.model';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-company-by-speaker',
    templateUrl: './guess-company-by-speaker.component.html',
    standalone: false
})
export class GuessCompanyBySpeakerComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public speakerCompanies: SpeakerCompanies;
  public title: string;
  public logoImageSource: string;

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
    this.stateService.getSpeakerCompanies(this.language)
      .subscribe(data => {
          if (data) {
            this.speakerCompanies = data;
            this.title = `${this.speakerCompanies.questionSetName} (${this.speakerCompanies.currentIndex + 1}/${this.speakerCompanies.totalNumber})`;

            if (this.speakerCompanies.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.speakerCompanies.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.speakerCompanies.currentIndex, id)
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
