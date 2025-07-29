import { Injectable } from '@angular/core';
import { lastValueFrom, Observable } from 'rxjs';
import { PrimeNG } from 'primeng/config';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class LocaleService {
  public static readonly EN_LANGUAGE_CODE = 'en';
  public static readonly RU_LANGUAGE_CODE = 'ru';

  constructor(public translateService: TranslateService, private primeNG: PrimeNG) {
    this.translateService.addLangs([LocaleService.EN_LANGUAGE_CODE, LocaleService.RU_LANGUAGE_CODE]);
    this.translateService.setFallbackLang(LocaleService.EN_LANGUAGE_CODE);

    this.changeLanguage(LocaleService.EN_LANGUAGE_CODE).then();
  }

  getLanguage(): string {
    return this.translateService.getCurrentLang();
  }

  async changeLanguage(languageCode: string) {
    const language$ = this.translateService.use(languageCode);
    const primeng$ = this.translateService.get(languageCode);

    await lastValueFrom(language$);
    await lastValueFrom(primeng$).then(res => this.primeNG.setTranslation(res));
  }

  isLanguageValid(languageCode: string): boolean {
    return this.translateService.langs.includes(languageCode);
  }

  getDefaultLanguage(): string {
    return LocaleService.EN_LANGUAGE_CODE;
  }

  getInitialLanguage(pathLanguageCode: string | undefined): string {
    if (pathLanguageCode) {
      return this.isLanguageValid(pathLanguageCode) ? pathLanguageCode : this.getDefaultLanguage();
    } else {
      return this.getDefaultLanguage();
    }
  }

  async localeIfNeeded(newLanguageCode: string) {
    if (this.getLanguage() !== newLanguageCode) {
      await this.changeLanguage(newLanguageCode);
    }
  }

  getResourceString(key: string | string[], interpolateParams?: object): Observable<string> {
    return this.translateService.get(key, interpolateParams);
  }

  interpolate(expr: string, params?: any): string {
    return this.translateService.parser.interpolate(expr, params);
  }
}
