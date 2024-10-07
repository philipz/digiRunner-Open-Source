import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Ac0401RoutingModule } from './ac0401-routing.module';
import { Ac0401Component } from './ac0401.component';
import { PrimengModule } from 'src/app/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

@NgModule({
  imports: [
    CommonModule,
    Ac0401RoutingModule,
    PrimengModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [Ac0401Component],
  providers:[TokenExpiredGuard]
})
export class Ac0401Module { }
