import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { PhotoNames } from '../../../shared/models/guess/photo-names.model';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-name',
    templateUrl: './guess-name-by-photo.component.html',
    standalone: false
})
export class GuessNameByPhotoComponent implements OnInit {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  private speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public photoNames: PhotoNames;
  public title: string;
  public logoImageSource: string;
  public imageSource: string;
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
    this.stateService.getPhotoNames(this.language)
      .subscribe(data => {
          if (data) {
            this.photoNames = data;
            this.title = `${this.photoNames.questionSetName} (${this.photoNames.currentIndex + 1}/${this.photoNames.totalNumber})`;
            this.imageSource = `${this.speakersImageDirectory}/${this.photoNames.photoFileName}`;

            if (this.photoNames.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.photoNames.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.photoNames.currentIndex, id)
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
