import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { ChartModule } from 'primeng/chart';
import { CheckboxModule } from 'primeng/checkbox';
import { DropdownModule } from 'primeng/dropdown';
import { DynamicDialogModule } from 'primeng/dynamicdialog';
import { MultiSelectModule } from 'primeng/multiselect';
import { RippleModule } from 'primeng/ripple';
import { RouterModule } from '@angular/router';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { ChartZoomInComponent } from './olap/chart/chart-zoom-in.component';
import { GeneralModule } from '../../general/general.module';
import { InformationModule } from '../information.module';
import { MessageModule } from '../../message/message.module';
import { EventTypeStatisticsComponent } from './event-type-statistics.component';
import { EventStatisticsComponent } from './event-statistics.component';
import { SpeakerStatisticsComponent } from './speaker-statistics.component';
import { CompanyStatisticsComponent } from './company-statistics.component';
import { OlapStatisticsComponent } from './olap-statistics.component';
import { StatisticsTabMenuComponent } from './statistics-tabmenu.component';

@NgModule({
  declarations: [
    ChartZoomInComponent,
    EventTypeStatisticsComponent,
    EventStatisticsComponent,
    SpeakerStatisticsComponent,
    CompanyStatisticsComponent,
    OlapStatisticsComponent,
    StatisticsTabMenuComponent
  ],
  imports: [
    AutoCompleteModule,
    ButtonModule,
    ChartModule,
    CommonModule,
    FormsModule,
    CheckboxModule,
    DropdownModule,
    DynamicDialogModule,
    MultiSelectModule,
    RippleModule,
    RouterModule,
    TableModule,
    TooltipModule,
    GeneralModule,
    InformationModule,
    MessageModule,
    TranslateModule,
    SelectButtonModule
  ]
})
export class StatisticsModule {
}
