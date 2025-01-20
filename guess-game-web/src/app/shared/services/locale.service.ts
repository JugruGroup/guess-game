import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { lastValueFrom, Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { PrimeNG } from 'primeng/config';
import { MessageService } from '../../modules/message/message.service';
import { Language } from '../models/language.model';

@Injectable({
  providedIn: 'root'
})
export class LocaleService {
  private readonly EN_LANGUAGE_CODE = 'en';
  private readonly RU_LANGUAGE_CODE = 'ru';

  private baseUrl = 'api/locale';

  constructor(private http: HttpClient, private messageService: MessageService, public translateService: TranslateService,
              private primeNG: PrimeNG) {
    this.translateService.addLangs([this.EN_LANGUAGE_CODE, this.RU_LANGUAGE_CODE]);
    this.translateService.setDefaultLang(this.EN_LANGUAGE_CODE);

    this.getLanguageAndChangeInterfaceLanguage().then();
  }

  getLanguage(): Observable<Language> {
    return this.http.get<Language>(`${this.baseUrl}/language`)
      .pipe(
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  setLanguage(language: Language): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/language`, language)
      .pipe(
        map(data => {
            this.changeInterfaceLanguage(language).then();
            return data;
          }
        ),
        catchError((response: Response) => {
          this.messageService.reportMessage(response);
          throw response;
        })
      );
  }

  async getLanguageAndChangeInterfaceLanguage() {
    const language$ = this.getLanguage();
    const language = await lastValueFrom(language$);

    await this.changeInterfaceLanguage(language);
  }

  async changeInterfaceLanguage(language: Language) {
    const languageCode = (language === Language.Russian) ? this.RU_LANGUAGE_CODE : this.EN_LANGUAGE_CODE;
    const language$ = this.translateService.use(languageCode);
    const primeng$ = this.translateService.get(languageCode);

    await lastValueFrom(language$);
    await lastValueFrom(primeng$).then(res => this.primeNG.setTranslation(res));
  }
}
