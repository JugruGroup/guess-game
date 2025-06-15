import { Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { StartParameters } from '../../../shared/models/start/start-parameters.model';
import { GuessMode } from '../../../shared/models/guess-mode.model';
import { EventType } from '../../../shared/models/event-type/event-type.model';
import { Event } from '../../../shared/models/event/event.model';
import { EventService } from '../../../shared/services/event.service';
import { LocaleService } from '../../../shared/services/locale.service';
import { QuestionService } from '../../../shared/services/question.service';
import { StateService } from '../../../shared/services/state.service';
import {
  findEventById, findEventsByIds,
  findEventTypeById,
  findEventTypesByIds,
  getEventsWithFullDisplayName
} from '../../general/utility-functions';

@Component({
    selector: 'app-start',
    templateUrl: './start.component.html',
    standalone: false
})
export class StartComponent implements OnInit, OnDestroy {
  private readonly MIN_QUANTITY_VALUE = 4;

  private imageDirectory = 'assets/images';
  public eventsImageDirectory = `${this.imageDirectory}/events`;

  public eventTypes: EventType[] = [];
  public selectedEventTypes: EventType[] = [];
  public eventTypeSelectItems: SelectItem[] = [];

  public events: Event[] = [];
  public selectedEvents: Event[] = [];

  public guessMode = GuessMode;
  public selectedGuessMode: GuessMode = GuessMode.GuessNameByPhotoMode;

  public quantities: number[] = [];
  public selectedQuantity: number;
  public quantitySelectItems: SelectItem[] = [];

  private defaultEvent: Event;

  public language: string;
  private languageSubscription: Subscription;

  constructor(private questionService: QuestionService, private stateService: StateService, private eventService: EventService,
              private router: Router, private translateService: TranslateService, private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.loadEventTypes();

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

  fillEventTypes(eventTypes: EventType[]) {
    this.eventTypes = eventTypes;
    this.eventTypeSelectItems = this.eventTypes.map(et => {
        return {label: et.displayName, value: et};
      }
    );
  }

  loadEventTypes() {
    this.questionService.getEventTypes(this.language)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        if (this.eventTypes.length > 0) {
          this.eventService.getDefaultEvent(this.language)
            .subscribe(defaultEventData => {
              this.defaultEvent = defaultEventData;

              const selectedEventType = (this.defaultEvent) ?
                findEventTypeById(this.defaultEvent.eventTypeId, this.eventTypes) : null;

              if (selectedEventType) {
                this.selectedEventTypes = [selectedEventType];
              } else {
                this.selectedEventTypes = [this.eventTypes[0]];
              }

              this.loadEvents(this.selectedEventTypes);
            });
        } else {
          this.selectedEventTypes = [];
          this.loadEvents(this.selectedEventTypes);
        }
      });
  }

  onEventTypeChange(eventTypes: EventType[]) {
    this.loadEvents(eventTypes);
  }

  loadEvents(eventTypes: EventType[]) {
    if (this.translateService.currentLang) {
      this.questionService.getEvents(eventTypes.map(et => et.id), this.language)
        .subscribe(data => {
          this.events = getEventsWithFullDisplayName(data, this.translateService);

          if (this.events.length > 0) {
            const selectedEvent = (this.defaultEvent) ?
              findEventById(this.defaultEvent.id, this.events) : null;

            if (selectedEvent) {
              this.selectedEvents = [selectedEvent];
            } else {
              this.selectedEvents = [this.events[0]];
            }
          } else {
            this.selectedEvents = [];
          }

          this.loadQuantities(this.selectedEventTypes, this.selectedEvents, this.selectedGuessMode);
        });
    }
  }

  onEventChange(events: Event[]) {
    this.loadQuantities(this.selectedEventTypes, events, this.selectedGuessMode);
  }

  onModeChange(guessMode: string) {
    this.loadQuantities(this.selectedEventTypes, this.selectedEvents, guessMode);
  }

  fillQuantities(quantities: number[]) {
    this.quantities = quantities;
    this.quantitySelectItems = quantities.map(q => {
        return {label: q.toString(), value: q};
      }
    );
  }

  loadQuantities(eventTypes: EventType[], events: Event[], guessMode) {
    this.questionService.getQuantities(eventTypes.map(et => et.id), events.map(e => e.id), guessMode)
      .subscribe(data => {
        this.fillQuantities(data);
        this.selectedQuantity = (data && (data.length > 0)) ? data[0] : null;
      });
  }

  onLanguageChange() {
    const currentSelectedEventTypes = this.selectedEventTypes;
    const currentSelectedEvents = this.selectedEvents;
    const currentSelectedQuantity = this.selectedQuantity;

    this.questionService.getEventTypes(this.language)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        if (this.eventTypes.length > 0) {
          const selectedEventTypes = (currentSelectedEventTypes) ?
            findEventTypesByIds(currentSelectedEventTypes.map(et => et.id), this.eventTypes) : [];

          if (selectedEventTypes && (selectedEventTypes.length > 0)) {
            this.selectedEventTypes = selectedEventTypes;
          } else {
            this.selectedEventTypes = [this.eventTypes[0]];
          }
        } else {
          this.selectedEventTypes = [];
        }

        this.questionService.getEvents(this.selectedEventTypes.map(et => et.id), this.language)
          .subscribe(data => {
            this.events = getEventsWithFullDisplayName(data, this.translateService);

            if (this.events.length > 0) {
              const selectedEvents = (currentSelectedEvents) ?
                findEventsByIds(currentSelectedEvents.map(e => e.id), this.events) : [];

              if (selectedEvents && (selectedEvents.length > 0)) {
                this.selectedEvents = selectedEvents;
              } else {
                this.selectedEvents = [this.events[0]];
              }
            } else {
              this.selectedEvents = [];
            }

            this.questionService.getQuantities(this.selectedEventTypes.map(et => et.id),
              this.selectedEvents.map(e => e.id), this.selectedGuessMode)
              .subscribe(data => {
                this.fillQuantities(data);

                if (currentSelectedQuantity) {
                  this.selectedQuantity = currentSelectedQuantity;
                } else {
                  this.selectedQuantity = (data && (data.length > 0)) ? data[0] : null;
                }
              });
          });
      });
  }

  getSelectedQuantityValue(): number {
    return !isNaN(this.selectedQuantity) ? this.selectedQuantity : 0;
  }

  start() {
    this.stateService.setStartParameters(
      new StartParameters(
        this.selectedEventTypes.map(et => et.id),
        this.selectedEvents.map(e => e.id),
        this.selectedGuessMode,
        this.getSelectedQuantityValue()))
      .subscribe(() => {
        this.router.navigateByUrl(`/${this.language}/game/guess/name-by-photo`);
      });
  }

  isEventsDisabled(): boolean {
    return (this.selectedEventTypes &&
      ((this.selectedEventTypes.length > 1) || ((this.selectedEventTypes.length === 1) && !this.selectedEventTypes[0].conference)));
  }

  isStartDisabled(): boolean {
    return (!this.selectedEventTypes) || (!this.selectedEvents) ||
      (this.selectedEventTypes && (this.selectedEventTypes.length <= 0)) ||
      (this.selectedEvents && (this.selectedEvents.length <= 0)) ||
      (this.getSelectedQuantityValue() < this.MIN_QUANTITY_VALUE);
  }

  isEventTypeInactiveNotSelected(eventType: EventType) {
    if (eventType && eventType.inactive && this.selectedEventTypes) {
      return (this.selectedEventTypes.indexOf(eventType) === -1);
    } else {
      return false;
    }
  }
}
