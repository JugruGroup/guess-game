import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../../shared/services/locale.service';

@Component({
    selector: 'app-companies-tabmenu',
    templateUrl: './companies-tabmenu.component.html',
    standalone: false
})
export class CompaniesTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;
  @Input() private id: number;

  public items: MenuItem[] = [];
  public language: string;

  constructor(private translateService: TranslateService, private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.initItems();

    this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.initItems();
      });
  }

  initItems() {
    const items = [
      {labelKey: 'companies.list.title', routerLink: `/${this.language}/information/companies/list`},
      {labelKey: 'companies.search.title', routerLink: `/${this.language}/information/companies/search`}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'company.title', routerLink: `/${this.language}/information/companies/company/${this.id}`});
    }

    this.items = items;
  }
}
