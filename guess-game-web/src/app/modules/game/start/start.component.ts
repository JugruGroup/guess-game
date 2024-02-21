import { AfterViewChecked, Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { StartParameters } from '../../../shared/models/start/start-parameters.model';
import { GuessMode } from '../../../shared/models/guess-mode.model';
import { EventType } from '../../../shared/models/event-type/event-type.model';
import { Event } from '../../../shared/models/event/event.model';
import { EventService } from '../../../shared/services/event.service';
import { QuestionService } from '../../../shared/services/question.service';
import { StateService } from '../../../shared/services/state.service';
import { findEventById, findEventTypeById, getEventsWithFullDisplayName } from '../../general/utility-functions';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html'
})
export class StartComponent implements OnInit, AfterViewChecked {
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

  private selectedOptionsUpdated = false;

  @ViewChildren('eventTypeRow', {read: ElementRef}) rowElement: QueryList<ElementRef>;

  constructor(private questionService: QuestionService, private stateService: StateService, private eventService: EventService,
              private router: Router, public translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.loadEventTypes();

    this.translateService.onLangChange
      .subscribe(() => this.loadEventTypes());
  }

  loadEventTypes() {
    this.questionService.getEventTypes()
      .subscribe(eventTypesData => {
        this.eventTypes = eventTypesData;
        this.eventTypeSelectItems = this.eventTypes.map(et => {
            return {label: et.displayName, value: et};
          }
        );

        if (this.eventTypes.length > 0) {
          this.eventService.getDefaultEvent()
            .subscribe(defaultEventData => {
              this.defaultEvent = defaultEventData;

              const selectedEventType = (this.defaultEvent) ? findEventTypeById(this.defaultEvent.eventTypeId, this.eventTypes) : null;

              if (selectedEventType) {
                this.selectedEventTypes = [selectedEventType];
                this.selectedOptionsUpdated = true;
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

  ngAfterViewChecked() {
    if (this.selectedOptionsUpdated) {
      if (this.selectedEventTypes && (this.selectedEventTypes.length > 0)) {
        const eventType: EventType = this.selectedEventTypes[0];
        const elementRef = this.rowElement.find(r => r.nativeElement.getAttribute('id') === eventType.id.toString());

        if (elementRef) {
          elementRef.nativeElement.scrollIntoView({behavior: 'auto', block: 'center', inline: 'nearest'});
        }
      }

      this.selectedOptionsUpdated = false;
    }
  }

  onEventTypeChange(eventTypes: EventType[]) {
    this.loadEvents(eventTypes);
  }

  loadEvents(eventTypes: EventType[]) {
    if (this.translateService.currentLang) {
      this.questionService.getEvents(eventTypes.map(et => et.id))
        .subscribe(data => {
          this.events = getEventsWithFullDisplayName(data, this.translateService);

          if (this.events.length > 0) {
            const selectedEvent = (this.defaultEvent) ? findEventById(this.defaultEvent.id, this.events) : null;

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

  loadQuantities(eventTypes: EventType[], events: Event[], guessMode) {
    this.questionService.getQuantities(eventTypes.map(et => et.id), events.map(e => e.id), guessMode)
      .subscribe(data => {
        this.quantities = data;
        this.selectedQuantity = (data && (data.length > 0)) ? data[0] : null;
        this.quantitySelectItems = this.quantities.map(q => {
            return {label: q.toString(), value: q};
          }
        );
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
      .subscribe(data => {
        this.router.navigateByUrl('/game/guess/name-by-photo');
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
