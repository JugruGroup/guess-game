import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from '../../modules/message/message.service';
import { Organizer } from '../models/organizer/organizer.model';
import { Topic } from '../models/topic/topic.model';

@Injectable({
  providedIn: 'root'
})
export class TopicService {
  private baseUrl = 'api/topic';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getTopics(language: string) {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<Topic[]>(`${this.baseUrl}/topics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getFilterTopics(isConferences: boolean, isMeetups: boolean, organizer: Organizer, language: string): Observable<Topic[]> {
    let params = new HttpParams()
      .set('conferences', isConferences.toString())
      .set('meetups', isMeetups.toString())
      .set('language', language);

    if (organizer) {
      params = params.set('organizerId', organizer.id.toString());
    }

    return this.http.get<Topic[]>(`${this.baseUrl}/filter-topics`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
