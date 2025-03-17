import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-talks-tabmenu',
  templateUrl: './talks-tabmenu.component.html',
  standalone: false
})
export class TalksTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;
  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    const items = [
      {labelKey: 'talks.search.title', routerLinkSuffix: 'information/talks/search'}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'talk.title', routerLinkSuffix: `information/talks/talk/${this.id}`});
    }

    this.items = items;
  }
}
