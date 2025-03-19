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

import { AnswerService } from './shared/services/answer.service';
import { AppComponent } from './app.component';
import { CancelGameComponent } from './modules/game/guess/cancel-game.component';
import { CompanyComponent } from './modules/information/companies/company.component';
import { CompaniesListComponent } from './modules/information/companies/companies-list.component';
import { CompaniesComponent } from './modules/information/companies/companies.component';
import { CompaniesModule } from './modules/information/companies/companies.module';
import { CompaniesSearchComponent } from './modules/information/companies/companies-search.component';
import { CompanyStatisticsComponent } from './modules/information/statistics/company/company-statistics.component';
import { EventComponent } from './modules/information/events/event.component';
import { EventsComponent } from './modules/information/events/events.component';
import { EventsModule } from './modules/information/events/events.module';
import { EventsSearchComponent } from './modules/information/events/events-search.component';
import { EventStatisticsComponent } from './modules/information/statistics/event/event-statistics.component';
import { EventTypeComponent } from './modules/information/event-types/event-type.component';
import { EventTypesComponent } from './modules/information/event-types/event-types.component';
import { EventTypesModule } from './modules/information/event-types/event-types.module';
import { EventTypeService } from './shared/services/event-type.service';
import { EventTypesSearchComponent } from './modules/information/event-types/event-types-search.component';
import { EventTypeStatisticsComponent } from './modules/information/statistics/event-type/event-type-statistics.component';
import { EventService } from './shared/services/event.service';
import { GameComponent } from './modules/game/game.component';
import { gameStateCanActivate } from './shared/guards/game-state.guard';
import { GuessComponent } from './modules/game/guess/guess.component';
import { GuessAccountBySpeakerComponent } from './modules/game/guess/guess-account-by-speaker.component';
import { GuessCompanyBySpeakerComponent } from './modules/game/guess/guess-company-by-speaker.component';
import { GuessModule } from './modules/game/guess/guess.module';
import { GuessNameByPhotoComponent } from './modules/game/guess/guess-name-by-photo.component';
import { GuessPhotoByNameComponent } from './modules/game/guess/guess-photo-by-name.component';
import { GuessSpeakerByAccountComponent } from './modules/game/guess/guess-speaker-by-account.component';
import { GuessSpeakerByCompanyComponent } from './modules/game/guess/guess-speaker-by-company.component';
import { GuessSpeakerByTagCloudComponent } from './modules/game/guess/guess-speaker-by-tag-cloud.component';
import { GuessSpeakerByTalkComponent } from './modules/game/guess/guess-speaker-by-talk.component';
import { GuessTagCloudBySpeakerComponent } from './modules/game/guess/guess-tag-cloud-by-speaker.component';
import { GuessTalkBySpeakerComponent } from './modules/game/guess/guess-talk-by-speaker.component';
import { HomeComponent } from './modules/home/home.component';
import { HomeModule } from './modules/home/home.module';
import { InformationComponent } from './modules/information/information.component';
import { InformationModule } from './modules/information/information.module';
import { LanguageComponent } from './modules/general/language.component';
import { MessageModule } from './modules/message/message.module';
import { OlapStatisticsComponent } from './modules/information/statistics/olap/olap-statistics.component';
import { QuestionService } from './shared/services/question.service';
import { ResultComponent } from './modules/game/result/result.component';
import { ResultModule } from './modules/game/result/result.module';
import { SpeakerComponent } from './modules/information/speakers/speaker.component';
import { SpeakerService } from './shared/services/speaker.service';
import { SpeakersListComponent } from './modules/information/speakers/speakers-list.component';
import { SpeakersComponent } from './modules/information/speakers/speakers.component';
import { SpeakersModule } from './modules/information/speakers/speakers.module';
import { SpeakersSearchComponent } from './modules/information/speakers/speakers-search.component';
import { SpeakerStatisticsComponent } from './modules/information/statistics/speaker/speaker-statistics.component';
import { StartComponent } from './modules/game/start/start.component';
import { StartModule } from './modules/game/start/start.module';
import { StateService } from './shared/services/state.service';
import { StatisticsService } from './shared/services/statistics.service';
import { StatisticsComponent } from './modules/information/statistics/statistics.component';
import { StatisticsModule } from './modules/information/statistics/statistics.module';
import { TalkComponent } from './modules/information/talks/talk.component';
import { TalksComponent } from './modules/information/talks/talks.component';
import { TalksModule } from './modules/information/talks/talks.module';
import { TalkService } from './shared/services/talk.service';
import { TalksSearchComponent } from './modules/information/talks/talks-search.component';
import { TopicService } from './shared/services/topic.service';

