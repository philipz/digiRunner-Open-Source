import { Component, OnInit, Input } from '@angular/core';
import { BaseComponent } from 'src/app/layout/base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { MessageService } from 'primeng/api';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { TranslateService } from '@ngx-translate/core';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0053Req } from 'src/app/models/api/ThemeService/dpb0053.interface';
import { ThemeService } from 'src/app/shared/services/api-theme.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0056Resp } from 'src/app/models/api/ThemeService/dpb0056.interface';
import { DPB0054Req } from 'src/app/models/api/ThemeService/dpb0054.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { DPB0082Req } from 'src/app/models/api/FileService/dpb0082.interface';
import { of } from 'rxjs';

@Component({
    selector: 'app-theme-category',
    templateUrl: './theme-category.component.html',
    styleUrls: ['./theme-category.component.css'],
    providers: [MessageService]
})
export class ThemeCategoryComponent extends BaseComponent implements OnInit {

    @Input() data!: FormParams;
    @Input() close!: Function;

    form!: FormGroup;
    formOperate = FormOperate;
    _fileData?: File|undefined;
    _fileName: string = '';
    _originFileName: string = '';
    submitBtnName: string = '';
    themeNameMaxLength = { value: 100 };

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private translate: TranslateService,
        private alert: AlertService,
        private themeService: ThemeService,
        private tool: ToolService,
        private message: MessageService,
        private file: FileService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        const codes = ['button.create', 'button.update'];
        this.translate.get(codes).subscribe(i18n => this.init(i18n));
    }

    init(i18n) {
        switch (this.data && this.data.operate) {
            case FormOperate.create:
                this.submitBtnName = i18n['button.create'];
                this.form = this.fb.group(this.resetFormGroup(FormOperate.create)!);
                break;
            case FormOperate.update:
                this.submitBtnName = i18n['button.update'];
                this.form = this.fb.group(this.resetFormGroup(FormOperate.update)!);
                break;
        }
    }

    submitForm() {
        switch (this.data && this.data.operate) {
            case FormOperate.create:
                // TODO Multipart get temp file name
                window.setTimeout(() => {
                    this.file.uploadFile2(this._fileData!).subscribe(res => {
                        if (this.tool.checkDpSuccess(res.ResHeader)) {
                            let ReqBody = {
                                themeName: this.themeName!.value,
                                fileName: res.RespBody.tempFileName
                            } as DPB0053Req
                            this.themeService.createTheme(ReqBody).subscribe(res => {
                                if (this.tool.checkDpSuccess(res.ResHeader)) {
                                    this.form.reset();
                                    this._fileName = '';
                                    if (this.close) this.close(of(res));
                                }
                            });
                        }
                    });
                });
                break;
            case FormOperate.update:
                // TODO Multipart get temp file name
                let updateReqBody = {
                    themeId: this.data.data.themeId,
                    themeName: this.themeName!.value,
                    lv: this.data.data.lv
                } as DPB0054Req;
                if (this._fileName == this._originFileName) {
                    updateReqBody.fileName = null;
                    let updateObservable = this.themeService.updateTheme(updateReqBody);
                    if (this.close) this.close(updateObservable);
                }
                else {
                    window.setTimeout(() => {
                        this.file.uploadFile2(this._fileData!).subscribe(res => {
                            if (this.tool.checkDpSuccess(res.ResHeader)) {
                                updateReqBody.fileName = res.RespBody.tempFileName;
                                let updateObservable = this.themeService.updateTheme(updateReqBody);
                                if (this.close) this.close(updateObservable);
                            }
                        });
                    });
                }
                break;
        }
    }

    openFileBrowser() {
        $('#fileName').click();
    }

    async fileChange(files: FileList) {
        const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
        const dict = await this.tool.getDict(code);
        if (files.length != 0) {
            this.message.add({ severity: 'success', summary: dict['uploading'], detail: `${dict['waiting']}!` });
            let fileSize = files.item(0)!.size;
            let fileType = files.item(0)!.type;
            if (fileType != 'image/jpeg' && fileType != 'image/png' && fileType != 'image/gif') {
                this.message.clear();
                this.alert.ok(`Return message : `, dict['cfm_img_format'], undefined);
                this._fileName = '';
                this._fileData = undefined;
                return;
            }
            else if (fileSize / 1024 / 1024 > 5) {
                this.message.clear();
                this.alert.ok(`Return message : `, dict['cfm_size'], undefined);
                this._fileName = '';
                this._fileData = undefined;
                return;
            }
            else {
                let fileReader = new FileReader();
                fileReader.onloadend = () => {
                    this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
                    this._fileData = files.item(0)!;
                    this._fileName = files.item(0)!.name;
                }
                fileReader.readAsDataURL(files.item(0)!);
            }
        }
        else {
            this._fileName = '';
        }
    }

    private resetFormGroup(formOperate?: FormOperate) {
        //初始化
        if (!formOperate) return {
            themeName: new FormControl(''),
            fileName: new FormControl('')
        };
        switch (formOperate) {
            case FormOperate.create:
                return {
                    themeName: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringLengthValidator(this.themeNameMaxLength.value)]),
                    fileName: new FormControl('', ValidatorFns.requiredValidator())
                };
            case FormOperate.update:
                let updateFormData = this.data.data as DPB0056Resp;
                this._fileName = this._originFileName = updateFormData.fileName;
                return {
                    themeName: new FormControl(updateFormData.themeName, [ValidatorFns.requiredValidator(), ValidatorFns.stringLengthValidator(this.themeNameMaxLength.value)]),
                    fileName: new FormControl('')
                };
            default:
              return {
                themeName: new FormControl(''),
                fileName: new FormControl('')
            };
        }
    }

    public get themeName() { return this.form.get('themeName'); };
    public get fileName() { return this.form.get('fileName'); };
}
