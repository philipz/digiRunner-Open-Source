import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0113RoutingModule } from './np0113-routing.module';
import { Np0113Component } from './np0113.component';

@NgModule({
    imports: [
        CommonModule,
        Np0113RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0113Component],
    providers: [TokenExpiredGuard]
})
export class Np0113Module { }
