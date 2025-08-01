import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { EventPart } from '../../shared/models/event/event-part.model';
import { HomeState } from '../../shared/models/home-state.model';
import { EventService } from '../../shared/services/event.service';
import { LocaleService } from '../../shared/services/locale.service';
import { getEventDates } from '../general/utility-functions';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  standalone: false
})
export class HomeComponent implements OnInit, OnDestroy {
  public imageDirectory = 'assets/images';
  public eventsImageDirectory = `${this.imageDirectory}/events`;

  public eventPart: EventPart;
  public eventDates: string;
  public homeState = HomeState.LoadingState;

  public language: string;
  private languageSubscription: Subscription;

  constructor(private eventService: EventService, public translateService: TranslateService,
              private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.loadDefaultEvent();

    this.languageSubscription = this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.loadDefaultEvent();
      });
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  loadDefaultEvent() {
    if (this.translateService.getCurrentLang()) {
      this.eventService.getDefaultEventPartHomeInfo(this.language)
        .subscribe(data => {
          this.eventPart = data;
          this.eventDates = (this.eventPart) ?
            getEventDates(this.eventPart.startDate, this.eventPart.endDate, this.translateService) : null;
          this.homeState = (this.eventPart) ? HomeState.DefaultStateFoundState : HomeState.DefaultStateNotFoundState;
        });
    }
  }

  isLoading(): boolean {
    return (this.homeState === HomeState.LoadingState);
  }

  isDefaultEventFound(): boolean {
    return (this.homeState === HomeState.DefaultStateFoundState);
  }

  isDefaultEventNotFound(): boolean {
    return (this.homeState === HomeState.DefaultStateNotFoundState);
  }
}
