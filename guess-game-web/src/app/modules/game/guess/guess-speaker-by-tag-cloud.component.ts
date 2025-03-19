import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { StateService } from '../../../shared/services/state.service';
import { TagCloudSpeakers } from '../../../shared/models/guess/tag-cloud-speakers.model';

@Component({
    selector: 'app-guess-speaker-by-tag-cloud',
    templateUrl: './guess-speaker-by-tag-cloud.component.html',
    standalone: false
})
export class GuessSpeakerByTagCloudComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  private imageSourcePrefix = 'data:image/jpeg;base64,';
  public tagCloudSpeakers: TagCloudSpeakers;
  public title: string;
  public logoImageSource: string;
  public tagCloudImageSource: string;
  public speakerImageSource0: string;
  public speakerImageSource1: string;
  public speakerImageSource2: string;
  public speakerImageSource3: string;

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
    this.stateService.getTagCloudSpeakers(this.language)
      .subscribe(data => {
          if (data) {
            this.tagCloudSpeakers = data;
            this.title =
              `${this.tagCloudSpeakers.questionSetName} (${this.tagCloudSpeakers.currentIndex + 1}/${this.tagCloudSpeakers.totalNumber})`;
            this.tagCloudImageSource = `${this.imageSourcePrefix}${this.tagCloudSpeakers.image}`;
            this.speakerImageSource0 = `${this.speakersImageDirectory}/${this.tagCloudSpeakers.speakerPhotoFileName0}`;
            this.speakerImageSource1 = `${this.speakersImageDirectory}/${this.tagCloudSpeakers.speakerPhotoFileName1}`;
            this.speakerImageSource2 = `${this.speakersImageDirectory}/${this.tagCloudSpeakers.speakerPhotoFileName2}`;
            this.speakerImageSource3 = `${this.speakersImageDirectory}/${this.tagCloudSpeakers.speakerPhotoFileName3}`;

            if (this.tagCloudSpeakers.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.tagCloudSpeakers.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.tagCloudSpeakers.currentIndex, id)
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
