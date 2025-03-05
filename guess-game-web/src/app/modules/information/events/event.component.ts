import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { EventDays } from '../../../shared/models/event/event-days.model';
import { EventDetails } from '../../../shared/models/event/event-details.model';
import { EventService } from '../../../shared/services/event.service';
import { LocaleService } from '../../../shared/services/locale.service';
import {
  getEventDates,
  getSpeakersWithCompaniesString,
  getTalkClassnameByFilename,
  getTalksWithMaterialsOrderNumber,
  getTalksWithSpeakersString
} from '../../general/utility-functions';

@Component({
    selector: 'app-event',
    templateUrl: './event.component.html',
    standalone: false
})
export class EventComponent implements OnInit {
  private imageDirectory = 'assets/images';
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public twitterUrlPrefix = 'https://twitter.com';
  public gitHubUrlPrefix = 'https://github.com';
  public habrUrlPrefix = 'https://habr.com/users';
  public googleMapsUrlPrefix = 'https://www.google.com/maps/place';

  public id: number;
  public eventDetails: EventDetails = new EventDetails();
  public speakersMultiSortMeta: any[] = [];
  public talksMultiSortMeta: any[] = [];

  public language: string;

  constructor(private eventService: EventService, public translateService: TranslateService,
              private activatedRoute: ActivatedRoute, private localeService: LocaleService) {
    this.speakersMultiSortMeta.push({field: 'displayName', order: 1});
    this.speakersMultiSortMeta.push({field: 'companiesString', order: 1});

    this.talksMultiSortMeta.push({field: 'talkDay', order: 1});
    this.talksMultiSortMeta.push({field: 'talkStartTime', order: 1});
    this.talksMultiSortMeta.push({field: 'track', order: 1});
    this.talksMultiSortMeta.push({field: 'talkEndTime', order: 1});

    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      const idString: string = params['id'];
      const idNumber = Number(idString);

      if (!isNaN(idNumber)) {
        this.id = idNumber;
        this.loadEvent(this.id);

        this.translateService.onLangChange
          .subscribe(() => {
            this.language = this.localeService.getLanguage();
            this.loadEvent(this.id);
          });
      }
    });
  }

  loadEvent(id: number) {
    if (this.translateService.currentLang) {
      this.eventService.getEvent(id, this.language)
        .subscribe(data => {
          this.eventDetails = this.getEventDetailsWithFilledAttributes(data);
        });
    }
  }

  getEventDetailsWithFilledAttributes(eventDetails: EventDetails): EventDetails {
    if (eventDetails?.speakers) {
      eventDetails.speakers = getSpeakersWithCompaniesString(eventDetails.speakers);
    }

    if (eventDetails?.talks) {
      eventDetails.talks = getTalksWithMaterialsOrderNumber(getTalksWithSpeakersString(eventDetails.talks));
    }

    return eventDetails;
  }

  isDisplayPlacesVisible() {
    return ((this.eventDetails.event?.days) && (this.eventDetails.event.days.length > 0));
  }

  isEventLinksVisible() {
    return this.eventDetails.event?.siteLink || this.eventDetails.event?.facebookLink || this.eventDetails.event?.vkLink ||
      this.eventDetails.event?.twitterLink || this.eventDetails.event?.youtubeLink || this.eventDetails.event?.telegramLink ||
      this.eventDetails.event?.speakerdeckLink || this.eventDetails.event?.habrLink;
  }

  isSpeakersListVisible() {
    return ((this.eventDetails.speakers) && (this.eventDetails.speakers.length > 0));
  }

  isTalksListVisible() {
    return ((this.eventDetails.talks) && (this.eventDetails.talks.length > 0));
  }

  isTopicColumnVisible() {
    return ((this.eventDetails.event) && !this.eventDetails.event.topicExist);
  }

  getDisplayPlace(eventDays: EventDays): string {
    let place = '';

    if (eventDays?.placeCity && (eventDays.placeCity.length > 0)) {
      place += eventDays?.placeCity;
    }

    if (place && (place.length > 0) && eventDays?.placeVenueAddress && (eventDays.placeVenueAddress.length > 0)) {
      place += ', ';
    }

    if (eventDays?.placeVenueAddress && (eventDays.placeVenueAddress.length > 0)) {
      place += eventDays?.placeVenueAddress;
    }

    return place;
  }

  getEventDays(eventDays: EventDays): string {
    return getEventDates(eventDays.startDate, eventDays.endDate, this.translateService);
  }

  getClassnameByFilename(filename: string): string {
    return getTalkClassnameByFilename(filename);
  }
}
