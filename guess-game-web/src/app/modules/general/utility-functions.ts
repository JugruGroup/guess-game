import { formatDate } from '@angular/common';
import { ActivatedRouteSnapshot, Params } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Event } from '../../shared/models/event/event.model';
import { EventType } from '../../shared/models/event-type/event-type.model';
import { Talk } from '../../shared/models/talk/talk.model';
import { Speaker } from '../../shared/models/speaker/speaker.model';
import { EventTypeMetrics } from '../../shared/models/statistics/event-type/event-type-metrics.model';
import { EventTypeStatistics } from '../../shared/models/statistics/event-type/event-type-statistics.model';
import { Organizer } from '../../shared/models/organizer/organizer.model';
import { Topic } from '../../shared/models/topic/topic.model';
import { OlapEntityStatistics } from '../../shared/models/statistics/olap/statistics/olap-entity-statistics.model';
import { OlapEventTypeMetrics } from '../../shared/models/statistics/olap/metrics/olap-event-type-metrics.model';
import { OlapEntityMetrics } from '../../shared/models/statistics/olap/metrics/olap-entity-metrics.model';

export function isStringEmpty(value: string): boolean {
  return (!value || (value.trim().length <= 0));
}

export function findOrganizerById(id: number, organizers: Organizer[]): Organizer {
  for (let i = 0; i < organizers.length; i++) {
    const organizer: Organizer = organizers[i];

    if (id === organizer.id) {
      return organizer;
    }
  }

  return null;
}

export function findTopicById(id: number, topics: Topic[]): Topic {
  for (let i = 0; i < topics.length; i++) {
    const topic: Topic = topics[i];

    if (id === topic.id) {
      return topic;
    }
  }

  return null;
}

export function findEventTypeById(id: number, eventTypes: EventType[]): EventType {
  for (let i = 0; i < eventTypes.length; i++) {
    const eventType: EventType = eventTypes[i];

    if (id === eventType.id) {
      return eventType;
    }
  }

  return null;
}

export function findEventTypesByIds(ids: number[], eventTypes: EventType[]): EventType[] {
  const result: EventType[] = [];

  for (const id of ids) {
    const eventType = findEventTypeById(id, eventTypes);

    if (eventType) {
      result.push(eventType);
    }
  }

  return result;
}

export function findEventById(id: number, events: Event[]): Event {
  for (let i = 0; i < events.length; i++) {
    const event: Event = events[i];

    if (id === event.id) {
      return event;
    }
  }

  return null;
}

export function findEventsByIds(ids: number[], events: Event[]): Event[] {
  const result: Event[] = [];

  for (const id of ids) {
    const event = findEventById(id, events);

    if (event) {
      result.push(event);
    }
  }

  return result;
}

export function isEventStartDateVisible(startDate: Date): boolean {
  return !!startDate;
}

export function isEventEndDateVisible(startDate: Date, endDate: Date): boolean {
  return (startDate && endDate && (startDate !== endDate));
}

export function isEventHyphenVisible(startDate: Date, endDate: Date): boolean {
  return (isEventStartDateVisible(startDate) && isEventEndDateVisible(startDate, endDate));
}

export function getEventDates(startDate: Date, endDate: Date, translateService: TranslateService): string {
  const isEventStartDateVisibleFlag = isEventStartDateVisible(startDate);
  const isEventHyphenVisibleFlag = isEventHyphenVisible(startDate, endDate);
  const isEventEndDateVisibleFlag = isEventEndDateVisible(startDate, endDate);

  let result = '';

  if (isEventStartDateVisibleFlag) {
    result += formatDate(startDate, 'shortDate', translateService.getCurrentLang(), undefined);
  }

  if (isEventHyphenVisibleFlag) {
    result += '–';
  }

  if (isEventEndDateVisibleFlag) {
    result += formatDate(endDate, 'shortDate', translateService.getCurrentLang(), undefined);
  }

  return result;
}

export function isTalkStartTimeVisible(startTime: Date): boolean {
  return !!startTime;
}

