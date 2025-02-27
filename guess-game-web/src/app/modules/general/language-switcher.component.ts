import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, Params, Router } from '@angular/router';
import { getRouteParams } from './utility-functions';
import { LocaleService } from '../../shared/services/locale.service';

@Component({
  selector: 'app-language-switcher',
  templateUrl: './language-switcher.component.html',
  standalone: false
})
export class LanguageSwitcherComponent implements OnInit {
  protected readonly LocaleService = LocaleService;

  @Output() reload: EventEmitter<any> = new EventEmitter<any>();

  public selectedLanguageCode: string;

  constructor(private localeService: LocaleService, private router: Router, private activatedRoute: ActivatedRoute) {
  }

  getUrlWithLanguage(snapshot: ActivatedRouteSnapshot, languageCode: string): string {
    return snapshot.pathFromRoot
      .map(v => {
        if (v.paramMap.has('language')) {
          return languageCode;
        } else {
          return v.url.map(segment => segment.toString()).join('/');
        }
      })
      .join('/');
  }

  ngOnInit(): void {
    const params = getRouteParams(this.activatedRoute.snapshot);
    const pathLanguageCode = params.language;
    const languageCode = this.localeService.getInitialLanguage(pathLanguageCode);

    this.localeService.localeIfNeeded(languageCode)
      .then(() => {
        this.selectedLanguageCode = this.localeService.getLanguage();

        if (!this.localeService.isLanguageValid(pathLanguageCode)) {
          this.navigateByLanguageCode(this.localeService.getDefaultLanguage());
        }
      });
  }

  navigateByLanguageCode(languageCode: string) {
    const url = this.getUrlWithLanguage(this.activatedRoute.snapshot, languageCode);

    this.router.navigateByUrl(url).then();
  }

  onLanguageChange(languageCode: string) {
    this.localeService.changeLanguage(languageCode)
      .then(() => {
        this.navigateByLanguageCode(languageCode);
        this.reload.emit();
      });
  }

  isEnChecked(): boolean {
    return this.selectedLanguageCode === LocaleService.EN_LANGUAGE_CODE;
  }

  isRuChecked(): boolean {
    return this.selectedLanguageCode === LocaleService.RU_LANGUAGE_CODE;
  }
}
