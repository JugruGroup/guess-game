import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Speaker } from '../models/speaker/speaker.model';
import { SpeakerDetails } from '../models/speaker/speaker-details.model';
import { SelectedEntities } from '../models/common/selected-entities.model';
import { MessageService } from '../../modules/message/message.service';

@Injectable({
  providedIn: 'root'
})
export class SpeakerService {
  private baseUrl = 'api/speaker';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getSpeakersByFirstLetter(firstLetter: string, language: string): Observable<Speaker[]> {
    const params = new HttpParams()
      .set('firstLetter', firstLetter)
      .set('language', language);

    return this.http.get<Speaker[]>(`${this.baseUrl}/first-letter-speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakersByFirstLetters(firstLetters: string): Observable<Speaker[]> {
    const params = new HttpParams()
      .set('firstLetters', firstLetters);

    return this.http.get<Speaker[]>(`${this.baseUrl}/first-letters-speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSelectedSpeakers(selectedEntities: SelectedEntities): Observable<Speaker[]> {
    return this.http.post<Speaker[]>(`${this.baseUrl}/selected-speakers`, selectedEntities)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakers(name: string, company: string, twitter: string, gitHub: string, habr: string, description: string,
              javaChampion: boolean, mvp: boolean): Observable<Speaker[]> {
    let params = new HttpParams()
      .set('javaChampion', javaChampion.toString())
      .set('mvp', mvp.toString());
    if (name) {
      params = params.set('name', name.toString());
    }
    if (company) {
      params = params.set('company', company.toString());
    }
    if (twitter) {
      params = params.set('twitter', twitter.toString());
    }
    if (gitHub) {
      params = params.set('gitHub', gitHub.toString());
    }
    if (habr) {
      params = params.set('habr', habr.toString());
    }
    if (description) {
      params = params.set('description', description.toString());
    }

    return this.http.get<Speaker[]>(`${this.baseUrl}/speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeaker(id: number): Observable<SpeakerDetails> {
    return this.http.get<SpeakerDetails>(`${this.baseUrl}/speaker/${id}`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
