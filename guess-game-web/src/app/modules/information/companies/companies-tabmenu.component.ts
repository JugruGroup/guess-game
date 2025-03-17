import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-companies-tabmenu',
  templateUrl: './companies-tabmenu.component.html',
  standalone: false
})
export class CompaniesTabMenuComponent implements OnInit {
  @Input() public activeIndex: number;
  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    const items = [
      {labelKey: 'companies.list.title', routerLinkSuffix: 'information/companies/list'},
      {labelKey: 'companies.search.title', routerLinkSuffix: 'information/companies/search'}
    ];

    if (!isNaN(this.id)) {
      items.push({labelKey: 'company.title', routerLinkSuffix: `information/companies/company/${this.id}`});
    }

    this.items = items;
  }
}
