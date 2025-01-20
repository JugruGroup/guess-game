import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import {
  HttpBackend,
  HttpClient,
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeRu from '@angular/common/locales/ru';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MarkdownModule } from 'ngx-markdown';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { definePreset } from '@primeng/themes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

import { AppComponent } from './app.component';
import { CompaniesModule } from './modules/information/companies/companies.module';
import { EventTypesModule } from './modules/information/event-types/event-types.module';
import { EventsModule } from './modules/information/events/events.module';
import { HomeModule } from './modules/home/home.module';
import { InformationModule } from './modules/information/information.module';
import { GuessModule } from './modules/game/guess/guess.module';
import { MessageModule } from './modules/message/message.module';
import { ResultModule } from './modules/game/result/result.module';
import { StartModule } from './modules/game/start/start.module';
import { SpeakersModule } from './modules/information/speakers/speakers.module';
import { StatisticsModule } from './modules/information/statistics/statistics.module';
import { TalksModule } from './modules/information/talks/talks.module';
import { UnknownModule } from './modules/unknown/unknown.module';
import { HomeComponent } from './modules/home/home.component';
import { StartComponent } from './modules/game/start/start.component';
import { ResultComponent } from './modules/game/result/result.component';
import { GuessNameByPhotoComponent } from './modules/game/guess/guess-name-by-photo.component';
import { GuessPhotoByNameComponent } from './modules/game/guess/guess-photo-by-name.component';
import { GuessTalkBySpeakerComponent } from './modules/game/guess/guess-talk-by-speaker.component';
import { GuessSpeakerByTalkComponent } from './modules/game/guess/guess-speaker-by-talk.component';
import { GuessCompanyBySpeakerComponent } from './modules/game/guess/guess-company-by-speaker.component';
import { GuessSpeakerByCompanyComponent } from './modules/game/guess/guess-speaker-by-company.component';
import { GuessAccountBySpeakerComponent } from './modules/game/guess/guess-account-by-speaker.component';
import { GuessSpeakerByAccountComponent } from './modules/game/guess/guess-speaker-by-account.component';
import { GuessTagCloudBySpeakerComponent } from './modules/game/guess/guess-tag-cloud-by-speaker.component';
import { GuessSpeakerByTagCloudComponent } from './modules/game/guess/guess-speaker-by-tag-cloud.component';
import { CancelGameComponent } from './modules/game/guess/cancel-game.component';
import { NotFoundComponent } from './modules/unknown/not-found.component';
import { EventTypeComponent } from './modules/information/event-types/event-type.component';
import { EventTypesSearchComponent } from './modules/information/event-types/event-types-search.component';
import { EventComponent } from './modules/information/events/event.component';
import { EventsSearchComponent } from './modules/information/events/events-search.component';
import { TalkComponent } from './modules/information/talks/talk.component';
import { TalksSearchComponent } from './modules/information/talks/talks-search.component';
import { SpeakerComponent } from './modules/information/speakers/speaker.component';
import { SpeakersListComponent } from './modules/information/speakers/speakers-list.component';
import { SpeakersSearchComponent } from './modules/information/speakers/speakers-search.component';
import { CompanyComponent } from './modules/information/companies/company.component';
import { CompaniesListComponent } from './modules/information/companies/companies-list.component';
import { CompaniesSearchComponent } from './modules/information/companies/companies-search.component';
import { EventTypeStatisticsComponent } from './modules/information/statistics/event-type/event-type-statistics.component';
import { EventStatisticsComponent } from './modules/information/statistics/event/event-statistics.component';
import { SpeakerStatisticsComponent } from './modules/information/statistics/speaker/speaker-statistics.component';
import { CompanyStatisticsComponent } from './modules/information/statistics/company/company-statistics.component';
import { OlapStatisticsComponent } from './modules/information/statistics/olap/olap-statistics.component';
import { AnswerService } from './shared/services/answer.service';
import { EventService } from './shared/services/event.service';
import { EventTypeService } from './shared/services/event-type.service';
import { QuestionService } from './shared/services/question.service';
import { StateService } from './shared/services/state.service';
import { SpeakerService } from './shared/services/speaker.service';
import { StatisticsService } from './shared/services/statistics.service';
import { TalkService } from './shared/services/talk.service';
import { TopicService } from './shared/services/topic.service';
import { gameStateCanActivate } from './shared/guards/game-state.guard';

