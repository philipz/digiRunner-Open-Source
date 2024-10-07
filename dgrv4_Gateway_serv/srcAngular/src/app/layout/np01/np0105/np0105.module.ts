import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrimengModule } from '../../../shared/primeng.module';
import { SharedModule } from '../../../shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { TokenExpiredGuard } from 'src/app/shared';

import { Np0105RoutingModule } from './np0105-routing.module';
import { Np0105Component } from './np0105.component';
import { ThemeCategoryComponent } from './theme-category/theme-category.component';

@NgModule({
    imports: [
        CommonModule,
        Np0105RoutingModule,
        PrimengModule,
        SharedModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [Np0105Component, ThemeCategoryComponent],
    entryComponents: [ThemeCategoryComponent],
    providers: [TokenExpiredGuard]
})
export class Np0105Module { }
