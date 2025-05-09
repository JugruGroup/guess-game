import { Component, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SelectItem } from 'primeng/api';
import { Subscription } from 'rxjs';
import { EventTypeStatistics } from '../../../../shared/models/statistics/event-type/event-type-statistics.model';
import { LocaleService } from '../../../../shared/services/locale.service';
import { Organizer } from '../../../../shared/models/organizer/organizer.model';
import { OrganizerService } from '../../../../shared/services/organizer.service';
import { StatisticsService } from '../../../../shared/services/statistics.service';
import { Topic } from '../../../../shared/models/topic/topic.model';
import { TopicService } from '../../../../shared/services/topic.service';
import { findOrganizerById, findTopicById, getEventTypeStatisticsWithSortName } from '../../../general/utility-functions';

@Component({
    selector: 'app-event-type-statistics',
    templateUrl: './event-type-statistics.component.html',
    standalone: false
})
export class EventTypeStatisticsComponent implements OnInit, OnDestroy {
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

  public eventTypeStatistics = new EventTypeStatistics();
  public multiSortMeta: any[] = [];

  public language: string;
  private languageSubscription: Subscription;

  constructor(private statisticsService: StatisticsService, public organizerService: OrganizerService,
              private topicService: TopicService, public translateService: TranslateService,
              private localeService: LocaleService) {
    this.multiSortMeta.push({field: 'sortName', order: 1});
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

  fillTopics(topics: Topic[]) {
    this.topics = topics;
    this.topicSelectItems = this.topics.map(t => {
        return {label: t.name, value: t};
      }
    );
  }

  loadOrganizers() {
    const currentSelectedTopic = this.selectedTopic;

    this.organizerService.getOrganizers(this.language)
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        if (this.organizers.length > 0) {
          this.organizerService.getDefaultEventOrganizer(this.language)
            .subscribe(defaultOrganizerData => {
              const selectedOrganizer = (defaultOrganizerData) ? findOrganizerById(defaultOrganizerData.id, this.organizers) : null;
              this.selectedOrganizer = (selectedOrganizer) ? selectedOrganizer : null;

              this.topicService.getFilterTopics(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
                .subscribe(topicsData => {
                  this.fillTopics(topicsData);

                  if (this.topics.length > 0) {
                    this.selectedTopic = (currentSelectedTopic) ? findTopicById(currentSelectedTopic.id, this.topics) : null;
                  } else {
                    this.selectedTopic = null;
                  }

                  this.loadEventTypeStatistics(this.selectedOrganizer, this.selectedTopic);
                });
            });
        } else {
          this.selectedOrganizer = null;
          this.selectedTopic = null;
          this.loadEventTypeStatistics(this.selectedOrganizer, this.selectedTopic);
        }
      });
  }

  loadTopics() {
    this.topicService.getFilterTopics(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
      .subscribe(topicsData => {
        this.fillTopics(topicsData);

        this.selectedTopic = null;

        this.loadEventTypeStatistics(this.selectedOrganizer, this.selectedTopic);
      });
  }

  loadEventTypeStatistics(organizer: Organizer, topic: Topic) {
    this.statisticsService.getEventTypeStatistics(this.isConferences, this.isMeetups, organizer, topic, this.language)
      .subscribe(data => {
          this.eventTypeStatistics = getEventTypeStatisticsWithSortName(data);
        });
  }

  onEventTypeKindChange() {
    this.loadTopics();
  }

  onOrganizerChange() {
    this.loadTopics();
  }

  onTopicChange() {
    this.loadEventTypeStatistics(this.selectedOrganizer, this.selectedTopic);
  }

  onLanguageChange() {
    const currentSelectedOrganizer = this.selectedOrganizer;
    const currentSelectedTopic = this.selectedTopic;

    this.organizerService.getOrganizers(this.language)
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.selectedOrganizer = (currentSelectedOrganizer) ? findOrganizerById(currentSelectedOrganizer.id, this.organizers) : null;

        this.topicService.getFilterTopics(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
          .subscribe(topicsData => {
            this.fillTopics(topicsData);

            if (this.topics.length > 0) {
              this.selectedTopic = (currentSelectedTopic) ? findTopicById(currentSelectedTopic.id, this.topics) : null;
            } else {
              this.selectedTopic = null;
            }

            this.loadEventTypeStatistics(this.selectedOrganizer, this.selectedTopic);
          });
      });
  }

  isNoEventTypesDataFoundVisible() {
    return (this.eventTypeStatistics?.eventTypeMetricsList && (this.eventTypeStatistics.eventTypeMetricsList.length === 0));
  }

  isEventTypesListVisible() {
    return (this.eventTypeStatistics?.eventTypeMetricsList && (this.eventTypeStatistics.eventTypeMetricsList.length > 0));
  }
}
