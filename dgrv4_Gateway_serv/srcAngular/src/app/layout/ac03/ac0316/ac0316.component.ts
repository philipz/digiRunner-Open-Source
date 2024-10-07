import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { CommonAPI } from '../../../shared/register-api/common-api.class';
import { KeyValueComponent } from '../../../shared/key-value/key-value.module';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as shajs from 'sha.js'
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { GrantType } from 'src/app/models/common.enum';
import { ResToken } from '../../../models/api/TokenService/token.interface';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
// import { ModuleService } from 'src/app/shared/services/api-module.service';
// import { DCService } from 'src/app/shared/services/api-dc.service';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { AA0302Req } from 'src/app/models/api/ApiService/aa0302_v3.interface';
import { AA0422Req } from 'src/app/models/api/DCService/aa0422.interface';
import { FormParam, HttpHeader } from 'src/app/models/api/ApiService/aa0312_v3.interface';
import { ModuleService } from 'src/app/shared/services/api-module.service';
import { DCService } from 'src/app/shared/services/api-dc.service';

@Component({
  selector: 'app-ac0316',
  templateUrl: './ac0316.component.html',
  styleUrls: ['./ac0316.component.css'],
  providers: [ApiService, ModuleService, DCService]
})
export class Ac0316Component extends BaseComponent implements OnInit {

  @ViewChild('keyValueForm') kv_form!: KeyValueComponent;
  @ViewChild('keyValueRequest') kv_request!: KeyValueComponent;

  apiKey: string = '';
  moduleName: string = '';
  apiSrc: string = '';
  form: FormGroup;
  methods: { label: string; value: string; }[] = [];
  dcs: { label: string; value: string; }[] = [];
  sites: { label: string; value: string; }[] = [];
  resTime: string = "";
  funCode: string = '';
  protocol: string = '';
  acConf: AA0510Resp;

