import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";

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
    this.items = [
      {labelKey: 'eventTypes.search.title', routerLink: '/information/event-types/search'}
    ];

    if (!isNaN(this.id)) {
      this.items.push({labelKey: 'eventType.title', routerLink: `/information/event-types/event-type/${this.id}`});
    }
  }
}
