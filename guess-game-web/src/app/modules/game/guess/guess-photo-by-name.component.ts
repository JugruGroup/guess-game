import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { NamePhotos } from '../../../shared/models/guess/name-photos.model';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-picture',
    templateUrl: './guess-photo-by-name.component.html',
    standalone: false
})
export class GuessPhotoByNameComponent implements OnInit {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  private speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public namePhotos: NamePhotos;
  public title: string;
  public logoImageSource: string;
  public imageSource0: string;
  public imageSource1: string;
  public imageSource2: string;
  public imageSource3: string;
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
    this.stateService.getNamePhotos(this.language)
      .subscribe(data => {
          if (data) {
            this.namePhotos = data;
            this.title = `${this.namePhotos.questionSetName} (${this.namePhotos.currentIndex + 1}/${this.namePhotos.totalNumber})`;
            this.imageSource0 = `${this.speakersImageDirectory}/${this.namePhotos.photoFileName0}`;
            this.imageSource1 = `${this.speakersImageDirectory}/${this.namePhotos.photoFileName1}`;
            this.imageSource2 = `${this.speakersImageDirectory}/${this.namePhotos.photoFileName2}`;
            this.imageSource3 = `${this.speakersImageDirectory}/${this.namePhotos.photoFileName3}`;

            if (this.namePhotos.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.namePhotos.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.namePhotos.currentIndex, id)
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
