import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../../shared/services/locale.service';

@Component({
  selector: 'app-statistics-tabmenu',
  templateUrl: './statistics-tabmenu.component.html',
  standalone: false
})
export class StatisticsTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;

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
    this.items = [
      {labelKey: 'statistics.eventTypes.title', routerLink: `/${this.language}/information/statistics/event-types`},
      {labelKey: 'statistics.events.title', routerLink: `/${this.language}/information/statistics/events`},
      {labelKey: 'statistics.speakers.title', routerLink: `/${this.language}/information/statistics/speakers`},
      {labelKey: 'statistics.companies.title', routerLink: `/${this.language}/information/statistics/companies`},
      {labelKey: 'statistics.olap.title', routerLink: `/${this.language}/information/statistics/olap`}
    ];
  }
}
