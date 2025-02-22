import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, Params, Router } from '@angular/router';
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

  collectRouteParams(snapshot: ActivatedRouteSnapshot): Params {
    let params = {};
    const stack: ActivatedRouteSnapshot[] = [snapshot.root];

    while (stack.length > 0) {
      const route = stack.pop()!;
      params = {...params, ...route.params};
      stack.push(...route.children);
    }

    return params;
  }

  ngOnInit(): void {
    const params = this.collectRouteParams(this.activatedRoute.snapshot);
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
    //TODO: change
    // this.router.navigateByUrl(`/${languageCode}/${this.pageCode}`).then();
  }

  onLanguageChange(languageCode: string) {
    this.localeService.changeLanguage(languageCode)
      .then(() => this.navigateByLanguageCode(languageCode));

    // TODO: change
    // this.localeService.setLanguage(language)
    //   .subscribe(() => {
    //       this.reload.emit();
    //     }
    //   );
  }

  isEnChecked(): boolean {
    return this.selectedLanguageCode === LocaleService.EN_LANGUAGE_CODE;
  }

  isRuChecked(): boolean {
    return this.selectedLanguageCode === LocaleService.RU_LANGUAGE_CODE;
  }
}
