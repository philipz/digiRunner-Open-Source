import { Injectable } from "@angular/core";
import Swal from 'sweetalert2'
import { AlertType } from "src/app/models/common.enum";
import { Observable, from, zip } from "rxjs";
import { Router } from "@angular/router";


@Injectable()
export class AlertService {

    // opened : boolean;

    constructor(
        private router: Router
    ) {
        // this.opened = false;
    }
    ok(title: string, text: string, type: AlertType = AlertType.warning, html?: string) {
        // if (!this.opened){
        Swal.fire({

            title: title,
            text: text,
            icon: type,
            html: html,
            // target: document.getElementById('form-modal')??'',
            // showCancelButton: true,
            // confirmButtonText: 'Yes, delete it!',
            // cancelButtonText: 'No, keep it'
        }).then((result) => {
            // this.opened = false;
        })
        // this.opened = true;
        // }

    }

    confirm(title: string, text: string, confirmObservable?: Observable<any>, cancelObservable?: Observable<any>) {

        // if (!this.opened){
        Swal.fire({
            title: title,
            text: text,
            icon: AlertType.question,
            showConfirmButton: true,
            showCancelButton: true,

        }).then((result) => {
            // this.opened = false;
            if (result.value) {
                if (confirmObservable) {
                    confirmObservable.subscribe();
                }
            } else {
                if (cancelObservable) cancelObservable.subscribe();
            }
        })
        // this.opened = true;
        // }
    }

    error(title: string, text: string, type: AlertType = AlertType.error) {
        Swal.fire({
            title: title,
            text: text,
            icon: type
        }).then((result) => {

        });
    }

    logout(title: string, text: string, type: AlertType = AlertType.success) {
      // console.log(document.getElementById('form-modal'))
        Swal.fire({
            title: title,
            text: text,
            icon: type,

        }).then((result) => {
            this.router.navigateByUrl('/login');
        });
    }
}
