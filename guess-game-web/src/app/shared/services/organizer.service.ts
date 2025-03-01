import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Organizer } from '../models/organizer/organizer.model';
import { MessageService } from '../../modules/message/message.service';

@Injectable({
  providedIn: 'root'
})
export class OrganizerService {
  private baseUrl = 'api/organizer';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getOrganizers(language: string): Observable<Organizer[]> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<Organizer[]>(`${this.baseUrl}/organizers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getDefaultEventOrganizer(language: string): Observable<Organizer> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<Organizer>(`${this.baseUrl}/default-event-organizer`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
