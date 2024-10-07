import { Component, OnInit, Input } from '@angular/core';
import { BaseComponent } from 'src/app/layout/base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { FaqService } from 'src/app/shared/services/api-faq.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0026Req } from 'src/app/models/api/FaqService/dpb0026.interface';
import { DPB0028Req } from 'src/app/models/api/FaqService/dpb0028.interface';

@Component({
    selector: 'app-faq-form',
    templateUrl: './faq-form.component.html',
    styleUrls: ['./faq-form.component.css']
})
export class FaqFormComponent extends BaseComponent implements OnInit {

    @Input() data!: FormParams;
    @Input() close!: Function;

    _fileName: string = '';
    form: FormGroup;
    submitButtonName: string = '';
    status: { label: string; value: number; }[] = [];
    fileName: string = '';
    fileContent: string = '';
    formOperate = FormOperate;

    constructor(
        protected _route: ActivatedRoute,
        protected _tr: TransformMenuNamePipe,
        private _fb: FormBuilder,
        private _faq: FaqService,
        private _tool: ToolService,
    ) {
        super(_route, _tr);
        this.form = this._fb.group({
            questionId: new FormControl(),
            questionName: new FormControl(''),
            answerId: new FormControl(),
            answerName: new FormControl(''),
            dataSort: new FormControl(''),
            orgFileName: new FormControl(''),
            orgFileId: new FormControl(),
            faqAnswerId: new FormControl(),
            dataStatus: new FormControl('1')
        });
    }

    async ngOnInit() {
        const codes = ['button.create', 'button.update', 'active', 'inactive'];
        const dict = await this._tool.getDict(codes);
        //處理狀態
        this.status = [
            { label: dict['active'], value: 1 },
            { label: dict['inactive'], value: 0 },
        ];
        switch (this.data && this.data.operate) {
            case FormOperate.create:
                this.submitButtonName = dict['button.create'];
                break;
            case FormOperate.update:
                this.form.get('questionId')!.setValue(this.data.data.questionId);
                this.form.get('questionName')!.setValue(this.data.data.questionName);
                this.form.get('answerId')!.setValue(this.data.data.answerId);
                this.form.get('answerName')!.setValue(this.data.data.answerName);
                this.form.get('dataSort')!.setValue(this.data.data.dataSort);
                this.form.get('orgFileName')!.setValue(this.data.data.orgFileName);
                this.form.get('orgFileId')!.setValue(this.data.data.orgFileId);
                this.form.get('dataStatus')!.setValue(this.data.data.dataStatus == '啟用' ? '1' : '0');
                this.submitButtonName = dict['button.update'];
                break;
        }
    }

    public submitForm() {
        switch (this.data && this.data.operate) {
            case FormOperate.create:
                let createReqBody = {
                    questionName: this.form.get('questionName')!.value,
                    dataStatus: this.form.get('dataStatus')!.value,
                    dataSort: this.form.get('dataSort')!.value,
                    answerName: this.form.get('answerName')!.value,
                    fileName: this.fileName,
                    fileContent: this.fileContent
                } as DPB0026Req
                let createObservable = this._faq.addFaq(createReqBody);
                if (this.close) this.close(createObservable);
                break;
            case FormOperate.update:
                let updateReqBody = {
                    questionId: this.form.get('questionId')!.value,
                    questionName: this.form.get('questionName')!.value,
                    answerId: this.form.get('answerId')!.value,
                    answerName: this.form.get('answerName')!.value,
                    dataSort: this.form.get('dataSort')!.value,
                    fileName: this.fileName,
                    fileContent: this.fileContent,
                    orgFileName: this.form.get('orgFileName')!.value,
                    orgFileId: this.form.get('orgFileId')!.value,
                    dataStatus: this.form.get('dataStatus')!.value,
                } as DPB0028Req
                // console.log('update data :', updateReqBody)
                let updateObservable = this._faq.updateFaqById(updateReqBody);
                if (this.close) this.close(updateObservable);
                break;
        }
    }

    handleFile(files: FileList) {
        let fileReader = new FileReader();
        fileReader.onloadend = () => {
            this.fileName = files.item(0)!.name;
            // this.fileContent = fileReader.result;
            this.fileContent = fileReader.result!.toString().split('base64,')[1];
        }
        fileReader.readAsDataURL(files.item(0)!);
    }

    deleteFile() {
        this.form.get('orgFileName')!.setValue('');
        this.form.get('orgFileId')!.setValue('');
    }
}
