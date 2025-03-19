import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { EntitiesListComponent } from '../entity-list.component';
import { LocaleService } from '../../../shared/services/locale.service';
import { Speaker } from '../../../shared/models/speaker/speaker.model';
import { SpeakerService } from '../../../shared/services/speaker.service';

@Component({
    selector: 'app-speakers-list',
    templateUrl: './speakers-list.component.html',
    standalone: false
})
export class SpeakersListComponent extends EntitiesListComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public twitterUrlPrefix = 'https://twitter.com';
  public gitHubUrlPrefix = 'https://github.com';
  public habrUrlPrefix = 'https://habr.com/users';

  public speakers: Speaker[] = [];

  constructor(public speakerService: SpeakerService, translateService: TranslateService, localeService: LocaleService) {
    super(translateService, localeService);
  }

  ngOnInit(): void {
    this.loadSpeakers(this.selectedLetter);

    this.languageSubscription = this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.onLanguageChange();
      });
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  loadSpeakers(letter: string) {
    this.speakerService.getSpeakersByFirstLetter(letter, this.language)
      .subscribe(data => {
        this.speakers = data;
      });
  }

  isCurrentLetter(letter: string) {
    return (this.selectedLetter === letter);
  }

  changeLetter(letter: string) {
    this.selectedLetter = letter;

    this.paginatorFirst = 0;

    this.loadSpeakers(letter);
  }

  isSpeakersListVisible() {
    return (this.speakers.length > 0);
  }
}
