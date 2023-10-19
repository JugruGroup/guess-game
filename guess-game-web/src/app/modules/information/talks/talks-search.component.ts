import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { EventType } from '../../../shared/models/event-type/event-type.model';
import { Event } from '../../../shared/models/event/event.model';
import { Topic } from '../../../shared/models/topic/topic.model';
import { Talk } from '../../../shared/models/talk/talk.model';
import { EventTypeService } from '../../../shared/services/event-type.service';
import { EventService } from '../../../shared/services/event.service';
import { TalkService } from '../../../shared/services/talk.service';
import { TopicService } from '../../../shared/services/topic.service';
import {
  findEventById,
  findEventTypeById,
  findTopicById,
  getEventsWithBriefDisplayName,
  getEventsWithFullDisplayName,
  getTalksWithSpeakersString,
  isStringEmpty
} from '../../general/utility-functions';

@Component({
  selector: 'app-talks-search',
  templateUrl: './talks-search.component.html'
})
export class TalksSearchComponent implements OnInit {
  private imageDirectory = 'assets/images';
  public eventsImageDirectory = `${this.imageDirectory}/events`;

  public eventTypes: EventType[] = [];
  public selectedEventType: EventType;
  public eventTypeSelectItems: SelectItem[] = [];

  public events: Event[] = [];
  public selectedEvent: Event;
  public eventSelectItems: SelectItem[] = [];

  private defaultEvent: Event;

  public talkName: string;
  public speakerName: string;

  public topics: Topic[] = [];
  public selectedTopic: Topic;
  public topicSelectItems: SelectItem[] = [];

  public selectedLanguage: string;
  public languageSelectItems: SelectItem[] = [{label: 'EN', value: 'en'}, {label: 'RU', value: 'ru'}];

  public talks: Talk[] = [];

  private searched = false;
  public multiSortMeta: any[] = [];

  constructor(private eventTypeService: EventTypeService, private eventService: EventService,
              private talkService: TalkService, private topicService: TopicService,
              public translateService: TranslateService) {
    this.multiSortMeta.push({field: 'event.name', order: 1});
    this.multiSortMeta.push({field: 'talkDate', order: 1});
    this.multiSortMeta.push({field: 'name', order: 1});
  }

  ngOnInit(): void {
    this.loadEventTypes();
    this.loadTopics();
  }

  fillEventTypes(eventTypes: EventType[]) {
    this.eventTypes = eventTypes;
    this.eventTypeSelectItems = this.eventTypes.map(et => {
        return {label: et.name, value: et};
      }
    );
  }

  fillEvents(events: Event[]) {
    this.events = (this.translateService.currentLang) ?
      getEventsWithFullDisplayName(events, this.translateService) :
      getEventsWithBriefDisplayName(events);
    this.eventSelectItems = this.events.map(e => {
      return {label: e.displayName, value: e};
    });
  }

  fillTopics(topics: Topic[]) {
    this.topics = topics;
    this.topicSelectItems = this.topics.map(t => {
        return {label: t.name, value: t};
      }
    );
  }

  loadEventTypes() {
    this.eventTypeService.getFilterEventTypes(true, true, null)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        if (this.eventTypes.length > 0) {
          this.eventService.getDefaultEvent()
            .subscribe(defaultEventData => {
              this.defaultEvent = defaultEventData;

              const selectedEventType = (this.defaultEvent) ? findEventTypeById(this.defaultEvent.eventTypeId, this.eventTypes) : null;
              this.selectedEventType = (selectedEventType) ? selectedEventType : null;

              this.loadEvents(this.selectedEventType);
            });
        } else {
          this.selectedEventType = null;
          this.loadEvents(this.selectedEventType);
        }
      });
  }

  onEventTypeChange(eventType: EventType) {
    this.loadEvents(eventType);
    this.searched = false;
  }

  loadEvents(eventType: EventType) {
    if (eventType) {
      this.eventService.getEvents(true, true, null, eventType)
        .subscribe(data => {
          this.fillEvents(data);

          if (this.events.length > 0) {
            const selectedEvent = (this.defaultEvent) ? findEventById(this.defaultEvent.id, this.events) : null;
            this.selectedEvent = (selectedEvent) ? selectedEvent : null;
          } else {
            this.selectedEvent = null;
          }
        });
    } else {
      this.events = [];
      this.eventSelectItems = [];
      this.selectedEvent = null;
    }
  }

  onFilterChange() {
    this.searched = false;
  }

  loadTopics() {
    this.topicService.getTopics()
      .subscribe(topicsData => {
        this.fillTopics(topicsData);

        this.selectedTopic = null;
      });
  }

  loadTalks(eventType: EventType, event: Event, talkName: string, speakerName: string, topic: Topic, language: string) {
    this.talkService.getTalks(eventType, event, talkName, speakerName, topic, language)
      .subscribe(data => {
        this.talks = getTalksWithSpeakersString(data);
        this.searched = true;
      });
  }

  onLanguageChange() {
    const currentSelectedEventType = this.selectedEventType;
    const currentSelectedEvent = this.selectedEvent;

    // Load event types
    this.eventTypeService.getFilterEventTypes(true, true, null)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        if (this.eventTypes.length > 0) {
          this.selectedEventType = (currentSelectedEventType) ? findEventTypeById(currentSelectedEventType.id, this.eventTypes) : null;
        } else {
          this.selectedEventType = null;
        }

        // Load events and search
        if (this.selectedEventType) {
          this.eventService.getEvents(true, true, null, this.selectedEventType)
            .subscribe(data => {
              this.fillEvents(data);

              if (this.events.length > 0) {
                this.selectedEvent = (currentSelectedEvent) ? findEventById(currentSelectedEvent.id, this.events) : null;
              } else {
                this.selectedEvent = null;
              }

              this.loadTopicsAndSearch();
            });
        } else {
          this.events = [];
          this.eventSelectItems = [];
          this.selectedEvent = null;

          this.loadTopicsAndSearch();
        }
      });
  }

  loadTopicsAndSearch() {
    const currentSelectedTopic = this.selectedTopic;

    this.topicService.getTopics()
      .subscribe(topicsData => {
        this.fillTopics(topicsData);

        if (this.topics.length > 0) {
          this.selectedTopic = (currentSelectedTopic) ? findTopicById(currentSelectedTopic.id, this.topics) : null;
        } else {
          this.selectedTopic = null;
        }

        this.search();
      });
  }

  search() {
    if (!this.isSearchDisabled()) {
      this.loadTalks(this.selectedEventType, this.selectedEvent, this.talkName, this.speakerName, this.selectedTopic, this.selectedLanguage);
    }
  }

  clear() {
    this.selectedEventType = undefined;

    this.events = [];
    this.eventSelectItems = [];
    this.selectedEvent = undefined;

    this.talkName = undefined;
    this.speakerName = undefined;
    this.selectedTopic = undefined;
    this.selectedLanguage = undefined;

    this.searched = false;
  }

  isSearchDisabled(): boolean {
    return (!this.selectedEventType &&
      !this.selectedEvent &&
      !this.selectedTopic &&
      !this.selectedLanguage &&
      isStringEmpty(this.talkName) &&
      isStringEmpty(this.speakerName));
  }

  isNoTalksFoundVisible() {
    return (this.searched && (this.talks.length === 0));
  }

  isTalksListVisible() {
    return (this.searched && (this.talks.length > 0));
  }
}
