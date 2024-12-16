import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0225 extends BaseReq {
  ReqBody: DPB0225Req;
}

export interface DPB0225Req {
  siteUrl: string;
  rootCa: string;
  clientCert: string;
  clientKey: string;
  keyPassword?: string;
  tag?: string;
  enable:boolean
}

export interface RespDPB0225 extends BaseRes {
  RespBody: DPB0225Resp;
}

export interface DPB0225Resp { }

export interface DPB0225RespBefore extends BaseRes {
  RespBody: RespDPB0225RespBefore;
}

export interface RespDPB0225RespBefore {
  constraints: Array<ValidatorFormat>;
}

