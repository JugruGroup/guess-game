import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Company } from '../models/company/company.model';
import { CompanyDetails } from '../models/company/company-details.model';
import { CompanySearchResult } from '../models/company/company-search-result.model';
import { SelectedEntities } from '../models/common/selected-entities.model';
import { MessageService } from '../../modules/message/message.service';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private baseUrl = 'api/company';

  constructor(private http: HttpClient, private messageService: MessageService) {
  }

  getCompaniesByFirstLetter(digit: boolean, firstLetter: string, language: string): Observable<CompanySearchResult[]> {
    let params = new HttpParams()
      .set('digit', digit.toString())
      .set('language', language);

    if (firstLetter) {
      params = params.set('firstLetter', firstLetter);
    }

    return this.http.get<CompanySearchResult[]>(`${this.baseUrl}/first-letter-companies`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCompaniesByFirstLetters(firstLetters: string, language: string): Observable<Company[]> {
    const params = new HttpParams()
      .set('firstLetters', firstLetters)
      .set('language', language);

    return this.http.get<Company[]>(`${this.baseUrl}/first-letters-companies`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getSelectedCompanies(selectedEntities: SelectedEntities, language: string): Observable<Company[]> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.post<Company[]>(`${this.baseUrl}/selected-companies`, selectedEntities, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCompanyNamesByFirstLetters(firstLetters: string, language: string): Observable<string[]> {
    const params = new HttpParams()
      .set('firstLetters', firstLetters)
      .set('language', language);

    return this.http.get<string[]>(`${this.baseUrl}/first-letters-company-names`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCompanies(name: string, language: string): Observable<CompanySearchResult[]> {
    let params = new HttpParams()
      .set('language', language);

    if (name) {
      params = params.set('name', name.toString());
    }

    return this.http.get<CompanySearchResult[]>(`${this.baseUrl}/companies`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  getCompany(id: number, language: string): Observable<CompanyDetails> {
    const params = new HttpParams()
      .set('language', language);

    return this.http.get<CompanyDetails>(`${this.baseUrl}/company/${id}`, {params: params})
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }
}
