import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from '../../../models/common.enum';
import { ThemeCategoryComponent } from './theme-category/theme-category.component';
import { DPB0057Req } from 'src/app/models/api/ThemeService/dpb0057.interface';
import { DPB0055Themes, DPB0055Req } from 'src/app/models/api/ThemeService/dpb0055.interface';
import { ThemeService } from 'src/app/shared/services/api-theme.service';
import { FileService } from 'src/app/shared/services/api-file.service';
import { DPB0078Req } from 'src/app/models/api/FileService/dpb0078.interface';
import { DPB0056Req } from 'src/app/models/api/ThemeService/dpb0056.interface';

@Component({
    selector: 'app-np0105',
    templateUrl: './np0105.component.html',
    styleUrls: ['./np0105.component.css'],
    providers: [MessageService, ConfirmationService]
})
export class Np0105Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    form!: FormGroup;
    rowcount: number = 0;
    cols: { field: string; header: string; }[] = [];
    selected: Array<DPB0055Themes> = new Array();
    dialogTitle: string = '';
    dataList: Array<DPB0055Themes> = new Array();
    delList: Array<number> = new Array();
    loading: boolean = false;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private message: MessageService,
        private tool: ToolService,
        private themeService: ThemeService,
        private file: FileService,
        private confirmationService:ConfirmationService
    ) {
        super(route, tr);
    }

    async ngOnInit() {
        this.form = this.fb.group({
            keyword: new FormControl(''),
        });
        const codes = ['theme', 'theme_image'];
        const dicts = await this.tool.getDict(codes);
        this.cols = [
            { field: 'themeName', header: dicts['theme'] },
            { field: 'fileName', header: dicts['theme_image'] }
        ];
        this.selected = [];
        this.dataList = [];
        this.rowcount = 0;
        this.loading = true;
        let ReqBody = {
            keyword: this.form.get('keyword')!.value
        } as DPB0055Req;
        this.themeService.queryThemeLikeList_1_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.loading = false;
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
            else {
                this.loading = false;
            }
        });
    }

    async create() {
        const codes = ['message.create', 'message.theme', 'message.success'];
        const dicts = await this.tool.getDict(codes);
        this.dialogTitle = dicts['message.create'];
        let data: FormParams = {
            operate: FormOperate.create,
            displayInDialog: true,
            afterCloseCallback: (r) => {
                if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                    this.message.add({ severity: 'success', summary: `${dicts['message.create']} ${dicts['message.theme']}`, detail: `${dicts['message.create']} ${dicts['message.success']}!` });
                    this.submitForm();
                    this._dialog.open(ThemeCategoryComponent, data);
                }
            }
        }
        this._dialog.open(ThemeCategoryComponent, data);
    }

    submitForm() {
        this.selected = [];
        this.dataList = [];
        this.rowcount = 0;
        this.loading = true;
        let ReqBody = {
            keyword: this.form.get('keyword')!.value
        } as DPB0055Req;
        this.themeService.queryThemeLikeList_1(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.loading = false;
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
            else {
                this.loading = false;
            }
        });
    }

    moreData() {
        this.loading = true;
        let ReqBody = {
            themeId: this.dataList[this.dataList.length - 1].themeId,
            keyword: this.form.get('keyword')!.value
        } as DPB0055Req;
        this.themeService.queryThemeLikeList_1(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.loading = false;
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
            else {
                this.loading = false;
            }
        });
    }

    async showDialog(rowData: DPB0055Themes) {
        const codes = ['message.update', 'message.theme', 'message.success'];
        const dicts = await this.tool.getDict(codes);
        let ReqBody = {
            themeId: rowData.themeId
        } as DPB0056Req;
        this.themeService.queryThemeByPk(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dialogTitle = dicts['message.update'];
                let data: FormParams = {
                    operate: FormOperate.update,
                    data: res.RespBody,
                    displayInDialog: true,
                    afterCloseCallback: (r) => {
                        if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                            this.message.add({ severity: 'success', summary: `${dicts['message.update']} ${dicts['message.theme']}`, detail: `${dicts['message.update']} ${dicts['message.success']}!` });
                            this.submitForm();
                        }
                    }
                }
                this._dialog.open(ThemeCategoryComponent, data);
            }
        });
    }

    async delete() {
        this.delList = this.transformThemeId(this.selected);
        const codes = ['cfm_del_cate', 'cfm_del_cates','system_alert'];
        const dicts = await this.tool.getDict(codes);
        this.message.clear();
        // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: this.delList.length > 1 ? dicts['cfm_del_cates'] : dicts['cfm_del_cate'] });
        this.confirmationService.confirm({
          header: dicts['system_alert'],
          message: this.delList.length > 1 ? dicts['cfm_del_cates'] : dicts['cfm_del_cate'],
          accept: () => {
              this.onDeleteConfirm();
          }
        });
    }

    onDeleteConfirm() {
        this.message.clear();
        let ReqBody = {
            delList: this.delList
        } as DPB0057Req;
        this.themeService.deleteTheme(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['message.delete', 'message.theme', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.clear();
                this.message.add({ severity: 'success', summary: `${dicts['message.delete']} ${dicts['message.theme']}`, detail: `${dicts['message.delete']} ${dicts['message.success']}!` });
                this.submitForm();
                this.selected = new Array<DPB0055Themes>();
            }
        });
    }

    onReject() {
        this.message.clear();
    }

    downloadFile(filePath: string, fileName: string) {
        let ReqBody = {
            filePath: filePath
        } as DPB0078Req;
        this.file.downloadFile(ReqBody).subscribe(res => {
            const reader = new FileReader();
            reader.onloadend = function () {
                // if (window.navigator.msSaveOrOpenBlob) { //IE要使用 msSaveBlob
                //     window.navigator.msSaveBlob(res, fileName)
                // }
                // else {
                    const file = new File([res], fileName);
                    const url = window.URL.createObjectURL(file);
                    const a = document.createElement('a');
                    document.body.appendChild(a);
                    a.setAttribute('style', 'display: none');
                    a.href = url;
                    a.download = fileName;
                    a.click();
                    window.URL.revokeObjectURL(url);
                    a.remove();
                // }
            }
            reader.readAsText(res);
        });
    }

    transformThemeId(rowData: Array<DPB0055Themes>): Array<number> {
        this.delList = new Array<number>();
        rowData.map(item => {
            this.delList.push(item.themeId)
        });
        return this.delList;
    }

    fileNameConvert(fileName: string): string {
        if (fileName) {
            return decodeURIComponent(fileName);
        }
        return '';
    }
}
