import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-information-tabmenu',
    templateUrl: './information-tabmenu.component.html',
    standalone: false
})
export class InformationTabMenuComponent implements OnInit {
  @Input() public items: MenuItem[] = [];
  @Input() public activeIndex: number;

  public localItems: MenuItem[] = [];

  constructor(public translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.initMenuItems();

    this.translateService.onLangChange
      .subscribe(() => this.initMenuItems());
  }

  initMenuItems() {
    const keys = this.items.map(i => i.labelKey);

    this.translateService.get(keys)
      .subscribe(labels => {
        const newItems: MenuItem[] = [];

        this.items.forEach(i => {
          i.label = labels[i.labelKey];
          newItems.push(i);
        });

        this.localItems = newItems;
      });
  }
}
