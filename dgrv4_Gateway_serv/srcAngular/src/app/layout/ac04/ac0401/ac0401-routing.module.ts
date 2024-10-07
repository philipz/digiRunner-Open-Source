import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Ac0401Component } from './ac0401.component';
import { TokenExpiredGuard } from 'src/app/shared';

const routes: Routes = [
  {
      path: '', component: Ac0401Component   ,canActivate :[TokenExpiredGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Ac0401RoutingModule { }
