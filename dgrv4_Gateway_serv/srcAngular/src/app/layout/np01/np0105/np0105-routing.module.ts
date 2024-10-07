import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TokenExpiredGuard } from 'src/app/shared';
import { Np0105Component } from './np0105.component';
const routes: Routes = [
    {
        path: '', component: Np0105Component, canActivate: [TokenExpiredGuard]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class Np0105RoutingModule { }
