import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { ChartModule } from 'primeng/chart';
import { CheckboxModule } from 'primeng/checkbox';
import { DynamicDialogModule } from 'primeng/dynamicdialog';
import { MultiSelectModule } from 'primeng/multiselect';
import { RippleModule } from 'primeng/ripple';
import { RouterModule } from '@angular/router';
import { Select } from 'primeng/select';
import { SelectButton } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';

// import { AtftModule } from 'atft';
import { AtftModule } from '../../atft/atft.module';

import { ChartZoomInComponent } from './olap/chart/chart-zoom-in.component';
import { GeneralModule } from '../../general/general.module';
import { InformationModule } from '../information.module';
import { MessageModule } from '../../message/message.module';
import { EventPlaceStatisticsComponent } from './event-place/event-place-statistics.component';
import { EventTypeStatisticsComponent } from './event-type/event-type-statistics.component';
import { EventStatisticsComponent } from './event/event-statistics.component';
import { SpeakerStatisticsComponent } from './speaker/speaker-statistics.component';
import { CompanyStatisticsComponent } from './company/company-statistics.component';
import { OlapStatisticsComponent } from './olap/olap-statistics.component';
import { StatisticsTabMenuComponent } from './statistics-tabmenu.component';
import { ThreeDimensionsCubeComponent } from './olap/three-dimensions/three-dimensions-cube.component';
import { ThreeDimensionsGridComponent } from './olap/three-dimensions/three-dimensions-grid.component';
import {
  ThreeDimensionsBatchedSphereMeshComponent
} from './olap/three-dimensions/three-dimensions-batched-sphere-mesh.component';
import {
  ThreeDimensionsBatchedTextMeshComponent
} from './olap/three-dimensions/three-dimensions-batched-text-mesh.component';
import { ThreeDimensionsZoomInComponent } from './olap/three-dimensions/three-dimensions-zoom-in.component';

@NgModule({
  declarations: [
    ChartZoomInComponent,
    EventPlaceStatisticsComponent,
    EventTypeStatisticsComponent,
    EventStatisticsComponent,
    SpeakerStatisticsComponent,
    CompanyStatisticsComponent,
    OlapStatisticsComponent,
    StatisticsTabMenuComponent,
    ThreeDimensionsCubeComponent,
    ThreeDimensionsGridComponent,
    ThreeDimensionsBatchedSphereMeshComponent,
    ThreeDimensionsBatchedTextMeshComponent,
    ThreeDimensionsZoomInComponent
  ],
  imports: [
    AtftModule,
    AutoCompleteModule,
    ButtonModule,
    ChartModule,
    CommonModule,
    FormsModule,
    CheckboxModule,
    DynamicDialogModule,
    MultiSelectModule,
    RippleModule,
    RouterModule,
    Select,
    TableModule,
    TooltipModule,
    GeneralModule,
    InformationModule,
    MessageModule,
    TranslateModule,
    SelectButton
  ]
})
export class StatisticsModule {
}
