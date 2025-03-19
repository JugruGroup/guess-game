import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-events-tabmenu',
  templateUrl: './events-tabmenu.component.html',
  standalone: false
})
export class EventsTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;
  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    const items = [
      {labelKey: 'events.search.title', routerLinkSuffix: 'information/events/search'}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'event.title', routerLinkSuffix: `information/events/event/${this.id}`});
    }

    this.items = items;
  }
}
