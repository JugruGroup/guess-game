import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { CompanyDetails } from '../../../shared/models/company/company-details.model';
import { CompanyService } from '../../../shared/services/company.service';
import { LocaleService } from '../../../shared/services/locale.service';
import { getSpeakersWithCompaniesString } from '../../general/utility-functions';

@Component({
    selector: 'app-company',
    templateUrl: './company.component.html',
    standalone: false
})
export class CompanyComponent implements OnInit, OnDestroy {
  private imageDirectory = 'assets/images';
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public twitterUrlPrefix = 'https://twitter.com';
  public gitHubUrlPrefix = 'https://github.com';
  public habrUrlPrefix = 'https://habr.com/users';

  public id: number;
  public companyDetails: CompanyDetails = new CompanyDetails();
  public multiSortMeta: any[] = [];

  public language: string;
  private languageSubscription: Subscription;

  constructor(public companyService: CompanyService, public translateService: TranslateService,
              private activatedRoute: ActivatedRoute, private localeService: LocaleService) {
    this.multiSortMeta.push({field: 'displayName', order: 1});
    this.multiSortMeta.push({field: 'companiesString', order: 1});
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      const idString: string = params['id'];
      const idNumber = Number(idString);

      if (!isNaN(idNumber)) {
        this.id = idNumber;
        this.loadCompany(this.id);

        this.languageSubscription = this.translateService.onLangChange
          .subscribe(() => {
            this.language = this.localeService.getLanguage();
            this.onLanguageChange();
          });
      }
    });
  }

  ngOnDestroy() {
    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  loadCompany(id: number) {
    this.companyService.getCompany(id, this.language)
      .subscribe(data => {
        this.companyDetails = this.getCompanyDetailsWithFilledAttributes(data);
      });
  }

  getCompanyDetailsWithFilledAttributes(companyDetails: CompanyDetails): CompanyDetails {
    if (companyDetails?.speakers) {
      companyDetails.speakers = getSpeakersWithCompaniesString(companyDetails.speakers);
    }

    return companyDetails;
  }

  onLanguageChange() {
    this.loadCompany(this.id);
  }

  isCompanyLinksVisible() {
    return this.companyDetails.company?.siteLink;
  }

  isSpeakersListVisible() {
    return ((this.companyDetails.speakers) && (this.companyDetails.speakers.length > 0));
  }
}
