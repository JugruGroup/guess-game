import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { CompanySearchResult } from '../../../shared/models/company/company-search-result.model';
import { CompanyService } from '../../../shared/services/company.service';
import { LocaleService } from '../../../shared/services/locale.service';
import { isStringEmpty } from '../../general/utility-functions';

@Component({
    selector: 'app-companies-search',
    templateUrl: './companies-search.component.html',
    standalone: false
})
export class CompaniesSearchComponent implements OnInit, OnDestroy {
  public name: string;

  public companies: CompanySearchResult[] = [];

  private searched = false;
  public multiSortMeta: any[] = [];

  public language: string;
  private languageSubscription: Subscription;

  constructor(private companyService: CompanyService, public translateService: TranslateService,
              private localeService: LocaleService) {
    this.multiSortMeta.push({field: 'name', order: 1});
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.languageSubscription = this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.onLanguageChange();
      });
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  loadCompanies(name: string) {
    this.companyService.getCompanies(name, this.language)
      .subscribe(data => {
        this.companies = data;
        this.searched = true;
      });
  }

  onLanguageChange() {
    this.search();
  }

  onFilterChange() {
    this.searched = false;
  }

  search() {
    if (!this.isSearchDisabled()) {
      this.loadCompanies(this.name);
    }
  }

  clear() {
    this.name = undefined;
    this.companies = [];

    this.searched = false;
  }

  isSearchDisabled(): boolean {
    return isStringEmpty(this.name);
  }

  isNoCompaniesFoundVisible() {
    return (this.searched && (this.companies.length === 0));
  }

  isCompaniesListVisible() {
    return (this.searched && (this.companies.length > 0));
  }
}
