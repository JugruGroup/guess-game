import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";

@Component({
  selector: 'app-events-tabmenu',
  templateUrl: './events-tabmenu.component.html'
})
export class EventsTabMenuComponent implements OnInit {
  public readonly SCROLLABLE_WIDTH = 150;

  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    this.items = [
      {labelKey: 'events.search.title', routerLink: '/information/events/search'}
    ];

    if (!isNaN(this.id)) {
      this.items.push({labelKey: 'event.title', routerLink: `/information/events/event/${this.id}`});
    }
  }
}
