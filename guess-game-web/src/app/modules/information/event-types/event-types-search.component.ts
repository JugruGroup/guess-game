import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { EventType } from '../../../shared/models/event-type/event-type.model';
import { Organizer } from '../../../shared/models/organizer/organizer.model';
import { EventTypeService } from '../../../shared/services/event-type.service';
import { OrganizerService } from '../../../shared/services/organizer.service';
import { TopicService } from '../../../shared/services/topic.service';
import { Topic } from '../../../shared/models/topic/topic.model';
import { findOrganizerById, findTopicById, getEventTypesWithSortName } from '../../general/utility-functions';

@Component({
  selector: 'app-event-types-search',
  templateUrl: './event-types-search.component.html'
})
export class EventTypesSearchComponent implements OnInit {
  private imageDirectory = 'assets/images';
  public eventsImageDirectory = `${this.imageDirectory}/events`;

  public isConferences = true;
  public isMeetups = true;

  public organizers: Organizer[] = [];
  public selectedOrganizer: Organizer;
  public organizerSelectItems: SelectItem[] = [];

  public topics: Topic[] = [];
  public selectedTopic: Topic;
  public topicSelectItems: SelectItem[] = [];

  public eventTypes: EventType[] = [];

  constructor(private eventTypeService: EventTypeService, public organizerService: OrganizerService,
              private topicService: TopicService, public translateService: TranslateService) {
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

  fillTopics(topics: Topic[]) {
    this.topics = topics;
    this.topicSelectItems = this.topics.map(t => {
        return {label: t.name, value: t};
      }
    );
  }

  loadOrganizers() {
    const currentSelectedTopic = this.selectedTopic;

    this.organizerService.getOrganizers()
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        if (this.organizers.length > 0) {
          this.organizerService.getDefaultEventOrganizer()
            .subscribe(defaultOrganizerData => {
              const selectedOrganizer = (defaultOrganizerData) ? findOrganizerById(defaultOrganizerData.id, this.organizers) : null;
              this.selectedOrganizer = (selectedOrganizer) ? selectedOrganizer : null;

              this.topicService.getFilterTopics(this.isConferences, this.isMeetups, this.selectedOrganizer)
                .subscribe(topicsData => {
                  this.fillTopics(topicsData);

                  if (this.topics.length > 0) {
                    this.selectedTopic = (currentSelectedTopic) ? findTopicById(currentSelectedTopic.id, this.topics) : null;
                  } else {
                    this.selectedTopic = null;
                  }

                  this.loadEventTypes(this.selectedOrganizer, this.selectedTopic);
                });
            });
        } else {
          this.selectedOrganizer = null;
          this.selectedTopic = null;
          this.loadEventTypes(this.selectedOrganizer, this.selectedTopic);
        }
      });
  }

  loadTopics() {
    this.topicService.getFilterTopics(this.isConferences, this.isMeetups, this.selectedOrganizer)
      .subscribe(topicsData => {
        this.fillTopics(topicsData);

        this.selectedTopic = null;

        this.loadEventTypes(this.selectedOrganizer, this.selectedTopic);
      });
  }

  loadEventTypes(organizer: Organizer, topic: Topic) {
    this.eventTypeService.getEventTypes(this.isConferences, this.isMeetups, organizer, topic)
      .subscribe(data => {
        this.eventTypes = getEventTypesWithSortName(data);
      });
  }

  onEventTypeKindChange() {
    this.loadTopics();
  }

  onOrganizerChange() {
    this.loadTopics();
  }

  onTopicChange() {
    this.loadEventTypes(this.selectedOrganizer, this.selectedTopic);
  }

  onLanguageChange() {
    const currentSelectedOrganizer = this.selectedOrganizer;
    const currentSelectedTopic = this.selectedTopic;

    this.organizerService.getOrganizers()
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.selectedOrganizer = (currentSelectedOrganizer) ? findOrganizerById(currentSelectedOrganizer.id, this.organizers) : null;

        this.topicService.getFilterTopics(this.isConferences, this.isMeetups, this.selectedOrganizer)
          .subscribe(topicsData => {
            this.fillTopics(topicsData);

            if (this.topics.length > 0) {
              this.selectedTopic = (currentSelectedTopic) ? findTopicById(currentSelectedTopic.id, this.topics) : null;
            } else {
              this.selectedTopic = null;
            }

            this.loadEventTypes(this.selectedOrganizer, this.selectedTopic);
          });
      });
  }

  isNoEventTypesFoundVisible() {
    return (this.eventTypes && (this.eventTypes.length === 0));
  }

  isEventTypesListVisible() {
    return (this.eventTypes && (this.eventTypes.length > 0));
  }
}
