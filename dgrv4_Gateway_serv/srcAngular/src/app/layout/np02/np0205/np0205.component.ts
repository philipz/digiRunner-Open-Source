import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ClientCAService } from 'src/app/shared/services/api-certificate-authority.service';
import { DPB0229RespItem } from 'src/app/models/api/ServerService/dpb0229.interface';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AlertType } from 'src/app/models/common.enum';
import { DPB0226Req } from 'src/app/models/api/ServerService/dpb0226.interface';
import { DPB0225Req } from 'src/app/models/api/ServerService/dpb0225.interface';
import { DPB0231Req } from 'src/app/models/api/ServerService/dpb0231.interface';

@Component({
  selector: 'app-np0205',
  templateUrl: './np0205.component.html',
  styleUrls: ['./np0205.component.scss'],
  providers: [MessageService, ConfirmationService],
})
export class Np0205Component extends BaseComponent implements OnInit {
  form: FormGroup;
  cols: { field: string; header: string; width?: string }[] = [];
  dataList: Array<DPB0229RespItem> = new Array<DPB0229RespItem>();
  pageNum: number = 1;
  currentTitle = this.title;
  currentAction: string = '';
  btnData: MenuItem[] = [];
  @ViewChild('op') op;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private clientCA: ClientCAService,
    private confirmationService: ConfirmationService,
    private alertService: AlertService,
    private messageService: MessageService
  ) {
    super(route, tr);
    this.form = this.fb.group({
      siteUrl: new FormControl(''),
      rootCa: new FormControl(''),
      clientCert: new FormControl(''),
      clientKey: new FormControl(''),
      keyPassword: new FormControl(''),
      tag: new FormControl(''),
      enable: new FormControl(''),
    });
  }

  ngOnInit() {
    this.init();
  }

  async init() {
    const codes = [
      'status',
      'site',
      'ca_expired_time',
      'alert_desc',
      'label_tag',
      'last_update_user',
    ];
    const dict = await this.toolService.getDict(codes);

    this.cols = [
      { field: 'enable', header: dict['status'] },
      { field: 'siteUrl', header: dict['site'], width: '300px' },
      { field: 'expireDate', header: dict['ca_expired_time'] },
      { field: 'alert', header: dict['alert_desc'] },
      { field: 'tag', header: dict['label_tag'] },
      { field: 'updateUser', header: dict['last_update_user'] },
    ];
    this.dataList = [
      {
        enable: 1,
        siteUrl: 'https://api01.server.com:8443',
        expireDate: '2027/12/31',
        alert: '',
        tag: '核對戶籍用',
        updateDateTime: 1696993570720,
        updateUser: 'JUNIT_TEST',
      },
    ];
    // this.querySiteList();
  }

  querySiteList() {
    this.clientCA.querySiteList().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.infoList;
      }
    });
  }

  async changePage(action: string, rowData?: DPB0229RespItem) {
    const codes = [
      'site',
      'button.detail',
      'button.create',
      'button.update',
      'cfm_del',
      'message.delete',
      'message.success',
      'button.active',
      'button.disable',
      'cfm_mtls_enable',
      'cfm_mtls_disable',
      'fail'
    ];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.form);
    this.currentAction = action;
    this.form.enable();

    switch (action) {
      case 'default':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.clientCA.createSite_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
            this.enable?.setValue('1'); //預設啟用
            this.currentTitle += `> ${dict['button.create']}`;
            this.pageNum = 2;
          }
        });
        break;
      case 'update':
        this.clientCA
          .queryOneSite({ siteUrl: rowData!.siteUrl })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.siteUrl?.setValue(res.RespBody.siteUrl);
              this.enable?.setValue(res.RespBody.enable);
              this.rootCa?.setValue(res.RespBody.rootCa);
              this.clientCert?.setValue(res.RespBody.clientCert);
              this.clientKey?.setValue(res.RespBody.clientKey);
              this.keyPassword?.setValue(res.RespBody.keyPassword);
              this.tag?.setValue(res.RespBody.tag.split(','));

              this.clientCA.updateSite_before().subscribe((valid) => {
                this.addFormValidator(this.form, valid.RespBody.constraints);
                this.currentTitle += `> ${dict['button.update']}`;
                this.pageNum = 2;
              });
            }
          });

        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${dict['site']}:<br> ${rowData?.siteUrl}`,
          accept: () => {
            this.clientCA
              .deleteSite({ siteUrl: rowData!.siteUrl })
              .subscribe(async (res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['message.delete']} GTW API IdP Client List`,
                    detail: `${dict['message.delete']} ${dict['message.success']}!`,
                  });
                  this.querySiteList();
                }
              });
          },
        });

        break;
      case 'active':
        this.confirmationService.confirm({
          header: dict['button.active'],
          message: dict['cfm_mtls_enable'],
          accept: () => {
            this.clientCA
              .enableSite({ siteUrl: rowData!.siteUrl, enable: 1 })
              .subscribe((res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.alertService.ok(
                    res.RespBody.enable ? `${ dict['cfm_mtls_enable']} ${dict['message.success']}` : `${ dict['cfm_mtls_enable']} ${dict['message.fail']}`,
                    '',
                    res.RespBody.enable ? AlertType.success : AlertType.error
                  );
                }
                this.querySiteList();
              });
          },
        });

        break;
      case 'inactive':
        this.confirmationService.confirm({
          header: dict['button.disable'],
          message: dict['cfm_mtls_disable'],
          accept: () => {
            this.clientCA
              .enableSite({ siteUrl: rowData!.siteUrl, enable: 0 })
              .subscribe((res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.alertService.ok(
                    res.RespBody.enable ? `${ dict['cfm_mtls_disable']} ${dict['message.success']}` : `${ dict['cfm_mtls_disable']} ${dict['message.fail']}`,
                    '',
                    res.RespBody.enable ? AlertType.success : AlertType.error
                  );
                }
                this.querySiteList();
              });
          },
        });

        break;
    }
  }

  headerReturn() {
    this.changePage('default');
  }

  async create() {
    let req = {
      siteUrl: this.siteUrl?.value,
      rootCa: this.rootCa?.value,
      clientCert: this.clientCert?.value,
      clientKey: this.clientKey?.value,
      keyPassword: this.keyPassword?.value,
      tag: this.tag?.value ? (this.tag?.value).join(',') : this.tag?.value,
      enable: this.enable?.value,
    } as DPB0225Req;
    console.log(req);
    // this.clientCA.createSite(req).subscribe(async (res) => {
    //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
    //     const code = ['message.create', 'message.success'];
    //     const dict = await this.toolService.getDict(code);
    //     this.messageService.add({
    //       severity: 'success',
    //       summary: `${dict['message.create']}`,
    //       detail: `${dict['message.create']} ${dict['message.success']}!`,
    //     });

    //     this.querySiteList();
    //     this.changePage('default');
    //   }
    // });
  }

  update() {
    let req = {
      siteUrl: this.siteUrl?.value,
      rootCa: this.rootCa?.value,
      clientCert: this.clientCert?.value,
      clientKey: this.clientKey?.value,
      keyPassword: this.keyPassword?.value,
      tag: this.tag?.value,
      enable: this.enable?.value,
    } as DPB0226Req;

    // this.clientCA.updateSite(req).subscribe(async (res) => {
    //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
    //     const code = ['message.update', 'message.success'];
    //     const dict = await this.toolService.getDict(code);
    //     this.messageService.add({
    //       severity: 'success',
    //       summary: `${dict['message.update']}`,
    //       detail: `${dict['message.update']} ${dict['message.success']}!`,
    //     });

    //     this.querySiteList();
    //     this.changePage('default');
    //   }
    // });
  }

  async toggleBtnMenu(evt, rowData: DPB0229RespItem) {
    this.btnData = [];
    const code = ['button.active', 'button.disable'];
    const dict = await this.toolService.getDict(code);

    // 啟動
    let item = {
      label: dict['button.active'],
      command: () => {
        this.op.hide();
        this.changePage('active', rowData);
      },
    };
    this.btnData?.push(item);

    // 停用
    let disable = {
      label: dict['button.disable'],
      command: () => {
        this.op.hide();
        this.changePage('inactive', rowData);
      },
    };
    this.btnData?.push(disable);

    this.op.toggle(evt);
  }

  connectionTest() {
    let req = {
      siteUrl: this.siteUrl?.value,
      rootCa: this.rootCa?.value,
      clientCert: this.clientCert?.value,
      clientKey: this.clientKey?.value,
      keyPassword: this.keyPassword?.value,
    } as DPB0231Req;
    this.clientCA.checkMtlsConnection(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (res.RespBody.success) {
          this.alertService.ok(
            'Connect Success',
            res.RespBody.msg,
            AlertType.success
          );
        } else {
          this.alertService.ok(
            'Connect Fail',
            res.RespBody.msg,
            AlertType.error
          );
        }
      }
    });
  }

  public get siteUrl() {
    return this.form.get('siteUrl');
  }
  public get rootCa() {
    return this.form.get('rootCa');
  }
  public get clientCert() {
    return this.form.get('clientCert');
  }
  public get clientKey() {
    return this.form.get('clientKey');
  }
  public get keyPassword() {
    return this.form.get('keyPassword');
  }
  public get tag() {
    return this.form.get('tag');
  }
  public get enable() {
    return this.form.get('enable');
  }
}
