import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MarkdownModule } from 'ngx-markdown';
import { TranslateModule } from '@ngx-translate/core';
import { CheckboxModule } from 'primeng/checkbox';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { GeneralModule } from '../../general/general.module';
import { InformationModule } from '../information.module';
import { MessageModule } from '../../message/message.module';
import { EventComponent } from './event.component';
import { EventsSearchComponent } from './events-search.component';
import { EventsTabMenuComponent } from './events-tabmenu.component';

@NgModule({
  declarations: [
    EventComponent,
    EventsTabMenuComponent,
    EventsSearchComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    CheckboxModule,
    MarkdownModule,
    RouterModule,
    TableModule,
    GeneralModule,
    InformationModule,
    MessageModule,
    Select,
    TranslateModule
  ]
})
export class EventsModule {
}
