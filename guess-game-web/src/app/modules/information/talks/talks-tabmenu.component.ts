import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../../shared/services/locale.service';

@Component({
    selector: 'app-talks-tabmenu',
    templateUrl: './talks-tabmenu.component.html',
    standalone: false
})
export class TalksTabMenuComponent implements OnInit {
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
      {labelKey: 'talks.search.title', routerLink: `/${this.language}/information/talks/search`}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'talk.title', routerLink: `/${this.language}/information/talks/talk/${this.id}`});
    }

    this.items = items;
  }
}
