import { Component, OnDestroy, OnInit } from '@angular/core';
import { SelectItem } from 'primeng/api';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { EventService } from '../../../../shared/services/event.service';
import { EventStatistics } from '../../../../shared/models/statistics/event/event-statistics.model';
import { EventType } from '../../../../shared/models/event-type/event-type.model';
import { EventTypeService } from '../../../../shared/services/event-type.service';
import { LocaleService } from '../../../../shared/services/locale.service';
import { Organizer } from '../../../../shared/models/organizer/organizer.model';
import { OrganizerService } from '../../../../shared/services/organizer.service';
import { StatisticsService } from '../../../../shared/services/statistics.service';
import { findEventTypeById, findOrganizerById } from '../../../general/utility-functions';

@Component({
    selector: 'app-event-statistics',
    templateUrl: './event-statistics.component.html',
    standalone: false
})
export class EventStatisticsComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  public eventsImageDirectory = `${this.imageDirectory}/events`;

  public isConferences = true;
  public isMeetups = true;

  public organizers: Organizer[] = [];
  public selectedOrganizer: Organizer;
  public organizerSelectItems: SelectItem[] = [];

  public eventTypes: EventType[] = [];
  public selectedEventType: EventType;
  public eventTypeSelectItems: SelectItem[] = [];

  public eventStatistics = new EventStatistics();
  public multiSortMeta: any[] = [];

  public language: string;
  private languageSubscription: Subscription;

  constructor(private statisticsService: StatisticsService, private eventTypeService: EventTypeService,
              private eventService: EventService, public organizerService: OrganizerService,
              public translateService: TranslateService, private localeService: LocaleService) {
    this.multiSortMeta.push({field: 'name', order: 1});
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.loadOrganizers();

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

  fillOrganizers(organizers: Organizer[]) {
    this.organizers = organizers;
    this.organizerSelectItems = this.organizers.map(o => {
        return {label: o.name, value: o};
      }
    );
  }

  fillEventTypes(eventTypes: EventType[]) {
    this.eventTypes = eventTypes;
    this.eventTypeSelectItems = this.eventTypes.map(et => {
        return {label: et.name, value: et};
      }
    );
  }

  loadOrganizers() {
    this.organizerService.getOrganizers(this.language)
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.eventService.getDefaultEvent(this.language)
          .subscribe(defaultEventData => {
            this.selectedOrganizer = (defaultEventData) ? findOrganizerById(defaultEventData.organizerId, this.organizers) : null;

            this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
              .subscribe(eventTypesData => {
                this.fillEventTypes(eventTypesData);

                if (this.eventTypes.length > 0) {
                  this.selectedEventType = (defaultEventData) ? findEventTypeById(defaultEventData.eventTypeId, this.eventTypes) : null;
                } else {
                  this.selectedEventType = null;
                }

                this.loadEventStatistics(this.selectedOrganizer, this.selectedEventType);
              });
          });
      });
  }

  loadEventTypes() {
    this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        this.selectedEventType = null;

        this.loadEventStatistics(this.selectedOrganizer, this.selectedEventType);
      });
  }

  loadEventStatistics(organizer: Organizer, eventType: EventType) {
    this.statisticsService.getEventStatistics(this.isConferences, this.isMeetups, organizer, eventType, this.language)
      .subscribe(data => {
          this.eventStatistics = data;
        }
      );
  }

  onEventTypeKindChange() {
    this.loadEventTypes();
  }

  onOrganizerChange() {
    this.loadEventTypes();
  }

  onEventTypeChange() {
    this.loadEventStatistics(this.selectedOrganizer, this.selectedEventType);
  }

  onLanguageChange() {
    const currentSelectedOrganizer = this.selectedOrganizer;
    const currentSelectedEventType = this.selectedEventType;

    this.organizerService.getOrganizers(this.language)
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.selectedOrganizer = (currentSelectedOrganizer) ? findOrganizerById(currentSelectedOrganizer.id, this.organizers) : null;

        this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
          .subscribe(eventTypesData => {
            this.fillEventTypes(eventTypesData);

            if (this.eventTypes.length > 0) {
              this.selectedEventType = (currentSelectedEventType) ? findEventTypeById(currentSelectedEventType.id, this.eventTypes) : null;
            } else {
              this.selectedEventType = null;
            }

            this.loadEventStatistics(this.selectedOrganizer, this.selectedEventType);
          });
      });
  }

  isNoEventsFoundVisible() {
    return (this.eventStatistics?.eventMetricsList && (this.eventStatistics.eventMetricsList.length === 0));
  }

  isEventsListVisible() {
    return (this.eventStatistics?.eventMetricsList && (this.eventStatistics.eventMetricsList.length > 0));
  }
}
