import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";

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
    this.items = [
      {labelKey: 'speakers.list.title', routerLink: '/information/speakers/list'},
      {labelKey: 'speakers.search.title', routerLink: '/information/speakers/search'}
    ];

    if (!isNaN(this.id)) {
      this.items.push({labelKey: 'speaker.title', routerLink: `/information/speakers/speaker/${this.id}`});
    }
  }
}
