import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, NgForm } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { DPB0068Resp } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import * as dayjs from 'dayjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { MessageService } from 'primeng/api';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0065Req, DPB0065ClientReg } from 'src/app/models/api/RequisitionService/dpb0065.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { FileService } from 'src/app/shared/services/api-file.service';
import { RequisitionService } from 'src/app/shared/services/api-requisition.service';
import { DPB0066Req, DPB0066ClientReg } from 'src/app/models/api/RequisitionService/dpb0066.interface';
import { of } from 'rxjs';
import { TOrgService } from 'src/app/shared/services/org.service';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { BaseComponent } from 'src/app/layout/base-component';

@Component({
    selector: 'app-client-registered',
    templateUrl: './client-registered.component.html',
    styleUrls: ['./client-registered.component.css']
})
export class ClientRegisteredComponent extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;
    @Input() data!: FormParams;
    @Input() close!: Function;

    form: FormGroup;
    // forms: NgForm;
    minDateValue: Date = new Date();
    createDateTime: string = '';
    btnName: string = '';
    formOperate = FormOperate;
    minLength6 = { value: 6 };
    publicFlags: { label: string; value: string; }[] = [];
    dialogTitle: string =  '';
    orgName: string = '';
    fileName:string="";

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private list: ListService,
        private ngxService: NgxUiLoaderService,
        private message: MessageService,
        private file: FileService,
        private requisition: RequisitionService,
        private orgService: TOrgService
    ) {
        super(route, tr);
        this.form = this.fb.group({
          reqDesc: '',
          effectiveDate: '',
          clientId: '',
          emails: '',
          clientBlock: '',
          newClientBlock: '',
          confirmClientBlock: '',
          encPublicFlag: '',
          fileData: '', // multipart file data
          oriFileName: '', // original file name
          tmpFileName: '' // temp file name
        })
     }

    async ngOnInit() {
        this.form = this.fb.group(this.resetFormGroup(this.data.operate)!);
        let ReqBody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_AUTHORITY')) + ',' + 7,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _publicFlags:{label:string, value:string}[] = [];
                res.RespBody.subItems?.map(item => {
                    if (item.subitemNo != '-1') {
                        _publicFlags.push({ label: item.subitemName, value: item.subitemNo });
                    }
                });
                this.publicFlags = _publicFlags;
            }
        });
        const code = ['button.save', 'button.update', 'button.resend'];
        const dict = await this.tool.getDict(code);
        switch (this.data.operate) {
            case FormOperate.create:
                this.btnName = dict['button.save'];
                this.createDateTime = dayjs(new Date()).format('YYYY/MM/DD');
                this.form.controls['clientBlock'].setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form), ValidatorFns.requiredValidator(), ValidatorFns.minLengthValidator(this.minLength6.value)]);
                this.form.controls['confirmClientBlock'].setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form), ValidatorFns.requiredValidator(), ValidatorFns.minLengthValidator(this.minLength6.value)]);
                this.orgService.queryTOrgList({ orgID: this.tool.getOrgId() }).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.orgName = res.RespBody.orgList[0].orgName;
                    }
                });
                break;
            case FormOperate.update:
            case FormOperate.resend:
                this.btnName = this.data.operate == FormOperate.update ? dict['button.update'] : dict['button.resend'];
                this.form.controls['newClientBlock'].setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form, true), ValidatorFns.requiredValidator(), ValidatorFns.minLengthValidator(this.minLength6.value)]);
                this.form.controls['confirmClientBlock'].setValidators([ValidatorFns.confirmPasswordForClientValidator(this.form, true), ValidatorFns.requiredValidator(), ValidatorFns.minLengthValidator(this.minLength6.value)]);
                break;
        }

        this.requisition.createReqClientReq_beforer().subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
            }
        });


    }



    async fileChange(file: FileList) {
        const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
        const dict = await this.tool.getDict(code);
        if (file.length != 0) {
            let fileReader = new FileReader();
            fileReader.onloadend = () => {
                this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
                this.fileData!.setValue(file.item(0));
            }
            fileReader.readAsBinaryString(file.item(0)!);
            this.fileName = file[0].name;
        }
        else {
            this.fileData!.setValue(null);
        }
    }

    submitForm() {
        switch (this.data.operate) {
            case FormOperate.create:
                if (this.fileData!.value) {
                    window.setTimeout(() => {
                        this.file.uploadFile2(this.fileData!.value).subscribe(res => {
                            if (this.tool.checkDpSuccess(res.ResHeader)) {
                                this.tmpFileName!.setValue(res.RespBody.tempFileName);
                                this.createRequisition();
                            }
                        });
                    });
                }
                else {
                    this.tmpFileName!.setValue(null);
                    this.createRequisition();
                }
                break;
            case FormOperate.update:
            case FormOperate.resend:
                if (this.fileData!.value) {
                    window.setTimeout(() => {
                        this.file.uploadFile2(this.fileData!.value).subscribe(res => {
                            if (this.tool.checkDpSuccess(res.ResHeader)) {
                                this.tmpFileName!.setValue(res.RespBody.tempFileName);
                                this.updateOrResendRequisition();
                            }
                        });
                    });
                }
                else {
                    this.tmpFileName!.setValue(null);
                    this.updateOrResendRequisition();
                }
                break;
        }
    }

    createRequisition() {
        let ReqBody = {
            reqType: 'CLIENT_REG',
            effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD'),
            reqDesc: this.reqDesc!.value
        } as DPB0065Req;
        let _clientReg = {
            clientId: this.clientId!.value,
            clientName: this.clientName!.value,
            emails: this.emails!.value,
            clientBlock: this.tool.Base64Encoder(this.clientBlock!.value),
            encPublicFlag: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encPublicFlag!.value)) + ',' + this.encPublicFlag!.value
        } as DPB0065ClientReg;
        if (this.tmpFileName!.value) {
            _clientReg.tmpFileName = this.tmpFileName!.value
        }
        ReqBody.clientRegD = _clientReg;
        // console.log('ReqBody :', ReqBody)
        this.ngxService.start();
        this.requisition.createReq(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.ngxService.stop();
                const code = ['message.save', 'message.requisition.client_register', 'message.success'];
                const dict = await this.tool.getDict(code);
                this.message.add({ severity: 'success', summary: `${dict['message.save']} ${dict['message.requisition.client_register']}`, detail: `${dict['message.save']} ${dict['message.success']}!` });
                this.form.reset();
                this.form = this.fb.group(this.resetFormGroup(this.data.operate));
                $('#file').after($('#file').clone().val(""));
                $('#file').remove();
            }
        });
    }

    updateOrResendRequisition() {
        let updateReqBody = {
            reqOrdermId: this.data.data.reqOrdermId,
            lv: this.data.data.lv,
            act: this.data.operate == FormOperate.update ? 'U' : 'R',
            reqDesc: this.reqDesc!.value,
            effectiveDate: dayjs(this.effectiveDate!.value).format('YYYY/MM/DD')
        } as DPB0066Req;
        let _clientReg = {
            clientId: this.clientId!.value,
            clientName: this.clientName!.value,
            emails: this.emails!.value,
            clientBlock: this.tool.Base64Encoder(this.newClientBlock!.value),
            encPublicFlag: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encPublicFlag!.value)) + ',' + this.encPublicFlag!.value,
            newFileName: this.tmpFileName!.value ? this.tmpFileName!.value : this.oriFileName!.value,
            oriFileName: this.oriFileName!.value
        } as DPB0066ClientReg;
        updateReqBody.clientRegD = _clientReg;
        // console.log('resend :', updateReqBody)
        this.requisition.resendReq(updateReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                if (this.close) this.close(of(res.RespBody));
            }
        });
    }

    private resetFormGroup(formOperate?: FormOperate) {
        //初始化
        if (!formOperate) return {
            reqDesc: '',
            effectiveDate: '',
            clientId: '',
            emails: '',
            clientBlock: '',
            newClientBlock: '',
            confirmClientBlock: '',
            encPublicFlag: '',
            fileData: '', // multipart file data
            oriFileName: '', // original file name
            tmpFileName: '' // temp file name
        };
        switch (formOperate) {
            case FormOperate.create:
                return {
                    reqDesc: new FormControl('', ValidatorFns.requiredValidator()),
                    effectiveDate: new FormControl(dayjs(new Date()).format('YYYY/MM/DD'), ValidatorFns.requiredValidator()),
                    clientId: new FormControl('', ValidatorFns.requiredValidator()),
                    clientName: new FormControl('', ValidatorFns.requiredValidator()),
                    emails: new FormControl(''),
                    clientBlock: new FormControl(''),
                    newClientBlock: new FormControl(''),
                    confirmClientBlock: new FormControl(''),
                    encPublicFlag: new FormControl('', ValidatorFns.requiredValidator()),
                    fileData: new FormControl(null),
                    oriFileName: new FormControl(''),
                    tmpFileName: new FormControl('')
                }
            case FormOperate.update:
            case FormOperate.resend:
                let detailData = this.data.data as DPB0068Resp;
                return {
                    reqDesc: new FormControl(detailData.reqDesc, ValidatorFns.requiredValidator()),
                    effectiveDate: new FormControl(detailData.effectiveDate, ValidatorFns.requiredValidator()),
                    clientId: new FormControl(detailData.clientReg!.clientId, ValidatorFns.requiredValidator()),
                    clientName: new FormControl(detailData.clientReg!.clientName, ValidatorFns.requiredValidator()),
                    emails: new FormControl(detailData.clientReg!.emails),
                    clientBlock: new FormControl(''),
                    newClientBlock: new FormControl(''),
                    confirmClientBlock: new FormControl(''),
                    encPublicFlag: new FormControl(detailData.clientReg!.publicFlag, ValidatorFns.requiredValidator()),
                    fileData: new FormControl(null),
                    oriFileName: new FormControl(detailData.clientReg!.fileName),
                    tmpFileName: new FormControl('')
                };
            default:
              return {
                reqDesc: '',
                effectiveDate: '',
                clientId: '',
                emails: '',
                clientBlock: '',
                newClientBlock: '',
                confirmClientBlock: '',
                encPublicFlag: '',
                fileData: '', // multipart file data
                oriFileName: '', // original file name
                tmpFileName: '' // temp file name
            };
        }
    }

    openFileBrowser() {
        $('#file').click();
    }

    public get reqDesc() { return this.form.get('reqDesc'); };
    public get effectiveDate() { return this.form.get('effectiveDate'); };
    public get clientId() { return this.form.get('clientId'); };
    public get clientName() { return this.form.get('clientName'); };
    public get emails() { return this.form.get('emails'); };
    public get clientBlock() { return this.form.get('clientBlock'); };
    public get newClientBlock() { return this.form.get('newClientBlock'); };
    public get confirmClientBlock() { return this.form.get('confirmClientBlock'); };
    public get encPublicFlag() { return this.form.get('encPublicFlag'); };
    public get fileData() { return this.form.get('fileData'); };
    public get oriFileName() { return this.form.get('oriFileName'); };
    public get tmpFileName() { return this.form.get('tmpFileName'); };

}
