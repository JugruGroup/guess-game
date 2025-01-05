import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";

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
    this.items = [
      {labelKey: 'talks.search.title', routerLink: '/information/talks/search'}
    ];

    if (!isNaN(this.id)) {
      this.items.push({labelKey: 'talk.title', routerLink: `/information/talks/talk/${this.id}`});
    }
  }
}
