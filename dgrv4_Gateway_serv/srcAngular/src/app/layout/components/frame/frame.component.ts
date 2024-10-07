import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { environment } from 'src/environments/environment';
import { TimeRange } from 'src/app/models/common.enum';
// const createHash = require("sha256-uint8array").createHash;
const base64url = require('base64url');
import * as shajs from 'sha.js';
import { AlertService } from 'src/app/shared/services/alert.service';

@Component({
  selector: 'app-frame',
  templateUrl: './frame.component.html',
  styleUrls: ['./frame.component.css'],
  providers: [UtilService],
})
export class FrameComponent implements OnInit {
  @Input('reportID') reportID: string = '';

  url: SafeResourceUrl = '';
  timeRange = TimeRange.Today;
  timeRanges;

  constructor(
    private sanitizer: DomSanitizer,
    private util: UtilService,
    private tool: ToolService,
    private alertService: AlertService
  ) {}

  ngOnInit() {
    // this.timeRanges = this.tool.getTimeRange();
    // let url = `${environment.reportUrl}/app/kibana#/dashboard/e2b7a7e0-d388-11e8-9cad-796349acce0a?embed=true&_g=(refreshInterval:(pause:!t,value:86400000),time:(from:now%2Fd,mode:quick,to:now%2Fd))&_a=(description:'%E6%AF%8F%E5%A4%A9%E5%B0%8D%E4%B8%8D%E5%90%8CAPI%E9%80%B2%E8%A1%8C%E5%91%BC%E5%8F%AB%E6%AC%A1%E6%95%B8%E7%9A%84%E7%B5%B1%E8%A8%88%E8%A1%A8%20(API%20count%20Group%20by%20API)%0A',filters:!(),fullScreenMode:!f,options:(darkTheme:!f,hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(),gridData:(h:28,i:'1',w:41,x:0,y:0),id:'21efc440-d386-11e8-9cad-796349acce0a',panelIndex:'1',type:visualization,version:'6.4.2')),query:(language:lucene,query:''),timeRestore:!f,title:'1.API%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88(%E5%88%86%E6%88%90%E5%8A%9F%2F%E5%A4%B1%E6%95%97)',viewMode:view)`;
    this.url = this.sanitizer.bypassSecurityTrustResourceUrl('');
    this.search();
  }

  search() {
    let uuidObj = this.generateCAPI();
    // if (this.reportID.toLocaleLowerCase().startsWith('za')) {
    //   let url = ''
    //   if(this.reportID == 'za0003'){
    //     url = `https://127.0.0.1:4080/dgrv4/cus/za00/za0001`;
    //   }else
    //   url = `https://127.0.0.1:4080/dgrv4/cus/za00/${this.reportID}`;
    //   this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    //   return;
    // }


    if (this.timeRange) {
      this.util
        .queryReportUrls({
          reportID: this.reportID.toUpperCase(), //此欄位必須大寫，否則若客戶使用Orcle資料庫/大小寫有區別會造成查無資料的錯誤
          timeRange: 'T',
        })
        .subscribe((r) => {
          if (this.tool.checkDpSuccess(r.ResHeader)) {
            // this.util.getAuthCode_v3({
            //     authType: 'Proxy',
            //     resource: "xxxx",
            //     subclass: "xxxxxxx"
            // }).subscribe(res => {
            //     if (this.tool.checkDpSuccess(res.ResHeader)) {

            //     }
            // });

            let tarUrl = '';
            if (
              location.hostname == 'localhost' ||
              location.hostname == '127.0.0.1'
            ) {
              tarUrl = environment.apiUrl;
            } else {
              tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
            }

            // SYS_RPT:kibana報表
            // IFRAME:嵌內頁面
            // LINK:另開頁面
            let url = '';
            switch (r.RespBody.reportType) {
              case "SYS_RPT":
                url = `${tarUrl}${r.RespBody.rpContentPath}/login?cuuid=${uuidObj.uuid}&capi_key=${uuidObj.capi}&reportURL=${r.RespBody.reportUrl}&embed=true`;
                this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
                break;
              case "IFRAME":
                //  url =`${location.protocol}//${location.hostname}:${location.port}${r.RespBody.reportUrl}`
                 url =`${environment.cusHostName}${r.RespBody.reportUrl}`
                 this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
                break;
              case "LINK":
                url =`${r.RespBody.reportUrl}`
                window.open(url, '_blank');
                break;
              default:
                // 預期reportType存在
                this.alertService.ok('reportType not found','');
                // url = `${tarUrl}${r.RespBody.rpContentPath}/login?cuuid=${uuidObj.uuid}&capi_key=${uuidObj.capi}&reportURL=${r.RespBody.reportUrl}&embed=true`;
                // this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
                break;
            }

            // let url = `${tarUrl}/kibana/login?reportURL=${r.RespBody.reportUrl}&embed=true`

          }
        });
    }
  }

  generate_uuid() {
    var d = Date.now();
    if (
      typeof performance !== 'undefined' &&
      typeof performance.now === 'function'
    ) {
      d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
      /[xy]/g,
      function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
      }
    );
  }

  generateCAPI(): { uuid: string; capi: string } {
    let _uuid = this.generate_uuid().toUpperCase();
    let _capi = this.encodeUUID(_uuid);

    /**20230617 若錯誤給預設值 */
    if (_capi === 'Error: Digest method not supported') {
      _uuid = '67C42D24-BCFD-40B1-99E9-C4CE4D9ADBCD';
      _capi = 'tflvB2FW3nhUIvF3PMHQxw';
    }

    let uuidEncode: { uuid: string; capi: string } = {
      uuid: _uuid,
      capi: _capi,
    };

    return uuidEncode;
  }

  // CAPI key演算法流程
  encodeUUID(uuid: string) {
    if (typeof uuid !== 'string' || uuid.length === 0) return '';
    const sTpi = 'TPIdigiRunner';
    const arrayTpi = Uint8Array.of(0x80);
    const textEncoder = new TextEncoder();

    try {
      let sTarget = uuid.toLocaleUpperCase() + sTpi;

      let u8PayloadBytes = textEncoder.encode(sTarget);
      let u8Result = this.concatenate(Uint8Array, u8PayloadBytes, arrayTpi);

      // let sSha256 = createHash("SHA256").update(u8Result).digest();
      // console.log('createHash',sSha256)
      let sSha256 = shajs('sha256').update(u8Result).digest();
      // console.log('shajs',test)
      let sHalfSha256 = sSha256.subarray(sSha256.length / 2, sSha256.length);
      const result = base64url(sHalfSha256);
      return result;
    } catch (e) {
      console.log('e', e);
      return e;
    }
  }

  concatenate(resultConstructor, ...arrays) {
    let totalLength = 0;
    for (const arr of arrays) {
      totalLength += arr.length;
    }
    const result = new resultConstructor(totalLength);
    let offset = 0;
    for (const arr of arrays) {
      result.set(arr, offset);
      offset += arr.length;
    }
    return result;
  }
}