export function isTalkEndTimeVisible(startTime: Date, endTime: Date): boolean {
  return (startTime && endTime && (startTime !== endTime));
}

export function isTalkHyphenVisible(startTime: Date, endTime: Date): boolean {
  return (isTalkStartTimeVisible(startTime) && isTalkEndTimeVisible(startTime, endTime));
}

export function getTalkTimes(startTime: Date, endTime: Date, translateService: TranslateService): string {
  const isTalkStartTimeVisibleFlag = isTalkStartTimeVisible(startTime);
  const isTalkHyphenVisibleFlag = isTalkHyphenVisible(startTime, endTime);
  const isTalkEndTimeVisibleFlag = isTalkEndTimeVisible(startTime, endTime);

  let result = '';

  if (isTalkStartTimeVisibleFlag) {
    result += formatDate(startTime, 'HH:mm', translateService.getCurrentLang(), undefined);
  }

  if (isTalkHyphenVisibleFlag) {
    result += '–';
  }

  if (isTalkEndTimeVisibleFlag) {
    result += formatDate(endTime, 'HH:mm', translateService.getCurrentLang(), undefined);
  }

  return result;
}

export function getEventDisplayName(event: Event, translateService: TranslateService): string {
  let displayName = event.name;

  const eventDates: string[] = [];

  if (event.days) {
    event.days.map(ed => {
        const eventDaysDates = getEventDates(ed.startDate, ed.endDate, translateService);

        if ((eventDaysDates) && (eventDaysDates.length > 0)) {
          eventDates.push(eventDaysDates);
        }
      }
    );
  }

  if (eventDates.length > 0) {
    displayName += ` (${eventDates.join(', ')})`;
  }

  return displayName;
}

function getSortName(conference: boolean, organizerName: string, name: string): string {
  return (conference ? '0' : '1') + organizerName + name;
}

export function getEventTypesWithSortName(eventTypes: EventType[]): EventType[] {
  if (eventTypes) {
    for (let i = 0; i < eventTypes.length; i++) {
      const eventType: EventType = eventTypes[i];

      eventType.sortName = getSortName(eventType.conference, eventType.organizerName, eventType.name);
    }
  }

  return eventTypes;
}

export function getEventTypeStatisticsWithSortName(eventTypeStatistics: EventTypeStatistics): EventTypeStatistics {
  const eventTypeMetricsList: EventTypeMetrics[] = eventTypeStatistics.eventTypeMetricsList;

  if (eventTypeMetricsList) {
    for (let i = 0; i < eventTypeMetricsList.length; i++) {
      const eventTypeMetrics: EventTypeMetrics = eventTypeMetricsList[i];

      eventTypeMetrics.sortName = getSortName(eventTypeMetrics.conference, eventTypeMetrics.organizerName, eventTypeMetrics.displayName);
    }
  }

  return eventTypeStatistics;
}

export function getOlapEventTypeStatisticsWithSortName(eventTypeStatistics: OlapEntityStatistics<number, OlapEventTypeMetrics>): OlapEntityStatistics<number, OlapEventTypeMetrics> {
  const eventTypeMetricsList: OlapEventTypeMetrics[] = eventTypeStatistics.metricsList;

  if (eventTypeMetricsList) {
    for (let i = 0; i < eventTypeMetricsList.length; i++) {
      const eventTypeMetrics: OlapEventTypeMetrics = eventTypeMetricsList[i];

      eventTypeMetrics.sortName = getSortName(eventTypeMetrics.conference, eventTypeMetrics.organizerName, eventTypeMetrics.name);
    }
  }

  return eventTypeStatistics;
}

export function getEventsWithBriefDisplayName(events: Event[]): Event[] {
  if (events) {
    for (let i = 0; i < events.length; i++) {
      events[i].displayName = events[i].name;
    }
  }

  return events;
}

export function getEventsWithFullDisplayName(events: Event[], translateService: TranslateService): Event[] {
  if (events) {
    for (let i = 0; i < events.length; i++) {
      events[i].displayName = getEventDisplayName(events[i], translateService);
    }
  }

  return events;
}

