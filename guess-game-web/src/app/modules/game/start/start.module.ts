import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ListboxModule } from 'primeng/listbox';
import { RadioButtonModule } from 'primeng/radiobutton';
import { Select } from 'primeng/select';
import { StartComponent } from './start.component';
import { GeneralModule } from '../../general/general.module';
import { MessageModule } from '../../message/message.module';

@NgModule({
  declarations: [
    StartComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ListboxModule,
    RadioButtonModule,
    RouterModule,
    Select,
    TranslateModule,
    GeneralModule,
    MessageModule
  ]
})
export class StartModule {
}