const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: 'game/start', component: StartComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/name-by-photo', component: GuessNameByPhotoComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/photo-by-name', component: GuessPhotoByNameComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/talk-by-speaker', component: GuessTalkBySpeakerComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/speaker-by-talk', component: GuessSpeakerByTalkComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/company-by-speaker', component: GuessCompanyBySpeakerComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/speaker-by-company', component: GuessSpeakerByCompanyComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/account-by-speaker', component: GuessAccountBySpeakerComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/speaker-by-account', component: GuessSpeakerByAccountComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/tag-cloud-by-speaker', component: GuessTagCloudBySpeakerComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/guess/speaker-by-tag-cloud', component: GuessSpeakerByTagCloudComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/result', component: ResultComponent, canActivate: [gameStateCanActivate]},
  {path: 'game/cancel', component: CancelGameComponent},
  {path: 'game', redirectTo: 'game/start'},
  {path: 'information/event-types/search', component: EventTypesSearchComponent},
  {path: 'information/event-types/event-type/:id', component: EventTypeComponent},
  {path: 'information/events/search', component: EventsSearchComponent},
  {path: 'information/events/event/:id', component: EventComponent},
  {path: 'information/talks/search', component: TalksSearchComponent},
  {path: 'information/talks/talk/:id', component: TalkComponent},
  {path: 'information/speakers/list', component: SpeakersListComponent},
  {path: 'information/speakers/search', component: SpeakersSearchComponent},
  {path: 'information/speakers/speaker/:id', component: SpeakerComponent},
  {path: 'information/companies/list', component: CompaniesListComponent},
  {path: 'information/companies/search', component: CompaniesSearchComponent},
  {path: 'information/companies/company/:id', component: CompanyComponent},
  {path: 'information/statistics/event-types', component: EventTypeStatisticsComponent},
  {path: 'information/statistics/events', component: EventStatisticsComponent},
  {path: 'information/statistics/speakers', component: SpeakerStatisticsComponent},
  {path: 'information/statistics/companies', component: CompanyStatisticsComponent},
  {path: 'information/statistics/olap', component: OlapStatisticsComponent},
  {path: 'information/event-types', redirectTo: 'information/event-types/search'},
  {path: 'information/events', redirectTo: 'information/events/search'},
  {path: 'information/talks', redirectTo: 'information/talks/search'},
  {path: 'information/speakers', redirectTo: 'information/speakers/list'},
  {path: 'information/companies', redirectTo: 'information/companies/list'},
  {path: 'information/statistics', redirectTo: 'information/statistics/event-types'},
  {path: 'information/event-type/:id', redirectTo: 'information/event-types/event-type/:id'},
  {path: 'information/event/:id', redirectTo: 'information/events/event/:id'},
  {path: 'information/talk/:id', redirectTo: 'information/talks/talk/:id'},
  {path: 'information/speaker/:id', redirectTo: 'information/speakers/speaker/:id'},
  {path: 'information/company/:id', redirectTo: 'information/companies/company/:id'},
  {path: 'information', redirectTo: 'information/event-types/search'},
  {path: '', pathMatch: 'full', redirectTo: 'home'},
  {path: '**', component: NotFoundComponent}
];

// AoT requires an exported function for factories
export function HttpLoaderFactory(httpBackend: HttpBackend) {
  return new MultiTranslateHttpLoader(httpBackend, ['/assets/i18n/primelocale/', '/assets/i18n/']);
}

registerLocaleData(localeRu, 'ru');

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    RouterModule.forRoot(routes, {}),
    BrowserModule,
    BrowserAnimationsModule,
    MarkdownModule.forRoot({
      loader: HttpClient
    }),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpBackend]
      }
    }),
    CompaniesModule,
    EventsModule,
    EventTypesModule,
    HomeModule,
    InformationModule,
    GuessModule,
    MessageModule,
    ResultModule,
    StartModule,
    SpeakersModule,
    StatisticsModule,
    TalksModule,
    UnknownModule
  ],
  providers: [
    AnswerService,
    EventService,
    EventTypeService,
    QuestionService,
    StateService,
    SpeakerService,
    StatisticsService,
    TalkService,
    TopicService,
    provideHttpClient(withInterceptorsFromDi()),
    providePrimeNG({
      theme: {
        preset: definePreset(Aura, {
          semantic: {
            primary: {
              50: "{blue.50}",
              100: "{blue.100}",
              200: "{blue.200}",
              300: "{blue.300}",
              400: "{blue.400}",
              500: "{blue.500}",
              600: "{blue.600}",
              700: "{blue.700}",
              800: "{blue.800}",
              900: "{blue.900}",
              950: "{blue.950}"
            }
          }
        }),
        options: {
          cssLayer: {
            name: 'primeng',
            order: 'defaults, primeng, primeicons, fontawesome, bootstrap-icons'
          }
        }
      },
    })
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
