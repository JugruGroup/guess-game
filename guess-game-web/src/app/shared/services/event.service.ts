import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Event } from '../models/event/event.model';
import { EventType } from '../models/event-type/event-type.model';
import { EventDetails } from '../models/event/event-details.model';
import { MessageService } from '../../modules/message/message.service';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private baseUrl = 'api/event';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getEvents(isConferences: boolean, isMeetups: boolean, eventType: EventType): Observable<Event[]> {
    let params = new HttpParams()
      .set('conferences', isConferences.toString())
      .set('meetups', isMeetups.toString());

    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }

    return this.http.get<Event[]>(`${this.baseUrl}/events`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getDefaultEvent(): Observable<Event> {
    return this.http.get<Event>(`${this.baseUrl}/default-event`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getDefaultEventHomeInfo(): Observable<Event> {
    return this.http.get<Event>(`${this.baseUrl}/default-event-home-info`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getDefaultConference(): Observable<Event> {
    return this.http.get<Event>(`${this.baseUrl}/default-conference`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getEvent(id: number): Observable<EventDetails> {
    return this.http.get<EventDetails>(`${this.baseUrl}/event/${id}`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