const DEFAULT_LOCATION = '/en/home';
const routes: Routes = [
  {
    path: ':language',
    component: LanguageComponent,
    children: [
      {path: '', redirectTo: 'home', pathMatch: 'full'},
      {path: 'home', component: HomeComponent},
      {
        path: 'game',
        component: GameComponent,
        children: [
          {path: '', redirectTo: 'start', pathMatch: 'full'},
          {path: 'start', component: StartComponent, canActivate: [gameStateCanActivate]},
          {
            path: 'guess',
            component: GuessComponent,
            children: [
              {path: '', redirectTo: 'name-by-photo', pathMatch: 'full'},
              {path: 'name-by-photo', component: GuessNameByPhotoComponent, canActivate: [gameStateCanActivate]},
              {path: 'photo-by-name', component: GuessPhotoByNameComponent, canActivate: [gameStateCanActivate]},
              {path: 'talk-by-speaker', component: GuessTalkBySpeakerComponent, canActivate: [gameStateCanActivate]},
              {path: 'speaker-by-talk', component: GuessSpeakerByTalkComponent, canActivate: [gameStateCanActivate]},
              {path: 'company-by-speaker', component: GuessCompanyBySpeakerComponent, canActivate: [gameStateCanActivate]},
              {path: 'speaker-by-company', component: GuessSpeakerByCompanyComponent, canActivate: [gameStateCanActivate]},
              {path: 'account-by-speaker', component: GuessAccountBySpeakerComponent, canActivate: [gameStateCanActivate]},
              {path: 'speaker-by-account', component: GuessSpeakerByAccountComponent, canActivate: [gameStateCanActivate]},
              {path: 'tag-cloud-by-speaker', component: GuessTagCloudBySpeakerComponent, canActivate: [gameStateCanActivate]},
              {path: 'speaker-by-tag-cloud', component: GuessSpeakerByTagCloudComponent, canActivate: [gameStateCanActivate]},
            ]
          },
          {path: 'result', component: ResultComponent, canActivate: [gameStateCanActivate]},
          {path: 'cancel', component: CancelGameComponent},
        ]
      },
      {
        path: 'information',
        component: InformationComponent,
        children: [
          {path: '', redirectTo: 'event-types/search', pathMatch: 'full'},
          {
            path: 'event-types',
            component: EventTypesComponent,
            children: [
              {path: '', redirectTo: 'search', pathMatch: 'full'},
              {path: 'search', component: EventTypesSearchComponent},
              {path: 'event-type/:id', component: EventTypeComponent},
            ]
          },
          {
            path: 'events',
            component: EventsComponent,
            children: [
              {path: '', redirectTo: 'search', pathMatch: 'full'},
              {path: 'search', component: EventsSearchComponent},
              {path: 'event/:id', component: EventComponent},
            ]
          },
          {
            path: 'talks',
            component: TalksComponent,
            children: [
              {path: '', redirectTo: 'search', pathMatch: 'full'},
              {path: 'search', component: TalksSearchComponent},
              {path: 'talk/:id', component: TalkComponent},
            ]
          },
          {
            path: 'speakers',
            component: SpeakersComponent,
            children: [
              {path: '', redirectTo: 'list', pathMatch: 'full'},
              {path: 'list', component: SpeakersListComponent},
              {path: 'search', component: SpeakersSearchComponent},
              {path: 'speaker/:id', component: SpeakerComponent},
            ]
          },
          {
            path: 'companies',
            component: CompaniesComponent,
            children: [
              {path: '', redirectTo: 'list', pathMatch: 'full'},
              {path: 'list', component: CompaniesListComponent},
              {path: 'search', component: CompaniesSearchComponent},
              {path: 'company/:id', component: CompanyComponent},
            ]
          },
          {
            path: 'statistics',
            component: StatisticsComponent,
            children: [
              {path: '', redirectTo: 'event-types', pathMatch: 'full'},
              {path: 'event-types', component: EventTypeStatisticsComponent},
              {path: 'events', component: EventStatisticsComponent},
              {path: 'speakers', component: SpeakerStatisticsComponent},
              {path: 'companies', component: CompanyStatisticsComponent},
              {path: 'olap', component: OlapStatisticsComponent},
            ]
          }
        ]
      }
    ]
  },
  {path: '**', redirectTo: DEFAULT_LOCATION}
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
    TalksModule
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
