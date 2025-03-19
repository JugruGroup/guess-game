import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { MessageService } from '../../modules/message/message.service';
import { StartParameters } from '../models/start/start-parameters.model';
import { GameState } from '../models/game-state.model';
import { PhotoNames } from '../models/guess/photo-names.model';
import { NamePhotos } from '../models/guess/name-photos.model';
import { SpeakersTalks } from '../models/guess/speakers-talks.model';
import { TalkSpeakers } from '../models/guess/talk-speakers.model';
import { SpeakerCompanies } from '../models/guess/speaker-companies.model';
import { CompanySpeakers } from '../models/guess/company-speakers.model';
import { SpeakerAccounts } from '../models/guess/speaker-accounts.model';
import { AccountSpeakers } from '../models/guess/account-speakers.model';
import { SpeakerTagClouds } from '../models/guess/speaker-tag-clouds.model';
import { TagCloudSpeakers } from '../models/guess/tag-cloud-speakers.model';

@Injectable({
  providedIn: 'root'
})
export class StateService {
  private baseUrl = 'api/state';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  setStartParameters(startParameters: StartParameters): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/parameters`, startParameters)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  deleteStartParameters(): Observable<string> {
    return this.http.delete<string>(`${this.baseUrl}/parameters`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getState(): Observable<GameState> {
    return this.http.get<GameState>(`${this.baseUrl}/state`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  setState(state: GameState): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/state`, state)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getPhotoNames(language: string): Observable<PhotoNames> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<PhotoNames>(`${this.baseUrl}/photo-names`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getNamePhotos(language: string): Observable<NamePhotos> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<NamePhotos>(`${this.baseUrl}/name-photos`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakerTalks(language: string): Observable<SpeakersTalks> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<SpeakersTalks>(`${this.baseUrl}/speaker-talks`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getTalkSpeakers(language: string): Observable<TalkSpeakers> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<TalkSpeakers>(`${this.baseUrl}/talk-speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakerCompanies(language: string): Observable<SpeakerCompanies> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<SpeakerCompanies>(`${this.baseUrl}/speaker-companies`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCompanySpeakers(language: string): Observable<CompanySpeakers> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<CompanySpeakers>(`${this.baseUrl}/company-speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakerAccounts(language: string): Observable<SpeakerAccounts> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<SpeakerAccounts>(`${this.baseUrl}/speaker-accounts`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getAccountSpeakers(language: string): Observable<AccountSpeakers> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<AccountSpeakers>(`${this.baseUrl}/account-speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSpeakerTagClouds(language: string): Observable<SpeakerTagClouds> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<SpeakerTagClouds>(`${this.baseUrl}/speaker-tag-clouds`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getTagCloudSpeakers(language: string): Observable<TagCloudSpeakers> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<TagCloudSpeakers>(`${this.baseUrl}/tag-cloud-speakers`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
