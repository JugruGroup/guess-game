import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-statistics-tabmenu',
  templateUrl: './statistics-tabmenu.component.html',
  standalone: false
})
export class StatisticsTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    this.items = [
      {labelKey: 'statistics.eventTypes.title', routerLinkSuffix: 'information/statistics/event-types'},
      {labelKey: 'statistics.events.title', routerLinkSuffix: 'information/statistics/events'},
      {labelKey: 'statistics.speakers.title', routerLinkSuffix: 'information/statistics/speakers'},
      {labelKey: 'statistics.companies.title', routerLinkSuffix: 'information/statistics/companies'},
      {labelKey: 'statistics.olap.title', routerLinkSuffix: 'information/statistics/olap'}
    ];
  }
}
