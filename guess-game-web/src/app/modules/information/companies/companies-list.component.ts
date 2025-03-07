import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { CompanySearchResult } from '../../../shared/models/company/company-search-result.model';
import { CompanyService } from '../../../shared/services/company.service';
import { EntitiesListComponent } from '../entity-list.component';
import { LocaleService } from '../../../shared/services/locale.service';

@Component({
    selector: 'app-companies-list',
    templateUrl: './companies-list.component.html',
    standalone: false
})
export class CompaniesListComponent extends EntitiesListComponent implements OnInit {
  private readonly DEFAULT_IS_DIGIT = false;
  public readonly DIGIT_BUTTON_TEXT = '0-9';

  public isDigit = this.DEFAULT_IS_DIGIT;
  public companies: CompanySearchResult[] = [];

  constructor(private companyService: CompanyService, translateService: TranslateService, localeService: LocaleService) {
    super(translateService, localeService);
  }

  ngOnInit(): void {
    this.loadCompanies(this.isDigit, this.selectedLetter);

    this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.onLanguageChange();
      });
  }

  loadCompanies(isDigit: boolean, letter: string) {
    this.companyService.getCompaniesByFirstLetter(isDigit, letter, this.language)
      .subscribe(data => {
        this.companies = data;
      });
  }

  isCurrentLetter(letter: string) {
    return !this.isDigit && (this.selectedLetter === letter);
  }

  changeDigitAndLetter(isDigit: boolean, letter: string) {
    this.isDigit = isDigit;
    this.selectedLetter = letter;

    this.paginatorFirst = 0;

    this.loadCompanies(isDigit, letter);
  }

  changeLetter(letter: string) {
    this.changeDigitAndLetter(false, letter);
  }

  changeDigit() {
    this.changeDigitAndLetter(true, null);
  }

  isCompaniesListVisible() {
    return (this.companies.length > 0);
  }
}
