import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../../shared/services/locale.service';
import { SpeakerDetails } from '../../../shared/models/speaker/speaker-details.model';
import { SpeakerService } from '../../../shared/services/speaker.service';
import {
  getTalkClassnameByFilename,
  getTalksWithMaterialsOrderNumber,
  getTalksWithSpeakersString
} from '../../general/utility-functions';

@Component({
    selector: 'app-speaker',
    templateUrl: './speaker.component.html',
    standalone: false
})
export class SpeakerComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public twitterUrlPrefix = 'https://twitter.com';
  public gitHubUrlPrefix = 'https://github.com';
  public habrUrlPrefix = 'https://habr.com/users';

  public id: number;
  public speakerDetails: SpeakerDetails = new SpeakerDetails();
  public multiSortMeta: any[] = [];

  public language: string;
  private languageSubscription: Subscription;

  constructor(public speakerService: SpeakerService, public translateService: TranslateService,
              private activatedRoute: ActivatedRoute, private localeService: LocaleService) {
    this.multiSortMeta.push({field: 'talkDate', order: -1});
    this.multiSortMeta.push({field: 'name', order: 1});
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      const idString: string = params['id'];
      const idNumber = Number(idString);

      if (!isNaN(idNumber)) {
        this.id = idNumber;
        this.loadSpeaker(this.id);

        this.languageSubscription = this.translateService.onLangChange
          .subscribe(() => {
            this.language = this.localeService.getLanguage();
            this.onLanguageChange();
          });
      }
    });
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  loadSpeaker(id: number) {
    this.speakerService.getSpeaker(id, this.language)
      .subscribe(data => {
        this.speakerDetails = this.getSpeakerDetailsWithFilledAttributes(data);
      });
  }

  getSpeakerDetailsWithFilledAttributes(speakerDetails: SpeakerDetails): SpeakerDetails {
    if (speakerDetails?.talks) {
      speakerDetails.talks = getTalksWithMaterialsOrderNumber(getTalksWithSpeakersString(speakerDetails.talks));
    }

    return speakerDetails;
  }

  onLanguageChange() {
    this.loadSpeaker(this.id);
  }

  isSocialsVisible(): boolean {
    return (!!this.speakerDetails.speaker?.javaChampion || !!this.speakerDetails.speaker?.anyMvp ||
      !!this.speakerDetails.speaker?.twitter || !!this.speakerDetails.speaker?.gitHub || !!this.speakerDetails.speaker?.habr);
  }

  isTalksListVisible(): boolean {
    return ((this.speakerDetails.talks) && (this.speakerDetails.talks.length > 0));
  }

  getClassnameByFilename(filename: string): string {
    return getTalkClassnameByFilename(filename);
  }
}
