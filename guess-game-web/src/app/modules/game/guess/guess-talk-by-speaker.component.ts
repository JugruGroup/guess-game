import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { AnswerService } from '../../../shared/services/answer.service';
import { GameState } from '../../../shared/models/game-state.model';
import { LocaleService } from '../../../shared/services/locale.service';
import { SpeakersTalks } from '../../../shared/models/guess/speakers-talks.model';
import { StateService } from '../../../shared/services/state.service';

@Component({
    selector: 'app-guess-talk-by-speaker',
    templateUrl: './guess-talk-by-speaker.component.html',
    standalone: false
})
export class GuessTalkBySpeakerComponent implements OnInit {
  private imageDirectory = 'assets/images';
  private eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public speakersTalks: SpeakersTalks;
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
    this.stateService.getSpeakerTalks(this.language)
      .subscribe(data => {
          if (data) {
            this.speakersTalks = data;
            this.title = `${this.speakersTalks.questionSetName} (${this.speakersTalks.currentIndex + 1}/${this.speakersTalks.totalNumber})`;

            if (this.speakersTalks.logoFileName) {
              this.logoImageSource = `${this.eventsImageDirectory}/${this.speakersTalks.logoFileName}`;
            }
          } else {
            this.result();
          }
        }
      );
  }

  answer(id: number) {
    this.answerService.setAnswer(this.speakersTalks.currentIndex, id)
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
