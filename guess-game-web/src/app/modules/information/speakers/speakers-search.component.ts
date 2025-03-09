import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { CompanyService } from '../../../shared/services/company.service';
import { LocaleService } from '../../../shared/services/locale.service';
import { Speaker } from '../../../shared/models/speaker/speaker.model';
import { SpeakerService } from '../../../shared/services/speaker.service';
import { getSpeakersWithCompaniesString, isStringEmpty } from '../../general/utility-functions';

@Component({
    selector: 'app-speakers-search',
    templateUrl: './speakers-search.component.html',
    standalone: false
})
export class SpeakersSearchComponent implements OnInit {
  private imageDirectory = 'assets/images';
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public twitterUrlPrefix = 'https://twitter.com';
  public gitHubUrlPrefix = 'https://github.com';
  public habrUrlPrefix = 'https://habr.com/users';

  public name: string;
  public company: string;
  public twitter: string;
  public gitHub: string;
  public habr: string;
  public description: string;
  public isJavaChampion = false;
  public isMvp = false;

  public speakers: Speaker[] = [];

  private searched = false;
  public multiSortMeta: any[] = [];

  public companySuggestions: string[];

  public language: string;

  constructor(private speakerService: SpeakerService, public translateService: TranslateService,
              private companyService: CompanyService, private localeService: LocaleService) {
    this.multiSortMeta.push({field: 'displayName', order: 1});
    this.multiSortMeta.push({field: 'companiesString', order: 1});
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.translateService.onLangChange
      .subscribe(() => {
        this.language = this.localeService.getLanguage();
        this.onLanguageChange();
      });
  }

  loadSpeakers(name: string, company: string, twitter: string, gitHub: string, habr: string, description: string,
               isJavaChampion: boolean, isMvp: boolean) {
    this.speakerService.getSpeakers(name, company, twitter, gitHub, habr, description, isJavaChampion, isMvp, this.language)
      .subscribe(data => {
        this.speakers = getSpeakersWithCompaniesString(data);
        this.searched = true;
      });
  }

  onLanguageChange() {
    this.search();
  }

  onFilterChange() {
    this.searched = false;
  }

  search() {
    if (!this.isSearchDisabled()) {
      this.loadSpeakers(this.name, this.company, this.twitter, this.gitHub, this.habr, this.description, this.isJavaChampion, this.isMvp);
    }
  }

  clear() {
    this.name = undefined;
    this.company = undefined;
    this.twitter = undefined;
    this.gitHub = undefined;
    this.habr = undefined;
    this.description = undefined;
    this.isJavaChampion = false;
    this.isMvp = false;
    this.speakers = [];

    this.searched = false;
  }

  isSearchDisabled(): boolean {
    return (isStringEmpty(this.name) &&
      isStringEmpty(this.company) &&
      isStringEmpty(this.twitter) &&
      isStringEmpty(this.gitHub) &&
      isStringEmpty(this.habr) &&
      isStringEmpty(this.description) &&
      (!this.isJavaChampion) &&
      (!this.isMvp));
  }

  isNoSpeakersFoundVisible() {
    return (this.searched && (this.speakers.length === 0));
  }

  isSpeakersListVisible() {
    return (this.searched && (this.speakers.length > 0));
  }

  companySearch(event) {
    this.companyService.getCompanyNamesByFirstLetters(event.query, this.language)
      .subscribe(data => {
          this.companySuggestions = data;
        }
      );
  }
}
