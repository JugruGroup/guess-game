import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { LocaleService } from '../../shared/services/locale.service';

@Component({
    selector: 'app-information-menubar',
    templateUrl: './information-menubar.component.html',
    standalone: false
})
export class InformationMenubarComponent implements OnInit {
  private readonly EVENT_TYPES_TITLE_KEY = 'eventTypes.title';
  private readonly EVENTS_TITLE_KEY = 'events.title';
  private readonly TALKS_TITLE_KEY = 'talks.title';
  private readonly SPEAKERS_TITLE_KEY = 'speakers.title';
  private readonly COMPANIES_TITLE_KEY = 'companies.title';
  private readonly STATISTICS_TITLE_KEY = 'statistics.title';

  private readonly KEYS = [this.EVENT_TYPES_TITLE_KEY, this.EVENTS_TITLE_KEY, this.TALKS_TITLE_KEY,
    this.SPEAKERS_TITLE_KEY, this.COMPANIES_TITLE_KEY, this.STATISTICS_TITLE_KEY];

  @Output() reload: EventEmitter<any> = new EventEmitter<any>();

  public items: MenuItem[] = [];
  public language: string;

  constructor(public translateService: TranslateService, private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.initMenuItems();

    this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.initMenuItems();
        this.reload.emit(); // TODO: delete
      });
  }

  initMenuItems() {
    this.translateService.get(this.KEYS)
      .subscribe(labels => {
        this.items = [
          {label: labels[this.EVENT_TYPES_TITLE_KEY], routerLink: `/${this.language}/information/event-types`},
          {label: labels[this.EVENTS_TITLE_KEY], routerLink: `/${this.language}/information/events`},
          {label: labels[this.TALKS_TITLE_KEY], routerLink: `/${this.language}/information/talks`},
          {label: labels[this.SPEAKERS_TITLE_KEY], routerLink: `/${this.language}/information/speakers`},
          {label: labels[this.COMPANIES_TITLE_KEY], routerLink: `/${this.language}/information/companies`},
          {label: labels[this.STATISTICS_TITLE_KEY], routerLink: `/${this.language}/information/statistics`}
        ];
      });
  }
}
