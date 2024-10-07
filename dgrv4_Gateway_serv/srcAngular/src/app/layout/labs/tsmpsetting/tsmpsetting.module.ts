import { TokenExpiredGuard } from 'src/app/shared';
import { TsmpsettingRoutingModule } from './tsmpsetting-routing.module';
import { TsmpsettingComponent } from './tsmpsetting.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    TsmpsettingRoutingModule
  ],
  declarations: [TsmpsettingComponent],
  providers:[TokenExpiredGuard]
})
export class TsmpsettingModule { }
