import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EventType } from '../models/event-type/event-type.model';
import { Event } from '../models/event/event.model';
import { Talk } from '../models/talk/talk.model';
import { TalkDetails } from '../models/talk/talk-details.model';
import { Topic } from '../models/topic/topic.model';
import { MessageService } from '../../modules/message/message.service';

@Injectable({
  providedIn: 'root'
})
export class TalkService {
  private baseUrl = 'api/talk';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getTalks(eventType: EventType, event: Event, talkName: string, speakerName: string, topic: Topic, language: string): Observable<Talk[]> {
    let params = new HttpParams();
    if (eventType) {
      params = params.set('eventTypeId', eventType.id.toString());
    }
    if (event) {
      params = params.set('eventId', event.id.toString());
    }
    if (talkName) {
      params = params.set('talkName', talkName);
    }
    if (speakerName) {
      params = params.set('speakerName', speakerName);
    }
    if (topic) {
      params = params.set('topicId', topic.id.toString());
    }
    if (language) {
      params = params.set('talkLanguage', language);
    }

    return this.http.get<Talk[]>(`${this.baseUrl}/talks`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getTalk(id: number): Observable<TalkDetails> {
    return this.http.get<TalkDetails>(`${this.baseUrl}/talk/${id}`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
