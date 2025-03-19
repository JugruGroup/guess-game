import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-event-types-tabmenu',
  templateUrl: './event-types-tabmenu.component.html',
  standalone: false
})
export class EventTypesTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;
  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    const items = [
      {labelKey: 'eventTypes.search.title', routerLinkSuffix: 'information/event-types/search'}
    ];

    if (!isNaN(this.id)) {
      items.push({
        labelKey: 'eventType.title',
        routerLinkSuffix: `information/event-types/event-type/${this.id}`
      });
    }

    this.items = items;
  }
}
