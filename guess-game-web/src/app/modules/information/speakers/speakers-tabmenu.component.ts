import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-speakers-tabmenu',
  templateUrl: './speakers-tabmenu.component.html',
  standalone: false
})
export class SpeakersTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;
  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    const items = [
      {labelKey: 'speakers.list.title', routerLinkSuffix: 'information/speakers/list'},
      {labelKey: 'speakers.search.title', routerLinkSuffix: 'information/speakers/search'}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'speaker.title', routerLinkSuffix: `information/speakers/speaker/${this.id}`});
    }

    this.items = items;
  }
}