  initData:{[key:string]:string} = {};

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tokenService: TokenService,
    private toolService: ToolService,
    private api: ApiService,
    private httpClient: HttpClient,
    private ngxService: NgxUiLoaderService,
    private signBlockService: SignBlockService,
    private moduleService: ModuleService,
    private alert: AlertService,
    private tool: ToolService,
    private dc: DCService,

  ) {
    super(route, tr);
    this.acConf = this.tool.getAcConf();

    this.form = this.fb.group({
      enableClientAuth: new FormControl(true),
      enableUserAuth: new FormControl(false),
      userName: new FormControl({ value: '', disabled: true }),
      passwd: new FormControl({ value: '', disabled: true }),
      clientId: new FormControl(""),
      clientPasswd: new FormControl(""),
      tsmpUrl: new FormControl(""),
      method: new FormControl('POST'),
      requestHeader: new FormControl,
      ContentType: new FormControl("application/json"),
      requestBody: new FormControl('none'),
      keyValueRequest: new FormControl(""),
      keyValueForm: new FormControl(""),
      bodyText: new FormControl(""),
      resBody: new FormControl({ value: '', disabled: false }),
      resStatus: new FormControl({ value: '', disabled: false }),
      headerList: new FormControl({ value: '', disabled: false }),
      dc: new FormControl(''),
      siteCode: new FormControl(''),
      baseUrl: new FormControl(location.hostname + (location.port ? ':' + location.port : ''))
    });
  }

  ngOnInit() {
    // this.route.params.subscribe((value) => {
    //   this.apiKey = value["apiKey"] == undefined ? "" : value["apiKey"];
    //   this.moduleName = value["moduleName"] == undefined ? "" : value["moduleName"];
    //   this.apiSrc = value["apiSrc"] == undefined ? "" : value["apiSrc"];
    // });
    // this.protocol = location.protocol;
    // this.init();
    // this.loadData();

    //---------

    this.route.params.subscribe((value) => {
      this.apiKey = value["apiKey"] == undefined ? "" : value["apiKey"];
      this.moduleName = value["moduleName"] == undefined ? "" : value["moduleName"];
      this.apiSrc = value["apiSrc"] == undefined ? "" : value["apiSrc"];
      this.initData = {
        'apiKey': this.apiKey,
        'moduleName' : this.moduleName,
        'apiSrc': this.apiSrc
      }
    });

  }

  public init(): void {
    this.methods = CommonAPI.methods;
  }

  public loadData(): void {
    if (this.apiKey == "" || this.moduleName == "") { return; }
    let ReqBody = {
      moduleName: this.moduleName,
      apiKey: this.apiKey
    } as AA0302Req;
    this.api.queryAPIDetail_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        var url: string = "";
        let _moduleName = res.RespBody.moduleName.t ? res.RespBody.moduleName.o : res.RespBody.moduleName.v;
        let _apiKey = res.RespBody.apiKey.t ? res.RespBody.apiKey.o : res.RespBody.apiKey.v;
        switch (this.apiSrc) {
          case 'M':
          default:
            let ReqBody = {
              moduleName: _moduleName
            } as AA0422Req;
            this.dc.queryDCList_2(ReqBody).subscribe(resp => {
              if (this.tool.checkDpSuccess(resp.ResHeader)) {
                let pattern = new RegExp("[\\[\\]\"]");
                let endpoint = "";
                let _pathOfJson = res.RespBody?.pathOfJson?.t ? res.RespBody.pathOfJson.o : res.RespBody!.pathOfJson?.v;
                if (_pathOfJson) {
                  for (let i = 0; i < _pathOfJson.length; i++) {
                    endpoint = endpoint + _pathOfJson.substr(i, 1).replace(pattern, '');
                  }
                }

                if (!resp.RespBody.dcList || _moduleName?.includes('tsmpaa')) { // 沒有部署主機，或是tsmpaa Module
                  url = _moduleName + endpoint;
                }
                else {
                  let dcOptions:{label:string, value:string}[] = [];
                  let activeDcCode = resp.RespBody.dcList.filter(dc => dc.active == true);
                  if (activeDcCode.length != 0) {
                    for (let item of activeDcCode) {
                      dcOptions.push({ label: this.acConf.dcPrefix + item.dcCode, value: item.dcCode });
                    }
                  }
                  else {
                    resp.RespBody.dcList.map(item => {
                      dcOptions.push({ label: this.acConf.dcPrefix + item.dcCode, value: item.dcCode });
                    });
                  }
                  // const codes = ['default_cr'];
                  // const dict = await this.toolService.getDict(codes);
                  // dcOptions.push({ label: dict['default_cr'], value: '' });
                  this.dcs = dcOptions;
                  this.form.get('dc')!.setValue(this.dcs[0].value);
                  let _dcPrefix = this.acConf.dcPrefix.substring(1, this.acConf.dcPrefix.length);
                  url = _dcPrefix + this.form.get('dc')!.value + '/' + _moduleName + endpoint;
                  this.form.get('dc')!.valueChanges.subscribe(option => {
                    let url = _dcPrefix + option + '/' + this.moduleName + endpoint;
                    if (option == '')
                      url = this.moduleName + endpoint;
                    this.form.get('tsmpUrl')!.setValue(url);
                  });
                }
                this.form.get("tsmpUrl")!.setValue(url);
              }
            });
            break;
          // case "N":
          //     let req = {
          //         moduleName: _moduleName,
          //         detailFlag: false
          //     };
          //     this.moduleService.querySiteList(req).subscribe(async resNaa0412 => {
          //         if (this.tool.checkSuccess(resNaa0412.resHeader)) {
          //             let siteCodeOptions = [];
          //             for (let item of resNaa0412.res_0412.siteInfoList) {
          //                 siteCodeOptions.push({ label: item.siteCode, value: item.siteCode });
          //             }
          //             this.sites = siteCodeOptions;
          //             url = `tsmpc/${this.sites[0].value}/${_moduleName}/${_apiKey}`;
          //             this.form.get("tsmpUrl").setValue(url);
          //             this.form.get('siteCode').valueChanges.subscribe(option => {
          //                 let url = `tsmpc/${option}/${_moduleName}/${_apiKey}`;
          //                 this.form.get('tsmpUrl').setValue(url);
          //             });
          //         }
          //     });
          //     break;
          case "C":
          case "R":
            url = "tsmpc/" + _moduleName + "/" + _apiKey;
            this.form.get("tsmpUrl")!.setValue(url);
            break;
        }
        //處理Methods
        if (res.RespBody.methodOfJson) {
          let _method = res.RespBody.methodOfJson.split(',');
          this.form.get("method")!.setValue(_method.length > 0 ? _method[0] : 'POST');
        }
      }
    });
  }

  public add(componentName: string): void {
    componentName === 'request' ? this.kv_request!.add() : this.kv_form.add();
  }

  startTimestamp;
  async send(): Promise<void> {
    const codes = ['dialog.warn', 'client_id_required', 'client_pwd_required', 'user_name_required', 'password_required', 'digirunner_required', "header_key_required"];
    const dict = await this.toolService.getDict(codes);
    this.form.get("resBody")!.setValue("");
    this.form.get("resStatus")!.setValue("");
    this.form.get("headerList")!.setValue("");

    this.resTime = "";

    var clientId = this.form.get("clientId")!.value;
    var clientPassword = this.form.get("clientPasswd")!.value;
    var userName = this.form.get("userName")!.value;
    var passwd = this.form.get("passwd")!.value;
    let grant_type = GrantType.password;

    if (this.form.get('enableClientAuth')!.value) {
      grant_type = GrantType.client_credentials;
      if (clientId == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_id_required']);
        return;
      }
      if (clientPassword == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_pwd_required']);
        return;
      }
      clientPassword = this.toolService.Base64Encoder(clientPassword);
    }

    if (this.form.get("enableUserAuth")!.value) {
      grant_type = GrantType.password;

      if (userName == "") {
        this.alert.ok(dict['dialog.warn'], dict['user_name_required']);
        return;
      }

      if (passwd == "") {
        this.alert.ok(dict['dialog.warn'], dict['password_required']);
        return;
      }
      passwd = this.toolService.Base64Encoder(passwd);
    }

    if (this.form.get("tsmpUrl")!.value.trim() == "") {
      this.alert.ok(dict['dialog.warn'], dict['digirunner_required']);
      return;
    }

    let headerList: HttpHeader[] = new Array<HttpHeader>();
    headerList = headerList.concat(this.keyvalueConvetToList(this.form.get("keyValueRequest")!.value));
    var isVaild: boolean = true;
    headerList.forEach(function (value) {
      if (value != undefined) {
        if (value.name.trim() == "" && value.value.trim() != "") {
          isVaild = false;
        }
      }
    });
    if (isVaild != true) { this.alert.ok(dict['dialog.warn'], dict['header_key_required']); return; }

    this.ngxService.start();
    let path = this.protocol + '//' + this.form.get('baseUrl')!.value + "/" + this.form.get("tsmpUrl")!.value;
    headerList = new Array<HttpHeader>();
    headerList = headerList.concat(this.keyvalueConvetToList(this.form.get("keyValueRequest")!.value));
    let body = this.reqBodyHandler();
    if (this.form.get('enableClientAuth')!.value || this.form.get("enableUserAuth")!.value) {
      if (clientId == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_id_required']);
        this.ngxService.stop();
        return;
      }
      if (clientPassword == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_pwd_required']);
        this.ngxService.stop();
        return;
      }

      this.tokenService.authBytestApi(clientId, clientPassword, userName, passwd, grant_type).subscribe((r: ResToken) => {
        this.signBlockService.getTestSignBlock(r.access_token).subscribe(res => {
          let signBlock = res.Res_getSignBlock.signBlock;
          let token = r.access_token;
          let header = this.createHeader(token, signBlock, headerList, body);
          this.startTimestamp = new Date().getTime();
          this.httpClientHandle(path, header, body);
        });
      });


    }
    else {
      let ContentType: string = this.form.get("ContentType")!.value;
      let header = new HttpHeaders();
      //content-Type (若是multipart/form-data 不用加)
      if (ContentType != "" && ContentType.toLowerCase() != "multipart/form-data".toLowerCase()) {
        header = header.append("Content-Type", ContentType);
      }
      headerList.forEach(function (value) {
        if (value != undefined) {
          if (value.name != "" && value.value != "") { header = header.append(value.name, value.value); }
        }
      });
      this.startTimestamp = new Date().getTime();
      this.httpClientHandle(path, header, body);
    }
  }

  private reqBodyHandler(): any {
    let body;
    switch (this.form.get("requestBody")!.value) {
      case "body":
        body = this.form.get("bodyText")!.value;
        break;
      case "xml":
        body = this.form.get("bodyText")!.value;
        break;
      case "form":
        let paramList: FormParam[] = new Array<FormParam>();
        paramList = paramList.concat(this.keyvalueConvetToList(this.form.get("keyValueForm")!.value));
        body = new FormData;
        for (var i = 0; i < paramList.length; i++) {
          var param = paramList[i];
          body.append(param.name, param.value);
        }
        break;
      case "formurl":
        paramList = new Array<FormParam>();
        paramList = paramList.concat(this.keyvalueConvetToList(this.form.get("keyValueForm")!.value));
        body = new URLSearchParams();
        for (var i = 0; i < paramList.length; i++) {
          var param = paramList[i];
          body.set(param.name, param.value);
        }
        body = body.toString();
        break;
      case "none":
        body = null;
        break;
    }
    return body;
  }

  private httpClientHandle(path: string, header: HttpHeaders, body?: any) {
    switch (this.form.get("method")!.value.toUpperCase()) {
      case "GET":
        this.httpClient.get(`${path}`, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
          //處理結果
          this.bindResResult(res);
        });
        break;
      case "HEAD":
        this.httpClient.head(`${path}`, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
          //處理結果
          this.bindResResult(res);
        });
        break;
      case "POST":
        this.httpClient.post(`${path}`, body, { headers: header, observe: 'response', responseType: 'text' }).subscribe(res => {
          //處理結果
          this.bindResResult(res);

        });
        break;
      case "PUT":
        this.httpClient.put(`${path}`, body, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
          //處理結果
          this.bindResResult(res);
        });
        break;
      case "DELETE":
        this.httpClient.delete(`${path}`, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
          //處理結果
          this.bindResResult(res);
        });
        break;
      case "CONNECT":
        // console.log("CONNECT");
        break;
      case "OPTIONS":
        this.httpClient.options(`${path}`, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
          //處理結果
          this.bindResResult(res);
        });
        break;
      case "TRACE":
        // console.log("TRACE");
        break;
      case "PATCH":
        this.httpClient.patch(`${path}`, body, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
          //處理結果
          this.bindResResult(res);
        });
        break;
    }
  }

  private bindResResult(data: any): void {

    //處理回應時間
    this.calResponseTime();

    let main_headers = {}
    const keys = data.headers.keys();
    let headers = keys.map(key => {
      `${key}: ${data.headers.get(key)}`
      main_headers[key] = data.headers.get(key)
    }
    );

    this.form.get("resBody")!.setValue(data.body);
    this.form.get("resStatus")!.setValue(data.status);
    this.form.get("headerList")!.setValue(JSON.stringify(main_headers));

    this.ngxService.stop();

  }

  public enableClientAuthHandler(): void {
    var isCheck = this.form.get("enableClientAuth")!.value;

    if (isCheck == true) {
      this.form.get("clientId")!.enable();
      this.form.get("clientPasswd")!.enable();
    }
    else {
      this.form.get("clientId")!.setValue("");
      this.form.get("clientId")!.disable();
      this.form.get("clientPasswd")!.setValue("");
      this.form.get("clientPasswd")!.disable();
    }
  }

  public enableUserAuthHandler(): void {
    var isCheck = this.form.get("enableUserAuth")!.value;

    if (isCheck == true) {
      this.form.get("userName")!.enable();
      this.form.get("passwd")!.enable();
    }
    else {
      this.form.get("userName")!.setValue("");
      this.form.get("userName")!.disable();

      this.form.get("passwd")!.setValue("");
      this.form.get("passwd")!.disable();
    }
  }

  public handleChange() {

    switch (this.form.get("requestBody")!.value) {

      case "body":
      case "none":
        this.form.get("ContentType")!.setValue("application/json");
        break;
      case "form":
        this.form.get("ContentType")!.setValue("multipart/form-data");
        break;

      case "formurl":
        this.form.get("ContentType")!.setValue("application/x-www-form-urlencoded");
        break;
      case "xml":
        this.form.get("ContentType")!.setValue("application/xml");
        break;
    }
  }

  keyvalueConvetToList(keyvalues: { key: string, value: string }[]) {
    let formatResult:HttpHeader[] = []
    if (keyvalues && keyvalues.length) {
      // let headerList: T[] = new Array<T>();
      formatResult =  keyvalues.map(keyvalue => {
        return { name: keyvalue.key, value: keyvalue.value } as HttpHeader
      })
    }
    return formatResult;
  }

  private createHeader(token: string, signBlock: string, headers: HttpHeader[], body?: any): HttpHeaders {

    var ContentType: string = this.form.get("ContentType")!.value;

    let signCode = this.cryptSignCode(body, signBlock);
    let header = new HttpHeaders({
      'SignCode': signCode,
      'Authorization': `Bearer ${token}`
    });

    //content-Type (若是multipart/form-data 不用加)
    if (ContentType != "" && ContentType.toLowerCase() != "multipart/form-data".toLowerCase()) {
      header = header.append("Content-Type", ContentType);
    }

    headers.forEach(function (value) {
      if (value != undefined) {

        if (value.name != "" && value.value != "") { header = header.append(value.name, value.value); }
      }
    });

    return header;
  }

  private cryptSignCode(body: Object, signBlock: string): string {
    // const prefix = 'DzANBgNVBAgTBnRhaXdhbjEPMA0GA1UEBxMGdGFpcGVpMRMwEQYDVQQKEwp0aGlu';
    const prefix = signBlock;
    const bodyJosn = body;
    // console.log(prefix + bodyJosn);
    return shajs('sha256').update(prefix + bodyJosn).digest('hex');
  }

  private calResponseTime(): void {
    const endTimestamp: number = new Date().getTime();
    const responseTimes = endTimestamp - this.startTimestamp;
    this.resTime = responseTimes + "ms";
  }
}