export function getTalksWithSpeakersString(talks: Talk[]): Talk[] {
  if (talks) {
    talks.forEach(t => {
      t.speakersString = t.speakers.map(s => s.displayName).join(', ');
    });
  }

  return talks;
}

export function getSpeakersWithCompaniesString(speakers: Speaker[]): Speaker[] {
  if (speakers) {
    speakers.forEach(s => {
      s.companiesString = s.companies.map(c => c.name).join(', ');
    });
  }

  return speakers;
}

export function getTalksWithMaterialsOrderNumber(talks: Talk[]): Talk[] {
  if (talks) {
    talks.forEach(t => {
      let materialsOrderNumber = 0;

      if (t.videoLinks) {
        materialsOrderNumber += t.videoLinks.length * 100;
      }

      if (t.presentationLinks) {
        materialsOrderNumber += t.presentationLinks.length * 10;
      }

      if (t.materialLinks) {
        materialsOrderNumber += t.materialLinks.length;
      }

      t.materialsOrderNumber = materialsOrderNumber;
    });
  }

  return talks;
}

export function getTalkClassnameByFilename(filename: string): string {
  return /.pdf$/i.test(filename) ? 'far fa-file-pdf' : 'far fa-file';
}

export function getFixedMeasureValues(measureValues: number[], quantity: number): number[] {
  if (measureValues) {
    if (measureValues.length < quantity) {
      return measureValues.concat(Array(quantity - measureValues.length).fill(0))
    } else if (measureValues.length > quantity) {
      return measureValues.slice(0, quantity);
    } else {
      return measureValues;
    }
  } else {
    return Array(quantity).fill(0);
  }
}

export function fixOlapEntityStatistics<T, S extends OlapEntityMetrics>(entityStatistics: OlapEntityStatistics<T, S>,
                                                                        measureValueFieldNamePrefix: string,
                                                                        fixCumulative = true) {
  const quantity = entityStatistics.dimensionValues.length;

  entityStatistics.metricsList.forEach(metrics => {
    metrics.measureValues = getFixedMeasureValues(metrics.measureValues, quantity);

    if (fixCumulative) {
      metrics.cumulativeMeasureValues = getFixedMeasureValues(metrics.cumulativeMeasureValues, quantity);
    }

    if (measureValueFieldNamePrefix) {
      for (let i = 0; i < metrics.measureValues.length; i++) {
        metrics[measureValueFieldNamePrefix + i] = metrics.measureValues[i];
      }
    }
  });

  entityStatistics.totals.measureValues = getFixedMeasureValues(entityStatistics.totals.measureValues, quantity);

  if (fixCumulative) {
    entityStatistics.totals.cumulativeMeasureValues = getFixedMeasureValues(entityStatistics.totals.cumulativeMeasureValues, quantity);
  }
}

export function getColorByIndex(index: number): string {
  const DEFAULT_COLORS = [
    '#3366cc', '#dc3912', '#ff9900', '#109618', '#990099',
    '#3b3eac', '#0099c6', '#dd4477', '#66aa00', '#b82e2e',
    '#316395', '#994499', '#22aa99', '#aaaa11', '#6633cc',
    '#e67300', '#8b0707', '#329262', '#5574a6', '#3b3eac'];

  return DEFAULT_COLORS[index % DEFAULT_COLORS.length];
}

export function hexToRgbA(hex: string, alpha: string) {
  let c;

  if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
    c = hex.substring(1).split('');

    if (c.length == 3) {
      c = [c[0], c[0], c[1], c[1], c[2], c[2]];
    }

    c = '0x' + c.join('');

    return 'rgba(' + [(c >> 16) & 255, (c >> 8) & 255, c & 255].join(',') + ',' + alpha + ')';
  }

  throw new Error('Bad Hex');
}

export function getRouteParams(snapshot: ActivatedRouteSnapshot): Params {
  let params = {};
  const stack: ActivatedRouteSnapshot[] = [snapshot.root];

  while (stack.length > 0) {
    const route = stack.pop()!;
    params = {...params, ...route.params};
    stack.push(...route.children);
  }

  return params;
}
