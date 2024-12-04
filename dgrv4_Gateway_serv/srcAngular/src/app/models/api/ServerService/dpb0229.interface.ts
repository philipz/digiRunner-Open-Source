import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0229 extends BaseReq {
  ReqBody: DPB0229Req;
}

export interface DPB0229Req {}

export interface RespDPB0229 extends BaseRes {
  RespBody: DPB0229Resp;
}

export interface DPB0229Resp {
  infoList: Array<DPB0229RespItem>;
}

export interface DPB0229RespItem {
  siteUrl: string;
  expireDate: string;
  alert: string;
  tag: string;
  updateDateTime: string|number;
  updateUser: string;
  enable:number;
}

// export interface DPB0229RespBefore extends BaseRes {
//   RespBody: RespDPB0229RespBefore;
// }

// export interface RespDPB0229RespBefore {
//   constraints: Array<ValidatorFormat>;
// }
