import { Component, Input, OnInit } from '@angular/core';
import { MenuItem } from "primeng/api";

@Component({
  selector: 'app-companies-tabmenu',
  templateUrl: './companies-tabmenu.component.html'
})
export class CompaniesTabMenuComponent implements OnInit {
  public readonly SCROLLABLE_WIDTH = 180;

  @Input() private id: number;

  public items: MenuItem[] = [];

  ngOnInit(): void {
    this.items = [
      {labelKey: 'companies.list.title', routerLink: '/information/companies/list'},
      {labelKey: 'companies.search.title', routerLink: '/information/companies/search'}
    ];

    if (!isNaN(this.id)) {
      this.items.push({labelKey: 'company.title', routerLink: `/information/companies/company/${this.id}`});
    }
  }
}
