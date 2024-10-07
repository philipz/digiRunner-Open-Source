import { ToolService } from './../../../shared/services/tool.service';
import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import { FaqService } from '../../../shared/services/api-faq.service';
import { MessageService, LazyLoadEvent, ConfirmationService } from 'primeng/api';
import { faqList, DPB0027Req } from 'src/app/models/api/FaqService/dpb0027.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FaqFormComponent } from './faq-form/faq-form.component';

@Component({
    selector: 'app-np0113',
    templateUrl: './np0113.component.html',
    styleUrls: ['./np0113.component.css'],
    providers: [MessageService, ConfirmationService]
})
export class Np0113Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    formOperate = FormOperate;
    cols: ({ field: string; header: string; type?: undefined; } | { field: string; header: string; })[] = [];
    form: FormGroup;
    rowcount: number = 0;
    status: ({ label: string; value: number; } | { label: string; value: string })[] = [];
    curQuestionId: number = 0;
    faqList: Array<faqList> = new Array();
    dialogTitle: string = '';
    loading: boolean = false;

    constructor(
        protected _route: ActivatedRoute,
        protected _tr: TransformMenuNamePipe,
        private _fb: FormBuilder,
        private _faq: FaqService,
        private _message: MessageService,
        private _tool: ToolService,
        private confirmationService:ConfirmationService
    ) {
        super(_route, _tr);
        this.form = this._fb.group({
            keyword: new FormControl(''),
            dataStatus: new FormControl(1)
        });
    }

    ngOnInit() {
        this.init();
    }

    async create() {
        const codes = ['message.create', 'message.faq', 'message.success'];
        const dicts = await this._tool.getDict(codes);
        this.dialogTitle = dicts['message.create'];
        let data: FormParams = {
            operate: FormOperate.create,
            displayInDialog: true,
            afterCloseCallback: (r) => {
                console.log('create scuess:', r)
                if (r && this._tool.checkDpSuccess(r.ResHeader)) {
                    this.search();
                    this._message.add({ severity: 'success', summary: `${dicts['message.create']} ${dicts['message.faq']}`, detail: `${dicts['message.create']} ${dicts['message.success']}!` });
                }
            }
        }
        this._dialog.open(FaqFormComponent, data);
    }

    private async init(): Promise<void> {
        const codes = ['faq_name', 'faq_ans', 'status', 'order', 'all', 'active', 'inactive'];
        const dict = await this._tool.getDict(codes);
        this.cols = [
            { field: 'questionName', header: dict['faq_name'] },
            { field: 'answerName', header: dict['faq_ans'] },
            { field: 'dataStatus', header: dict['status'] },
            { field: 'dataSort', header: dict['order'] }
        ];
        this.status = [
            { label: dict['inactive'], value: 0 },
            { label: dict['active'], value: 1 },
            { label: dict['all'], value: '' }
        ];
        let ReqBody = {
            questionId: null,
            dataSort: null,
            keyword: this.form.get('keyword')!.value,
            dataStatus: this.form.get('dataStatus')!.value
        } as DPB0027Req
        this._faq.queryFaqLikeList_0_ignore1298(ReqBody).subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this.faqList = res.RespBody.faqList;
                this.cutStr(this.faqList);
                this.rowcount = this.faqList.length;
            }
            else {
                this.faqList = [];
                this.rowcount = this.faqList.length;
            }
        });
    }

    public search() {
        let ReqBody = {
            questionId: null,
            dataSort: null,
            keyword: this.form.get('keyword')!.value,
            dataStatus: this.form.get('dataStatus')!.value
        } as DPB0027Req
        this._faq.queryFaqLikeList_0(ReqBody).subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this.faqList = res.RespBody.faqList;
                this.cutStr(this.faqList);
                this.rowcount = this.faqList.length;
            }
            else {
                this.faqList = [];
                this.rowcount = this.faqList.length;
            }
        });
    }

    moreData() {
        this.loading = true;
        let ReqBody = {
            questionId: this.faqList[this.faqList.length - 1].questionId,
            dataSort: null,
            dataStatus: this.form.get('dataStatus')!.value
        } as DPB0027Req
        this._faq.queryFaqLikeList_0(ReqBody).subscribe(res => {
            if (this._tool.checkSuccess(res.ResHeader)) {
                this.faqList = this.faqList.concat(res.RespBody.faqList);
                this.cutStr(this.faqList);
                this.loading = false;
                this.rowcount = this.faqList.length;
            }
            else {
                this.loading = false;
            }
        });
    }

    public async showDialog(rowData: faqList, operation: FormOperate): Promise<void> {
        const codes = ['cfm_del_faq', 'message.update', 'message.success', 'message.faq','system_alert'];
        const dict = await this._tool.getDict(codes);
        this.curQuestionId = rowData.questionId;
        switch (operation) {
            case FormOperate.update:
                this.dialogTitle = dict['dialog.edit'];
                this._faq.queryFaqById({ questionId: rowData.questionId }).subscribe(res => {
                    if (this._tool.checkDpSuccess(res.ResHeader)) {
                        let data: FormParams = {
                            operate: FormOperate.update,
                            data: res.RespBody,
                            displayInDialog: true,
                            afterCloseCallback: (r) => {
                                if (r && this._tool.checkDpSuccess(r.ResHeader)) {
                                    this._message.add({
                                        severity: 'success', summary: `${dict['message.update']} ${dict['message.faq']}`,
                                        detail: `${dict['message.update']} ${dict['message.success']}!`
                                    });
                                    this.search();
                                }
                            }
                        }
                        this._dialog.open(FaqFormComponent, data);
                    }
                });
                break;
            case FormOperate.delete:
                this._message.clear();
                // this._message.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_faq'], detail: `${rowData.questionName}` });
                // this._message.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_faq'] });
                this.confirmationService.confirm({
                  header: dict['system_alert'],
                  message: dict['cfm_del_faq'],
                  accept: () => {
                      this.delete();
                  }
                });
                break;
        }
    }

    public cancel(): void {
        this._message.clear();
    }

    public async delete(): Promise<void> {
        const codes = ['message.delete', 'message.faq', 'message.success'];
        const dict = await this._tool.getDict(codes);
        this._message.clear();
        this._faq.deleteFaqById({ questionId: this.curQuestionId }).subscribe(res => {
            if (this._tool.checkDpSuccess(res.ResHeader)) {
                this._message.add({ severity: 'success', summary: `${dict['message.delete']} ${dict['message.faq']}`, detail: `${dict['message.delete']} ${dict['message.success']}!` });
                this.search();
            }
        });
    }

    public cutStr(data: Array<faqList>): void {
        if (data && data.length) {
            data.forEach(d => {
                if (d.questionName.length >= 50) {
                    d.questionName = d.questionName.substring(0, 50) + '...';
                }
                if (d.answerName.length >= 50) {
                    d.answerName = d.answerName.substring(0, 50) + '...';
                }
            });
        }
    }

    public colStyle(col): any {
        switch (col.field) {
            case 'questionName':
                return { width: '30%' };
                break;
            case 'answerName':
                return { width: '30%' };
                break;
            case 'dataStatusString':
                return { width: '100px' };
                break;
            case 'dataSort':
                return { width: '100px' };
                break;
            default:
                return { width: 'inherit' };
                break;
        }
    }
}
