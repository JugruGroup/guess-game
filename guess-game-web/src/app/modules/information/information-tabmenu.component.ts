import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../shared/services/locale.service';

@Component({
    selector: 'app-information-tabmenu',
    templateUrl: './information-tabmenu.component.html',
    standalone: false
})
export class InformationTabMenuComponent implements OnInit, OnDestroy {
  @Input() public items: MenuItem[] = [];
  @Input() public activeIndex: number;

  public localItems: MenuItem[] = [];
  private languageSubscription: Subscription;

  constructor(private translateService: TranslateService, private localeService: LocaleService) {
  }

  ngOnInit(): void {
    this.initMenuItems();

    this.languageSubscription = this.translateService.onLangChange
      .subscribe(() => this.initMenuItems());
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  initMenuItems() {
    const language = this.localeService.getLanguage();
    const keys = this.items.map(i => i.labelKey);

    this.translateService.get(keys)
      .subscribe(labels => {
        const newItems: MenuItem[] = [];

        this.items.forEach(i => {
          i.label = labels[i.labelKey];
          i.routerLink = `/${language}/${i.routerLinkSuffix}`;
          newItems.push(i);
        });

        this.localItems = newItems;
      });
  }
}
