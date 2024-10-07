import { Component, HostListener, OnInit } from '@angular/core';
import { MatDrawerToggleResult, MatSidenav } from '@angular/material/sidenav';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {

  // collapedSideBar: boolean = false;


  appropriateClass:string = '';
  mode:string = 'side'; //side,over
  isMin:boolean = false;

  constructor() { }

  @HostListener('window:resize', ['$event'])
  getScreen(){
    //console.log(window.innerHeight);
    if(window.innerHeight<=412){
      this.appropriateClass = 'bottomRelative';
    }else{
      this.appropriateClass = 'bottomStick';
    }

    this.mode = window.innerWidth <= 992 ? 'over' : 'side';
    // this.minContent = window.innerWidth < 576;
  }

  ngOnInit(): void {
    // console.log('layout init')
  }


  // receiveCollapsed($event) {
  //   this.collapedSideBar = $event;
  // }

  toggleSideNav(drawer: MatSidenav) {
    drawer.toggle().then(result => {
      // console.log('選單狀態：' + result);
      this.isMin = !result;
    });
  }

}
