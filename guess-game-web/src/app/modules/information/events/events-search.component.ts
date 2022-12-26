import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { EventType } from '../../../shared/models/event-type/event-type.model';
import { EventPart } from '../../../shared/models/event/event-part.model';
import { Organizer } from '../../../shared/models/organizer/organizer.model';
import { EventTypeService } from '../../../shared/services/event-type.service';
import { EventService } from '../../../shared/services/event.service';
import { OrganizerService } from '../../../shared/services/organizer.service';
import { findEventTypeById, findOrganizerById } from '../../general/utility-functions';

@Component({
  selector: 'app-events-search',
  templateUrl: './events-search.component.html'
})
export class EventsSearchComponent implements OnInit {
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

  public eventParts: EventPart[] = [];
  public multiSortMeta: any[] = [];

  constructor(private eventService: EventService, public organizerService: OrganizerService,
              private eventTypeService: EventTypeService, public translateService: TranslateService) {
    this.multiSortMeta.push({field: 'startDate', order: -1});
    this.multiSortMeta.push({field: 'name', order: 1});
  }

  ngOnInit(): void {
    this.loadOrganizers();
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
    this.organizerService.getOrganizers()
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.eventService.getDefaultEvent()
          .subscribe(defaultEventData => {
            this.selectedOrganizer = (defaultEventData) ? findOrganizerById(defaultEventData.organizerId, this.organizers) : null;

            this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer)
              .subscribe(eventTypesData => {
                this.fillEventTypes(eventTypesData);

                if (this.eventTypes.length > 0) {
                  this.selectedEventType = (defaultEventData) ? findEventTypeById(defaultEventData.eventTypeId, this.eventTypes) : null;
                } else {
                  this.selectedEventType = null;
                }

                this.loadEvents(this.selectedOrganizer, this.selectedEventType);
              });
          });
      });
  }

  loadEventTypes() {
    this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        this.selectedEventType = null;

        this.loadEvents(this.selectedOrganizer, this.selectedEventType);
      });
  }

  loadEvents(organizer: Organizer, eventType: EventType) {
    this.eventService.getEventParts(this.isConferences, this.isMeetups, organizer, eventType)
      .subscribe(data => {
          this.eventParts = data;
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
    this.loadEvents(this.selectedOrganizer, this.selectedEventType);
  }

  onLanguageChange() {
    const currentSelectedOrganizer = this.selectedOrganizer;
    const currentSelectedEventType = this.selectedEventType;

    this.organizerService.getOrganizers()
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.selectedOrganizer = (currentSelectedOrganizer) ? findOrganizerById(currentSelectedOrganizer.id, this.organizers) : null;

        this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer)
          .subscribe(eventTypesData => {
            this.fillEventTypes(eventTypesData);

            if (this.eventTypes.length > 0) {
              this.selectedEventType = (currentSelectedEventType) ? findEventTypeById(currentSelectedEventType.id, this.eventTypes) : null;
            } else {
              this.selectedEventType = null;
            }

            this.loadEvents(this.selectedOrganizer, this.selectedEventType);
          });
      });
  }

  isNoEventsFoundVisible() {
    return (this.eventParts && (this.eventParts.length === 0));
  }

  isEventPartsListVisible() {
    return (this.eventParts && (this.eventParts.length > 0));
  }
}
