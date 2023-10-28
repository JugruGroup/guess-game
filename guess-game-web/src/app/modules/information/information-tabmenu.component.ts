import { AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-information-tabmenu',
  templateUrl: './information-tabmenu.component.html'
})
export class InformationTabMenuComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() public items: MenuItem[] = [];
  @Input() public scrollableWidth: number;

  public scrollable = false;
  public localItems: MenuItem[] = [];

  @ViewChild('tabMenuDiv') tabMenuDiv: ElementRef<HTMLDivElement>;

  constructor(public translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.initMenuItems();

    this.translateService.onLangChange
      .subscribe(() => this.initMenuItems());
  }

  initMenuItems() {
    const keys = this.items.map(i => i.labelKey);

    this.translateService.get(keys)
      .subscribe(labels => {
        const newItems: MenuItem[] = [];

        this.items.forEach(i => {
          i.label = labels[i.labelKey];
          newItems.push(i);
        });

        this.localItems = newItems;
      });
  }

  ngAfterViewInit(): void {
    this.onResize();
    window.addEventListener('resize', this.onResize);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);
  }

  onResize = (): void => {
    if (!isNaN(this.scrollableWidth) && this.tabMenuDiv) {
      this.scrollable = (this.tabMenuDiv.nativeElement.clientWidth < this.scrollableWidth);
    }
  }
}
