import { Observable } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ModuleService } from 'src/app/shared/services/api-module.service';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { concatMap } from 'rxjs/operators';
import { Location } from '@angular/common';
import * as dayjs from 'dayjs';
import { generate } from 'generate-password';
import { TranslateService } from '@ngx-translate/core';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';

@Component({
    selector: 'app-ac0401',
    templateUrl: './ac0401.component.html',
    styleUrls: ['./ac0401.component.css'],
    providers: [ModuleService]
})
export class Ac0401Component extends BaseComponent implements OnInit {

    url: string;
    file: { name: string, size: number };
    value: any;
    disabled: boolean = true;
    display: boolean = false;
    acConf: AA0510Resp;

    constructor(
        protected route: ActivatedRoute,
        protected tr: TransformMenuNamePipe,
        private messageService: MessageService,
        private moduleService: ModuleService,
        private toolService: ToolService,
        private location: Location,
        private translateService: TranslateService,
        private alertService: AlertService
    ) {
        super(route, tr);
        this.url = this.moduleService.uploadModuleUrl;
        // this.netUrl = '/tsmpnaa/04/aa0401';
    }

    ngOnInit() {
        this.acConf = this.toolService.getAcConf();
        // console.log(new Date);
        // var id = setInterval(() => {
        //   let res = this.toolService.isTokenExpired();
        //   console.log('isTokenExpired', res);
        //   if (res) {
        //     console.log(new Date);
        //     clearInterval(id);
        //   }
        // }, 1000)
    }

    excuteUpload(upload, authCode = '', netPath = '', isNet: boolean = false): Observable<boolean> {
        return Observable.create(obser => {
            let files = file && file.files && file.files.length ? file.files : $('#choose-file')[0].files;
            if (!files.length) return;
            let self = this;
            let fileExtension = (/[.]/.exec(files[0].name)) ? /[^.]+$/.exec(files[0].name)[0] : undefined;
            if (fileExtension != 'war' && this.acConf.edition == 'Express') { // Express版判斷檔案格式不為war檔
                const code = ['upload_fail', 'upload_need_war'];
                self.translateService.get(code).subscribe(i18n => {
                    self.messageService.add({ severity: 'info', summary: i18n['upload_fail'], detail: i18n['upload_need_war'] });
                });
                $('#choose-file').val('');
                self.file = undefined;
                self.disabled = true;
                self.display = false;
                return;
            }
            if ((fileExtension != 'war' && fileExtension != 'jar') && this.acConf.edition == 'Enterprise') { // Enterprise版判斷檔案格式不為jar、war檔
                const code = ['upload_fail', 'upload_need_jar_or_war'];
                self.translateService.get(code).subscribe(i18n => {
                    self.messageService.add({ severity: 'info', summary: i18n['upload_fail'], detail: i18n['upload_need_jar_or_war'] });
                });
                $('#choose-file').val('');
                self.file = undefined;
                self.disabled = true;
                self.display = false;
                return;
            }
            var formData = new FormData();
            for (var i = 0, file; file = files[i]; ++i) {
                formData.append('file', file);
            }
            let serverno = '1';
            let d = new Date();
            let date = dayjs(d).format('YYMMDDHHmmss');
            let alphaNumber = generate({ length: 6, numbers: true });
            let txDate = this.toolService.formateDate(d);
            formData.append('txSN', `${serverno}${date}${alphaNumber}`);
            formData.append('txDate', txDate);
            formData.append('txID', 'AA0401');
            formData.append('cID', sessionStorage.getItem('decode_token') ? JSON.parse(sessionStorage.getItem('decode_token'))['client_id'] : '');
            var xhr = new XMLHttpRequest();
            let token = this.toolService.getToken();
            var url = this.url;
            xhr.open('POST', url, true);
            xhr.setRequestHeader("Authorization", `Bearer ${token}`)
            xhr.addEventListener("error", function (evt) {
                self.messageService.add({ severity: 'error', summary: 'upload Error', detail: 'Upload Fail' });
            });
            if (xhr.upload) {
                xhr.upload.onprogress = this.updateProgress.bind(this);
            }
            xhr.onload = function (e) {
                let _res = JSON.parse(this.response);
                if (this.status == 200) {
                    obser.next(true);
                    if (upload) {
                        upload.clear();
                    } else {
                        $('#choose-file').val('');
                        self.file = undefined;
                    }
                    if (self.toolService.checkDpSuccess(_res.ResHeader)) { // 判斷API rtnCode為1100，表示上傳成功
                        const code = ['upload_file', 'upload_success'];
                        self.translateService.get(code).subscribe(i18n => {
                            self.messageService.add({ severity: 'info', summary: i18n['upload_file'], detail: i18n['upload_success'] });
                        });
                    }
                    else {
                        self.alertService.ok(`Return code : ${_res.ResHeader.rtnCode}`, _res.ResHeader.rtnMsg, null);
                    }
                }
                self.disabled = true;
                self.display = false;
            };
            xhr.send(formData);  // multipart/form-data
        });
    }

    uploadFiles(file, upload) {
        this.display = true;
        this.disabled = true;
        if (this.toolService.isTokenExpired()) {
            this.toolService.refreshToken().pipe(
                concatMap(() => this.excuteUpload(upload))
            ).subscribe();
        } else {
            this.excuteUpload(upload).subscribe();
        }
    }

    updateProgress(evt) {
        if (evt.lengthComputable) {
            var percentage = Math.round((evt.loaded / evt.total) * 100);
            // console.log(percentage);
            this.value = `${percentage}%`;
        }
    }

    onToastClose(event) {
        this.value = '';
    }

    openFileBrowser(event) {
        if (event.target.files.length != 0) {
            this.display = true;
            this.disabled = false;
            // this.file = ev.target.files[0];
            if (!this.file) this.file = { name: '', size: 0 };
            this.file.name = event.target.files[0].name;
            let isize = event.target.files[0].size / 1024
            this.file.size = (Math.round(isize * 100) / 100);
        }
        else {
            this.display = false;
            this.disabled = true;
            this.file = { name: '', size: 0 };
            return;
        }
    }

    prev() { this.location.back() }

}
