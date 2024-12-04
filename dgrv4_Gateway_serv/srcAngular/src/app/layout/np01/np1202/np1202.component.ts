import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { OpenApiKeyService } from 'src/app/shared/services/api-open-api-key.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import * as dayjs from 'dayjs';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import {
  DPB0234Req,
  DPB0234RespItem,
  DPB0234RespItemFromXapiKey,
} from 'src/app/models/api/ServerService/dpb0234.interface';
import * as ValidatorFns from '../../../shared/validator-functions';

@Component({
  selector: 'app-np1202',
  templateUrl: './np1202.component.html',
  styleUrls: ['./np1202.component.css'],
  providers: [MessageService, ConfirmationService],
})
export class Np1202Component extends BaseComponent implements OnInit {
  @ViewChild('keyWords', { static: true })
  keyWordsRef!: ElementRef<HTMLInputElement>;
  @ViewChild('xApiKey', { static: true })
  xApiKeyRef!: ElementRef<HTMLInputElement>;
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;

  currentAction: string = '';
  dataList: Array<DPB0234RespItem> = [];
  dataListFromXapiKey: Array<DPB0234RespItemFromXapiKey> = [];
  apiCount: string  ='';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private openApiService: OpenApiKeyService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);

    this.form = this.fb.group({
      flag: new FormControl('keyWords'),
      keyWords: new FormControl('', ValidatorFns.requiredValidator()),
      xApiKey: new FormControl(''),
    });
  }

  async ngOnInit() {}

  ngAfterViewInit() {
    this.flag.value == 'keyWords'
      ? this.keyWordsRef.nativeElement.focus()
      : this.xApiKeyRef.nativeElement.focus();
  }

  axios_queryApiStatusByGroup() {
    this.dataList = [];
    this.dataListFromXapiKey = [];
    this.apiCount = '';
    this.ngxSrvice.start();
    let req = {
      flag: this.flag.value,
      keyWords: this.flag.value == 'keyWords' ? this.keyWords.value : '',
      xApiKey: this.flag.value == 'xApiKey' ? this.xApiKey.value : '',
    } as DPB0234Req;
    this.serverService.queryApiStatusByGroup(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.dataListFromXapiKey = res.RespBody.dataListFromXapiKey;
        this.apiCount = res.RespBody.totalApi;
      }
      this.ngxSrvice.stop();
    });
  }

  tabKeys: string[] = ['keyWords', 'xApiKey'];
  tabChange(evt: any) {
    this.resetFormValidator(this.form);
    this.apiCount = '';
    this.dataList = [];
    this.dataListFromXapiKey = [];
    this.flag.setValue(this.tabKeys[evt.index]);
    if (this.tabKeys[evt.index] === 'keyWords') {
      this.keyWords.setValidators(ValidatorFns.requiredValidator());
      this.keyWords.updateValueAndValidity();
      setTimeout(() => {
        this.keyWordsRef.nativeElement.focus();
      }, 0);
    } else {
      this.xApiKey.setValidators(ValidatorFns.requiredValidator());
      this.xApiKey.updateValueAndValidity();
      setTimeout(() => {
        this.xApiKeyRef.nativeElement.focus();
      }, 0);
    }
  }

  formateDate(date: any, formate: string = 'YYYY/MM/DD HH:mm:ss') {
    return dayjs(parseInt(date)).format(formate) != 'Invalid Date'
      ? dayjs(parseInt(date)).format(formate)
      : '';
  }

  public get keyWords() {
    return this.form.get('keyWords')!;
  }
  public get xApiKey() {
    return this.form.get('xApiKey')!;
  }
  public get flag() {
    return this.form.get('flag')!;
  }
}
