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

  getRouteParams(snapshot: ActivatedRouteSnapshot): Params {
    let params = {};
    const stack: ActivatedRouteSnapshot[] = [snapshot.root];

    while (stack.length > 0) {
      const route = stack.pop()!;
      params = {...params, ...route.params};
      stack.push(...route.children);
    }

    return params;
  }

  getResolvedUrl(snapshot: ActivatedRouteSnapshot): string {
    return snapshot.pathFromRoot
      .map(v => v.url.map(segment => segment.toString()).join('/'))
      .join('/');
  }

  getResolvedUrlWithLanguage(snapshot: ActivatedRouteSnapshot, languageCode: string): string {
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

  getConfiguredUrl(snapshot: ActivatedRouteSnapshot): string {
    return '/' + snapshot.pathFromRoot
      .filter(v => v.routeConfig)
      .map(v => v.routeConfig!.path)
      .join('/');
  }

  // TODO: delete
  print(snapshot: ActivatedRouteSnapshot) {
    for (const currentSnapshot of snapshot.pathFromRoot) {
      console.log('params: ' + JSON.stringify(currentSnapshot.params) +
        ', url: ' + JSON.stringify(currentSnapshot.url) +
        ', fragment: ' + currentSnapshot.fragment);
    }
  }

  ngOnInit(): void {
    const params = this.getRouteParams(this.activatedRoute.snapshot);
    const pathLanguageCode = params.language;
    const languageCode = this.localeService.getInitialLanguage(pathLanguageCode);

    // TODO: delete
    this.print(this.activatedRoute.snapshot);
    console.log('resolvedUrl: ' + this.getResolvedUrl(this.activatedRoute.snapshot));
    console.log('resolvedUrlWithLanguage: ' + this.getResolvedUrlWithLanguage(this.activatedRoute.snapshot, 'aa'));
    console.log('configuredUrl: ' + this.getConfiguredUrl(this.activatedRoute.snapshot));

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
