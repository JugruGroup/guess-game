import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CheckboxModule } from 'primeng/checkbox';
import { RouterModule } from '@angular/router';
import { MarkdownModule } from 'ngx-markdown';
import { TranslateModule } from '@ngx-translate/core';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { GeneralModule } from '../../general/general.module';
import { InformationModule } from '../information.module';
import { MessageModule } from '../../message/message.module';
import { EventTypeComponent } from './event-type.component';
import { EventTypesSearchComponent } from './event-types-search.component';
import { EventTypesTabMenuComponent } from './event-types-tabmenu.component';

@NgModule({
  declarations: [
    EventTypeComponent,
    EventTypesSearchComponent,
    EventTypesTabMenuComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    CheckboxModule,
    MarkdownModule,
    Select,
    TableModule,
    RouterModule,
    GeneralModule,
    InformationModule,
    MessageModule,
    TranslateModule
  ]
})
export class EventTypesModule {
}
