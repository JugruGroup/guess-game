import { Component, Input, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { LocaleService } from '../../../shared/services/locale.service';

@Component({
  selector: 'app-events-tabmenu',
  templateUrl: './events-tabmenu.component.html',
  standalone: false
})
export class EventsTabMenuComponent implements OnInit {
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
      {labelKey: 'events.search.title', routerLink: `/${this.language}/information/events/search`}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'event.title', routerLink: `/${this.language}/information/events/event/${this.id}`});
    }

    this.items = items;
  }
}
