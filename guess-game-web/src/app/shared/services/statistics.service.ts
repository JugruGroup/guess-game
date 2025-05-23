import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CompanyStatistics } from '../models/statistics/company/company-statistics.model';
import { EventPlaceStatistics } from '../models/statistics/event-place/event-place-statistics.model';
import { EventType } from '../models/event-type/event-type.model';
import { EventTypeStatistics } from '../models/statistics/event-type/event-type-statistics.model';
import { EventStatistics } from '../models/statistics/event/event-statistics.model';
import { MessageService } from '../../modules/message/message.service';
import { OlapCityMetrics } from '../models/statistics/olap/metrics/olap-city-metrics.model';
import { OlapCityParameters } from '../models/statistics/olap/parameters/olap-city-parameters.model';
import { OlapCubeType } from '../models/statistics/olap/olap-cube-type.model';
import { OlapEntityStatistics } from '../models/statistics/olap/statistics/olap-entity-statistics.model';
import { OlapEventTypeMetrics } from '../models/statistics/olap/metrics/olap-event-type-metrics.model';
import { OlapEventTypeParameters } from '../models/statistics/olap/parameters/olap-event-type-parameters.model';
import { OlapMeasureType } from '../models/statistics/olap/olap-measure-type.model';
import { OlapParameters } from '../models/statistics/olap/parameters/olap-parameters.model';
import { OlapSpeakerMetrics } from '../models/statistics/olap/metrics/olap-speaker-metrics.model';
import { OlapSpeakerParameters } from '../models/statistics/olap/parameters/olap-speaker-parameters.model';
import { OlapStatistics } from '../models/statistics/olap/statistics/olap-statistics.model';
import { Organizer } from '../models/organizer/organizer.model';
import { SpeakerStatistics } from '../models/statistics/speaker/speaker-statistics.model';
import { Topic } from '../models/topic/topic.model';

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private baseUrl = 'api/statistics';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getEventTypeStatistics(conferences: boolean, meetups: boolean, organizer: Organizer, topic: Topic,
                         language: string): Observable<EventTypeStatistics> {
    let params = new HttpParams()
      .set('conferences', conferences.toString())
      .set('meetups', meetups.toString())
      .set('language', language);

    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }

    if (topic) {
      params = params.set('topicId', topic.id.toString());
    }

    return this.http.get<EventTypeStatistics>(`${this.baseUrl}/event-type-statistics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getEventStatistics(conferences: boolean, meetups: boolean, organizer: Organizer, eventType: EventType,
                     language: string): Observable<EventStatistics> {
    let params = new HttpParams()
      .set('conferences', conferences.toString())
      .set('meetups', meetups.toString())
      .set('language', language);

    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }

    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }

    return this.http.get<EventStatistics>(`${this.baseUrl}/event-statistics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getEventPlaceStatistics(conferences: boolean, meetups: boolean, organizer: Organizer, eventType: EventType,
                     language: string): Observable<EventPlaceStatistics> {
    let params = new HttpParams()
      .set('conferences', conferences.toString())
      .set('meetups', meetups.toString())
      .set('language', language);

    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }

    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }

    return this.http.get<EventPlaceStatistics>(`${this.baseUrl}/event-place-statistics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakerStatistics(conferences: boolean, meetups: boolean, organizer: Organizer, eventType: EventType,
                       language: string): Observable<SpeakerStatistics> {
    let params = new HttpParams()
      .set('conferences', conferences.toString())
      .set('meetups', meetups.toString())
      .set('language', language);

    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }

    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }

    return this.http.get<SpeakerStatistics>(`${this.baseUrl}/speaker-statistics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCompanyStatistics(conferences: boolean, meetups: boolean, organizer: Organizer, eventType: EventType,
                       language: string): Observable<CompanyStatistics> {
    let params = new HttpParams()
      .set('conferences', conferences.toString())
      .set('meetups', meetups.toString())
      .set('language', language);

    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }

    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }

    return this.http.get<CompanyStatistics>(`${this.baseUrl}/company-statistics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCubeTypes(): Observable<OlapCubeType[]> {
    return this.http.get<OlapCubeType[]>(`${this.baseUrl}/cube-types`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getMeasureTypes(cubeType: OlapCubeType): Observable<OlapMeasureType[]> {
    const params = new HttpParams()
      .set('cubeType', (cubeType) ? cubeType.toString() : null);

    return this.http.get<OlapMeasureType[]>(`${this.baseUrl}/measure-types`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getOlapStatistics(olapParameters: OlapParameters, language: string): Observable<OlapStatistics> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.post<OlapStatistics>(`${this.baseUrl}/olap-statistics`, olapParameters, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getOlapEventTypeStatistics(olapParameters: OlapEventTypeParameters, language: string):
    Observable<OlapEntityStatistics<number, OlapEventTypeMetrics>> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.post<OlapEntityStatistics<number, OlapEventTypeMetrics>>(`${this.baseUrl}/olap-event-type-statistics`,
      olapParameters, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getOlapSpeakerStatistics(olapParameters: OlapSpeakerParameters, language: string):
    Observable<OlapEntityStatistics<number, OlapSpeakerMetrics>> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.post<OlapEntityStatistics<number, OlapSpeakerMetrics>>(`${this.baseUrl}/olap-speaker-statistics`,
      olapParameters, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getOlapCityStatistics(olapParameters: OlapCityParameters, language: string):
    Observable<OlapEntityStatistics<number, OlapCityMetrics>> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.post<OlapEntityStatistics<number, OlapCityMetrics>>(`${this.baseUrl}/olap-city-statistics`,
      olapParameters, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
