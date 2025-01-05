import { Component, Input } from '@angular/core';
import { MenuItem } from "primeng/api";

@Component({
  selector: 'app-statistics-tabmenu',
  templateUrl: './statistics-tabmenu.component.html'
})
export class StatisticsTabMenuComponent {
  @Input() public activeIndex: number;

  public items: MenuItem[] = [
    {labelKey: 'statistics.eventTypes.title', routerLink: '/information/statistics/event-types'},
    {labelKey: 'statistics.events.title', routerLink: '/information/statistics/events'},
    {labelKey: 'statistics.speakers.title', routerLink: '/information/statistics/speakers'},
    {labelKey: 'statistics.companies.title', routerLink: '/information/statistics/companies'},
    {labelKey: 'statistics.olap.title', routerLink: '/information/statistics/olap'}
  ];
}
