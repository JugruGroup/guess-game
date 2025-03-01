import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { StateService } from '../../../shared/services/state.service';
import { TalkSpeakers } from '../../../shared/models/guess/talk-speakers.model';

@Component({
    selector: 'app-guess-speaker-by-talk',
    templateUrl: './guess-speaker-by-talk.component.html',
    standalone: false
})
export class GuessSpeakerByTalkComponent implements OnInit {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  private speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public talkSpeakers: TalkSpeakers;
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
    this.stateService.getTalkSpeakers(this.language)
      .subscribe(data => {
          if (data) {
            this.talkSpeakers = data;
            this.title = `${this.talkSpeakers.questionSetName} (${this.talkSpeakers.currentIndex + 1}/${this.talkSpeakers.totalNumber})`;
            this.imageSource0 = `${this.speakersImageDirectory}/${this.talkSpeakers.speakerPhotoFileName0}`;
            this.imageSource1 = `${this.speakersImageDirectory}/${this.talkSpeakers.speakerPhotoFileName1}`;
            this.imageSource2 = `${this.speakersImageDirectory}/${this.talkSpeakers.speakerPhotoFileName2}`;
            this.imageSource3 = `${this.speakersImageDirectory}/${this.talkSpeakers.speakerPhotoFileName3}`;

            if (this.talkSpeakers.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.talkSpeakers.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.talkSpeakers.currentIndex, id)
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
