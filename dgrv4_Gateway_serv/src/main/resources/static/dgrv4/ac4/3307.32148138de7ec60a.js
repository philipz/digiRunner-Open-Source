"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[3307],{23307:(N,f,i)=>{i.r(f),i.d(f,{Np1201Module:()=>Q});var n=i(69808),T=i(88893),v=i(24783),d=i(93075),L=i(86347),x=i(99291),g=i(15861),C=i(14525),p=i(59783),t=i(87587),c=i(56435),y=i(92340),_=i(57553),b=i(96614);let A=(()=>{class o{constructor(e){this.api=e,this.api.baseUrl=y.N.dpPath}get basePath(){return y.N.isv4?"dgrv4/11":"tsmpdpaa/11"}queryModuleLikeList_ignore1298(e){let l={ReqHeader:this.api.getReqHeader(_.Nx.queryModuleLikeList),ReqBody:e};return this.api.excuteNpPost_ignore1298(`${this.basePath}/DPB0040`,l)}queryModuleLikeList(e){let l={ReqHeader:this.api.getReqHeader(_.Nx.queryModuleLikeList),ReqBody:e};return this.api.npPost(`${this.basePath}/DPB0040`,l)}saveDeniedModule(e){let l={ReqHeader:this.api.getReqHeader(_.Nx.saveDeniedModule),ReqBody:e};return this.api.npPost(`${this.basePath}/DPB0041`,l)}}return o.\u0275fac=function(e){return new(e||o)(t.LFG(b.K))},o.\u0275prov=t.Yz7({token:o,factory:o.\u0275fac,providedIn:"root"}),o})();var M=i(45682),D=i(3937),R=i(63710),Z=i(23099),E=i(17773),F=i(51062);function O(o,a){if(1&o){const e=t.EpF();t.TgZ(0,"div",16)(1,"button",17),t.NdJ("click",function(){return t.CHM(e),t.oxw(2).saveDenied()}),t._uU(2),t.ALo(3,"translate"),t.qZA()()}2&o&&(t.xp6(2),t.hij(" ",t.lcZ(3,1,"button.setting")," "))}const S=function(o){return{width:o}};function P(o,a){1&o&&t._UZ(0,"col",20),2&o&&t.Q6J("ngStyle",t.VKq(1,S,"deniedString"===a.$implicit.field?"10%":"auto"))}function U(o,a){if(1&o&&(t.TgZ(0,"colgroup"),t._UZ(1,"col",18),t.YNc(2,P,1,3,"col",19),t.qZA()),2&o){const e=a.$implicit;t.xp6(2),t.Q6J("ngForOf",e)}}function B(o,a){if(1&o&&(t.TgZ(0,"th",23),t._uU(1),t.qZA()),2&o){const e=a.$implicit;t.Q6J("ngStyle",t.VKq(2,S,"deniedString"===e.field?"10%":"auto")),t.xp6(1),t.hij(" ",e.header," ")}}function w(o,a){if(1&o&&(t.TgZ(0,"tr")(1,"th",21),t._UZ(2,"p-tableHeaderCheckbox"),t.qZA(),t.YNc(3,B,2,4,"th",22),t.qZA()),2&o){const e=a.$implicit;t.xp6(3),t.Q6J("ngForOf",e)}}function H(o,a){if(1&o&&(t.TgZ(0,"td",20),t._uU(1),t.qZA()),2&o){const e=a.$implicit,l=t.oxw().$implicit;t.Q6J("ngStyle",t.VKq(2,S,"deniedString"===e.field?"10%":"auto")),t.xp6(1),t.hij(" ",l[e.field]," ")}}function I(o,a){if(1&o&&(t.TgZ(0,"tr")(1,"td",18),t._UZ(2,"p-tableCheckbox",24),t.qZA(),t.YNc(3,H,2,4,"td",19),t.qZA()),2&o){const e=a.$implicit,l=a.columns;t.xp6(2),t.Q6J("value",e),t.xp6(1),t.Q6J("ngForOf",l)}}function J(o,a){if(1&o){const e=t.EpF();t.TgZ(0,"tr")(1,"td")(2,"span"),t._uU(3),t.ALo(4,"translate"),t.qZA(),t.TgZ(5,"button",26),t.NdJ("click",function(){return t.CHM(e),t.oxw(3).moreDate()}),t._uU(6),t.ALo(7,"translate"),t.qZA()()()}if(2&o){const e=t.oxw().$implicit,l=t.oxw(2);t.xp6(1),t.uIk("colspan",e.length+1),t.xp6(2),t.AsE("",t.lcZ(4,4,"row_count"),": ",l.rowCount,""),t.xp6(3),t.Oqu(t.lcZ(7,6,"button.more"))}}function k(o,a){if(1&o&&t.YNc(0,J,8,8,"tr",25),2&o){const e=t.oxw(2);t.Q6J("ngIf",e.rowCount)}}function Y(o,a){if(1&o&&(t.TgZ(0,"tr")(1,"td"),t._uU(2),t.ALo(3,"translate"),t.qZA()()),2&o){const e=a.$implicit;t.xp6(1),t.uIk("colspan",e.length+1),t.xp6(1),t.hij(" ",t.lcZ(3,2,"no_rec")," ")}}const G=function(){return{"word-break":"break-word"}};function j(o,a){if(1&o){const e=t.EpF();t.TgZ(0,"p-table",9),t.NdJ("selectionChange",function(s){return t.CHM(e),t.oxw().selectedModules=s})("onRowSelect",function(s){return t.CHM(e),t.oxw().handleDeniedStatus(s)})("onRowUnselect",function(s){return t.CHM(e),t.oxw().handleDeniedStatus(s)})("onHeaderCheckboxToggle",function(s){return t.CHM(e),t.oxw().handleDeniedStatus(s)}),t.YNc(1,O,4,3,"ng-template",10),t.YNc(2,U,3,1,"ng-template",11),t.YNc(3,w,4,1,"ng-template",12),t.YNc(4,I,4,2,"ng-template",13),t.YNc(5,k,1,1,"ng-template",14),t.YNc(6,Y,4,4,"ng-template",15),t.qZA()}if(2&o){const e=t.oxw();t.Akn(t.DdM(5,G)),t.Q6J("columns",e.cols)("value",e.moduleList)("selection",e.selectedModules)}}const $=function(){return{marginTop:"60px"}},K=[{path:"",component:(()=>{class o extends C.H{constructor(e,l,s,r,h,m,u){super(e,l),this.fb=s,this.doc=r,this.tool=h,this.ngx=m,this.message=u,this.cols=[],this.rowCount=0,this.moduleList=[],this.selectedModules=[]}ngOnInit(){var e=this;this.form=this.fb.group({moduleName:new d.NI("")}),this.init(),this.doc.queryModuleLikeList_ignore1298({moduleName:null,moduleVersion:null}).subscribe(function(){var s=(0,g.Z)(function*(r){if(e.tool.checkDpSuccess(r.ResHeader)){const h=["open","non_publicise"],m=yield e.tool.getDict(h);e.moduleList=r.RespBody.moduleList,e.rowCount=e.moduleList.length,e.selectedModules=e.moduleList.filter(u=>"1"==u.deniedFlag),e.moduleList.map(u=>{u.deniedString="1"==u.deniedFlag?m.non_publicise:m.open})}});return function(r){return s.apply(this,arguments)}}())}init(){var e=this;return(0,g.Z)(function*(){const s=yield e.tool.getDict(["denied_status","module_name","module_version"]);e.cols=[{field:"deniedString",header:s.denied_status},{field:"moduleName",header:s.module_name}]})()}loadModuleList(){var e=this;this.doc.queryModuleLikeList({moduleName:null,moduleVersion:null}).subscribe(function(){var s=(0,g.Z)(function*(r){if(e.tool.checkDpSuccess(r.ResHeader)){const h=["open","non_publicise"],m=yield e.tool.getDict(h);e.moduleList=r.RespBody.moduleList,e.rowCount=e.moduleList.length,e.selectedModules=e.moduleList.filter(u=>"1"==u.deniedFlag),e.moduleList.map(u=>{u.deniedString="1"==u.deniedFlag?m.non_publicise:m.open})}});return function(r){return s.apply(this,arguments)}}())}moreDate(){var e=this;this.ngx.start(),this.doc.queryModuleLikeList({moduleName:this.moduleList[this.moduleList.length-1].moduleName,moduleVersion:this.moduleList[this.moduleList.length-1].moduleVersion}).subscribe(function(){var s=(0,g.Z)(function*(r){if(e.tool.checkDpSuccess(r.ResHeader)){e.ngx.stop();const h=["open","non_publicise"],m=yield e.tool.getDict(h);e.moduleList=e.moduleList.concat(r.RespBody.moduleList),e.rowCount=e.moduleList.length,e.selectedModules=e.moduleList.filter(u=>"1"==u.deniedFlag),e.moduleList.map(u=>{u.deniedString="1"==u.deniedFlag?m.non_publicise:m.open})}});return function(r){return s.apply(this,arguments)}}())}submitForm(){var e=this;this.ngx.start();let l={moduleName:null,moduleVersion:null,keyword:this.form.get("moduleName").value};this.doc.queryModuleLikeList(l).subscribe(function(){var s=(0,g.Z)(function*(r){if(e.tool.checkDpSuccess(r.ResHeader)){const h=["open","non_publicise"],m=yield e.tool.getDict(h);e.ngx.stop(),e.moduleList=r.RespBody.moduleList,e.rowCount=e.moduleList.length,e.selectedModules=e.moduleList.filter(u=>"1"==u.deniedFlag),e.moduleList.map(u=>{u.deniedString="1"==u.deniedFlag?m.non_publicise:m.open})}});return function(r){return s.apply(this,arguments)}}())}handleDeniedStatus(e){var l=this;return(0,g.Z)(function*(){const r=yield l.tool.getDict(["open","non_publicise"]);e.hasOwnProperty("data")&&(e.data.deniedString=e.data.deniedString==r.open?r.non_publicise:r.open),e.hasOwnProperty("checked")&&l.moduleList.map(h=>h.deniedString=e.checked?r.non_publicise:r.open)})()}saveDenied(){var e=this;this.ngx.start();let l={moduleNames:this.moduleNameFormat(this.selectedModules)};this.doc.saveDeniedModule(l).subscribe(function(){var s=(0,g.Z)(function*(r){if(e.tool.checkDpSuccess(r.ResHeader)){e.ngx.stop();const h=["message.setting","denied_status","message.success"],m=yield e.tool.getDict(h);e.message.add({severity:"success",summary:`${m["message.setting"]} ${m.denied_status}`,detail:`${m["message.setting"]} ${m["message.success"]}!`}),e.selectedModules=new Array,e.loadModuleList()}});return function(r){return s.apply(this,arguments)}}())}moduleNameFormat(e){let l="";for(let[s,r]of e.entries())l+=s!=e.length-1?r.moduleName+",":r.moduleName;return l}}return o.\u0275fac=function(e){return new(e||o)(t.Y36(x.gz),t.Y36(c.W),t.Y36(d.qu),t.Y36(A),t.Y36(M.g),t.Y36(D.LA),t.Y36(p.ez))},o.\u0275cmp=t.Xpm({type:o,selectors:[["app-np1201"]],features:[t._Bn([p.ez]),t.qOj],decls:11,vars:9,consts:[[3,"title"],[3,"formGroup","ngSubmit"],[1,"row"],[1,"col-12"],[1,"p-input-icon-right",2,"width","40vw"],[1,"pi","pi-search","tpi-i-search",3,"click"],["type","search","id","moduleName","formControlName","moduleName",1,"form-control","tpi-i-input",3,"placeholder"],["styleClass","p-datatable-striped",3,"columns","value","selection","style","selectionChange","onRowSelect","onRowUnselect","onHeaderCheckboxToggle",4,"ngIf"],["position","top-left"],["styleClass","p-datatable-striped",3,"columns","value","selection","selectionChange","onRowSelect","onRowUnselect","onHeaderCheckboxToggle"],["pTemplate","caption"],["pTemplate","colgroup"],["pTemplate","header"],["pTemplate","body"],["pTemplate","footer"],["pTemplate","emptymessage"],[1,"ui-helper-clearfix"],["type","button",1,"btn","tpi-primary","float-left","mr-1",3,"click"],[2,"width","2.25em"],[3,"ngStyle",4,"ngFor","ngForOf"],[3,"ngStyle"],["scope","col",2,"width","2.25em"],["scope","col",3,"ngStyle",4,"ngFor","ngForOf"],["scope","col",3,"ngStyle"],[3,"value"],[4,"ngIf"],["type","button",1,"btn","btn-warning","pull-right","ml-3",3,"click"]],template:function(e,l){1&e&&(t.TgZ(0,"app-container",0)(1,"form",1),t.NdJ("ngSubmit",function(){return l.submitForm()}),t.TgZ(2,"div",2)(3,"div",3)(4,"span",4)(5,"i",5),t.NdJ("click",function(){return l.submitForm()}),t.qZA(),t._UZ(6,"input",6),t.ALo(7,"translate"),t.qZA()()()(),t._UZ(8,"hr"),t.YNc(9,j,7,6,"p-table",7),t.qZA(),t._UZ(10,"p-toast",8)),2&e&&(t.Q6J("title",l.title),t.xp6(1),t.Q6J("formGroup",l.form),t.xp6(5),t.s9C("placeholder",t.lcZ(7,6,"module_name")),t.xp6(3),t.Q6J("ngIf",l.cols),t.xp6(1),t.Akn(t.DdM(8,$)))},directives:[R.e,d._Y,d.JL,d.sg,d.Fj,d.JJ,d.u,n.O5,Z.iA,p.jx,n.sg,n.PC,Z.Mo,Z.UA,E.FN],pipes:[F.X$],styles:[""]}),o})(),canActivate:[L.u6]}];let q=(()=>{class o{}return o.\u0275fac=function(e){return new(e||o)},o.\u0275mod=t.oAB({type:o}),o.\u0275inj=t.cJS({imports:[[x.Bz.forChild(K)],x.Bz]}),o})(),Q=(()=>{class o{}return o.\u0275fac=function(e){return new(e||o)},o.\u0275mod=t.oAB({type:o}),o.\u0275inj=t.cJS({providers:[L.u6],imports:[[n.ez,q,T.W,v.m,d.UX,d.u5]]}),o})()},63710:(N,f,i)=>{i.d(f,{e:()=>C});var n=i(87587),T=i(69808),v=i(51062);function d(p,t){if(1&p&&(n.TgZ(0,"div",9)(1,"h3",10),n._uU(2),n.ALo(3,"translate"),n.qZA()()),2&p){const c=n.oxw();n.xp6(2),n.Oqu(n.lcZ(3,1,c.title))}}function L(p,t){if(1&p){const c=n.EpF();n.TgZ(0,"div",11)(1,"button",12),n.NdJ("click",function(){return n.CHM(c),n.oxw().return()}),n._UZ(2,"i",13),n._uU(3),n.ALo(4,"translate"),n.qZA(),n.TgZ(5,"span",14),n._uU(6),n.qZA(),n.TgZ(7,"span",15),n._uU(8),n.qZA()()}if(2&p){const c=n.oxw();n.xp6(3),n.hij(" ",n.lcZ(4,3,"button.return_to_list")," "),n.xp6(3),n.hij("",c.getHead()," /"),n.xp6(2),n.Oqu(c.getTail())}}const x=[[["","center-view","center"]],"*"],g=["[center-view=center]","*"];let C=(()=>{class p{constructor(){this.title="",this.isDefault=!0,this.headerReturn=new n.vpe}ngOnInit(){}return(){this.headerReturn.emit(null)}getHead(){const c=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return c.pop(),c.join(" / ")}getTail(){const c=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return c[c.length-1]}}return p.\u0275fac=function(c){return new(c||p)},p.\u0275cmp=n.Xpm({type:p,selectors:[["app-container"]],inputs:{title:"title",isDefault:"isDefault"},outputs:{headerReturn:"headerReturn"},ngContentSelectors:g,decls:11,vars:2,consts:[[1,"h-100"],[1,"container-fluid","h-100",2,"padding-left","10px","margin-top","10px"],[1,"row","h-100","position-relative"],[1,"col","pb-5"],[1,"card-title","row"],["class","col-12 col-md-12",4,"ngIf"],["class","col-12 col-md-12","style","text-align: right;",4,"ngIf"],[1,"col","d-flex","justify-content-center"],[1,"my-0","mb-2"],[1,"col-12","col-md-12"],["id","content",1,"bd-title","mb-0"],[1,"col-12","col-md-12",2,"text-align","right"],["type","button","icon","",1,"btn","float-left","tpi-header-return",3,"click"],[1,"fas","fa-arrow-left",2,"margin-right","5px"],[1,"bd-title","mb-0",2,"color","#666464"],[1,"bd-title","mb-0",2,"color","#FF6E38","font-weight","bold"]],template:function(c,y){1&c&&(n.F$t(x),n.TgZ(0,"div",0)(1,"div",1)(2,"div",2)(3,"div",3)(4,"div",4),n.YNc(5,d,4,3,"div",5),n.YNc(6,L,9,5,"div",6),n.TgZ(7,"div",7),n.Hsn(8),n.qZA()(),n._UZ(9,"hr",8),n.Hsn(10,1),n.qZA()()()()),2&c&&(n.xp6(5),n.Q6J("ngIf",y.isDefault),n.xp6(1),n.Q6J("ngIf",!y.isDefault))},directives:[T.O5],pipes:[v.X$],styles:[".card.card-body[_ngcontent-%COMP%]   h3[_ngcontent-%COMP%]{font-size:20px;font-weight:400;color:#5a5541}"]}),p})()},92718:(N,f,i)=>{i.d(f,{u:()=>t});var n=i(54004),T=i(70262),v=i(62843),d=i(87587),L=i(45682),x=i(89709),g=i(99291),C=i(3937),p=i(60991);let t=(()=>{class c{constructor(_,b,A,M,D){this.toolService=_,this.tokenService=b,this.router=A,this.ngxService=M,this.logoutService=D}canActivate(){return this.ngxService.stopAll(),!this.toolService.isTokenExpired()||this.toolService.refreshToken().pipe((0,n.U)(_=>!!_.access_token),(0,T.K)(this.handleError.bind(this)))}handleError(_){return setTimeout(()=>this.logoutService.logout()),(0,v._)(()=>_)}}return c.\u0275fac=function(_){return new(_||c)(d.LFG(L.g),d.LFG(x.B),d.LFG(g.F0),d.LFG(C.LA),d.LFG(p.P))},c.\u0275prov=d.Yz7({token:c,factory:c.\u0275fac}),c})()},86347:(N,f,i)=>{i.d(f,{DL:()=>n.D,u6:()=>v.u});var n=i(81233),v=(i(45240),i(92718));i(45682)}}]);