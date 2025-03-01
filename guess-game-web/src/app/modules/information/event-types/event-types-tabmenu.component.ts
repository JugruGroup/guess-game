import { Component, Input, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { LocaleService } from '../../../shared/services/locale.service';

@Component({
    selector: 'app-event-types-tabmenu',
    templateUrl: './event-types-tabmenu.component.html',
    standalone: false
})
export class EventTypesTabMenuComponent implements OnInit {
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
    this.items = [
      {labelKey: 'eventTypes.search.title', routerLink: `/${this.language}/information/event-types/search`}
    ];

    if (!isNaN(this.id)) {
      this.items.push({labelKey: 'eventType.title', routerLink: `/${this.language}/information/event-types/event-type/${this.id}`});
    }
  }
}
