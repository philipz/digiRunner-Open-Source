"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[533],{12410:(N,b,s)=>{s.d(b,{X:()=>D});var t=s(87587),e=s(93075),E=s(45682),L=s(45330),x=s(12145),v=s(40845),M=s(69808),C=s(42629),f=s(59783),y=s(51062);function O(c,g){if(1&c){const l=t.EpF();t.TgZ(0,"button",11),t.NdJ("click",function(){return t.CHM(l),t.oxw().showCreateDialog()}),t._uU(1),t.ALo(2,"translate"),t.qZA()}2&c&&(t.xp6(1),t.Oqu(t.lcZ(2,1,"button.create")))}function T(c,g){if(1&c&&(t.TgZ(0,"div",12)(1,"span",13),t._uU(2),t.qZA()()),2&c){const l=g.$implicit,n=t.oxw();t.xp6(1),t.s9C("id",l.data.orgName+"_"+n.uuid),t.xp6(1),t.Oqu(l.data.orgName)}}function A(c,g){if(1&c){const l=t.EpF();t.TgZ(0,"p-footer")(1,"div",14)(2,"button",15),t.NdJ("click",function(){return t.CHM(l),t.oxw().saveConfirmData()}),t._uU(3),t.ALo(4,"translate"),t.qZA(),t.TgZ(5,"button",16),t.NdJ("click",function(){return t.CHM(l),t.oxw().cancelOrgDialog()}),t._uU(6),t.ALo(7,"translate"),t.qZA()()()}2&c&&(t.xp6(3),t.Oqu(t.lcZ(4,2,"button.confirm")),t.xp6(3),t.Oqu(t.lcZ(7,4,"button.cancel")))}let D=(()=>{class c{constructor(l,n,a,r){this.fb=l,this.tool=n,this.config=a,this.ref=r,this.showCreateBtn=!1,this.showFooterBtn=!1,this.doCreate=new t.vpe,this._selectedNode=new t.vpe,this.orgs=[],this.currentHighlight="",this.orgNameSuggestions=[],this.uuid=""}ngOnInit(){var l;this.config.data?(this._orgList=this.config.data.orgList,this.showFooterBtn=!0):this.showFooterBtn=!1,this.uuid=this._uuid(),this.form=this.fb.group({orgName:""});let n=[],a=[];this._orgList.map(r=>a.push(r.orgID)),this._orgList.map(r=>{-1==a.indexOf(r.parentID)?(r.master=!0,n.push({label:r.orgName,expanded:!1,data:r,children:[]})):n.push({label:r.orgName,expanded:!1,data:r,children:[]})});for(let r of n){let d=[...n];for(let h of d)r.data.orgID==h.data.parentID&&(null===(l=null==r?void 0:r.children)||void 0===l||l.push(h),r.expanded=!0)}this.orgs=n.find(r=>1==r.data.master)?[n.find(r=>1==r.data.master)]:[]}ngAfterViewInit(){if("#/ac00/ac0002"==window.location.hash){let l=document.getElementsByClassName("ui-treenode-selectable");window.setTimeout(()=>{var n,a,r,d,h;for(let o=0;o<l.length;o++)null===(n=null==l?void 0:l.item(o))||void 0===n||n.classList.add("ui-cursor");let m=document.getElementById((null===(r=null===(a=this._orgList)||void 0===a?void 0:a.find(o=>o.orgID==this.tool.getOrgId()))||void 0===r?void 0:r.orgName)+"_"+this.uuid);do{m=m.parentElement}while("TABLE"!=m.nodeName);for(let o=0;o<m.getElementsByClassName("ui-treenode-selectable").length;o++)null===(h=null===(d=null==m?void 0:m.getElementsByClassName("ui-treenode-selectable"))||void 0===d?void 0:d.item(o))||void 0===h||h.classList.remove("ui-cursor")})}}showCreateDialog(){this.doCreate.emit(!0)}saveConfirmData(){this.ref.close(this.selectedOrgNode)}cancelOrgDialog(){this.ref.close()}autoComplete(l){this.orgNameSuggestions=this.doSugges(l.query,this._orgList)}doSugges(l,n){let a=[];for(let r=0;r<n.length;r++){let d=n[r];d.orgName.toLowerCase().includes(l.toLowerCase())&&a.push(d.orgName)}return a}filter(){var l,n;this.form.get("orgName").value!=this.currentHighlight&&this.currentHighlight&&(document.getElementById(this.currentHighlight).parentElement.parentElement.parentElement.parentElement.style.backgroundColor=""),document.getElementById(this.form.get("orgName").value+"_"+this.uuid)?(this.currentHighlight=this.form.get("orgName").value+"_"+this.uuid,document.getElementById(this.form.get("orgName").value+"_"+this.uuid).parentElement.parentElement.parentElement.parentElement.style.backgroundColor="pink",null===(l=document.getElementById(this.form.get("orgName").value+"_"+this.uuid))||void 0===l||l.focus()):document.getElementById(this.currentHighlight)&&(document.getElementById(this.form.get("orgName").value+"_"+this.uuid).parentElement.parentElement.parentElement.parentElement.style.backgroundColor="",n=document.getElementById(this.currentHighlight).parentElement)}onNodeSelect(l){this._selectedNode.emit(l.data)}_uuid(){var l=Date.now();return"undefined"!=typeof performance&&"function"==typeof performance.now&&(l+=performance.now()),"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g,function(n){var a=(l+16*Math.random())%16|0;return l=Math.floor(l/16),("x"===n?a:3&a|8).toString(16)})}}return c.\u0275fac=function(l){return new(l||c)(t.Y36(e.qu),t.Y36(E.g),t.Y36(L.S),t.Y36(L.E7))},c.\u0275cmp=t.Xpm({type:c,selectors:[["app-organization"]],inputs:{_orgList:["orgList","_orgList"],showCreateBtn:"showCreateBtn",showFooterBtn:"showFooterBtn"},outputs:{doCreate:"doCreate",_selectedNode:"selectedNode"},decls:12,vars:9,consts:[[3,"formGroup","ngSubmit"],[1,"form-group","row"],[1,"col-12"],["formControlName","orgName",1,"ui-fluid",3,"suggestions","placeholder","completeMethod"],["pButton","","type","submit","icon","pi pi-search",1,"ml-1","tpi-primary"],["type","button","class","btn tpi-btn tpi-second ml-2 float-right",3,"click",4,"ngIf"],[1,"my-0","mb-2"],[1,"w-100",2,"overflow","auto","height","82%"],["layout","horizontal","selectionMode","single",3,"value","selection","selectionChange","onNodeSelect"],["pTemplate","default"],[4,"ngIf"],["type","button",1,"btn","tpi-btn","tpi-second","ml-2","float-right",3,"click"],[2,"margin","15px"],["tabindex","0",3,"id"],[1,"w-100",2,"justify-content","center","margin-top","10px","text-align","center"],["type","button",1,"btn","tpi-btn","tpi-primary","mr-2",3,"click"],["type","button",1,"btn","tpi-btn","tpi-cancel","mr-2",3,"click"]],template:function(l,n){1&l&&(t.TgZ(0,"form",0),t.NdJ("ngSubmit",function(){return n.filter()}),t.TgZ(1,"div",1)(2,"div",2)(3,"p-autoComplete",3),t.NdJ("completeMethod",function(r){return n.autoComplete(r)}),t.ALo(4,"translate"),t.qZA(),t._UZ(5,"button",4),t.YNc(6,O,3,3,"button",5),t.qZA()()(),t._UZ(7,"hr",6),t.TgZ(8,"div",7)(9,"p-tree",8),t.NdJ("selectionChange",function(r){return n.selectedOrgNode=r})("onNodeSelect",function(){return n.onNodeSelect(n.selectedOrgNode)}),t.YNc(10,T,3,2,"ng-template",9),t.qZA(),t.YNc(11,A,8,6,"p-footer",10),t.qZA()),2&l&&(t.Q6J("formGroup",n.form),t.xp6(3),t.s9C("placeholder",t.lcZ(4,7,"org_name")),t.Q6J("suggestions",n.orgNameSuggestions),t.xp6(3),t.Q6J("ngIf",n.showCreateBtn),t.xp6(3),t.Q6J("value",n.orgs)("selection",n.selectedOrgNode),t.xp6(2),t.Q6J("ngIf",n.showFooterBtn))},directives:[e._Y,e.JL,e.sg,x.Qc,e.JJ,e.u,v.Hq,M.O5,C.mp,f.jx,f.$_],pipes:[y.X$],styles:["body .ui-autocomplete .ui-autocomplete-input{height:calc(2.25rem + 2px);padding:.375rem .75rem;border:1px solid #ced4da;border-radius:.25rem;transition:border-color .15s ease-in-out,box-shadow .15s ease-in-out}body .ui-inputtext:enabled:focus:not(.ui-state-error){color:#495057;background-color:#fff;border-color:#80bdff;outline:0;box-shadow:0 0 0 .2rem #007bff40}body .ui-corner-all{border-radius:.25rem}.ui-tree.ui-tree-horizontal .ui-treenode-connector-table{width:2px}body .ui-tree.ui-tree-horizontal .ui-treenode .ui-treenode-content.ui-state-highlight{background-color:#f49e00!important}.ui-treenode-content.ui-state-default.ui-corner-all.ui-treenode-selectable.ui-cursor.ui-cursor{background-color:#e9ecef;pointer-events:none}.p-tree.p-tree-horizontal .p-treenode .p-treenode-content:hover{background:#FFECE5!important;color:#666!important}.p-tree.p-tree-horizontal .p-treenode .p-treenode-content.p-highlight{color:#fff!important;background:#FF6E38!important}\n"],encapsulation:2}),c})()},14479:(N,b,s)=>{s.d(b,{_:()=>m});var t=s(15861),e=s(87587),E=s(45682),L=s(1955),x=s(45330),v=s(93075),M=s(40845),C=s(69808),f=s(23099),y=s(59783),O=s(51062);function T(o,p){1&o&&e._UZ(0,"col"),2&o&&e.Udp("width",p.$implicit.width)}function A(o,p){if(1&o&&(e.TgZ(0,"colgroup"),e._UZ(1,"col",15),e.YNc(2,T,1,2,"col",16),e.qZA()),2&o){const i=p.$implicit;e.xp6(2),e.Q6J("ngForOf",i)}}function D(o,p){if(1&o&&(e.TgZ(0,"th",19),e._uU(1),e.qZA()),2&o){const i=p.$implicit;e.Udp("width",i.width),e.xp6(1),e.hij(" ",i.header," ")}}function c(o,p){if(1&o&&(e.TgZ(0,"tr")(1,"th",17),e._UZ(2,"p-tableHeaderCheckbox"),e.qZA(),e.YNc(3,D,2,3,"th",18),e.qZA()),2&o){const i=p.$implicit;e.xp6(3),e.Q6J("ngForOf",i)}}function g(o,p){if(1&o&&(e.TgZ(0,"td")(1,"span"),e._uU(2),e.qZA()()),2&o){const i=p.$implicit,_=e.oxw().$implicit;e.Udp("width",i.width),e.xp6(2),e.hij(" ",_[i.field]," ")}}function l(o,p){if(1&o&&(e.TgZ(0,"tr")(1,"td"),e._UZ(2,"p-tableCheckbox",20),e.qZA(),e.YNc(3,g,3,3,"td",16),e.qZA()),2&o){const i=p.$implicit,_=p.columns;e.xp6(2),e.Q6J("value",i),e.xp6(1),e.Q6J("ngForOf",_)}}function n(o,p){if(1&o){const i=e.EpF();e.TgZ(0,"tr")(1,"td")(2,"span"),e._uU(3),e.ALo(4,"translate"),e.qZA(),e.TgZ(5,"button",22),e.NdJ("click",function(){return e.CHM(i),e.oxw(3).moreData()}),e._uU(6),e.ALo(7,"translate"),e._UZ(8,"i",23),e.qZA()()()}if(2&o){const i=e.oxw().$implicit,_=e.oxw(2);e.xp6(1),e.uIk("colspan",i.length+1),e.xp6(2),e.AsE("",e.lcZ(4,4,"row_count"),": ",_.rowcount,""),e.xp6(3),e.hij(" ",e.lcZ(7,6,"button.more")," ")}}function a(o,p){if(1&o&&e.YNc(0,n,9,8,"tr",21),2&o){const i=e.oxw(2);e.Q6J("ngIf",i.rowcount)}}function r(o,p){if(1&o&&(e.TgZ(0,"tr")(1,"td"),e._uU(2),e.ALo(3,"translate"),e.qZA()()),2&o){const i=p.$implicit;e.xp6(1),e.uIk("colspan",i.length+1),e.xp6(1),e.hij(" ",e.lcZ(3,2,"no_rec")," ")}}const d=function(){return{"word-break":"break-word"}};function h(o,p){if(1&o){const i=e.EpF();e.TgZ(0,"p-table",9),e.NdJ("selectionChange",function(u){return e.CHM(i),e.oxw().selected=u}),e.YNc(1,A,3,1,"ng-template",10),e.YNc(2,c,4,1,"ng-template",11),e.YNc(3,l,4,2,"ng-template",12),e.YNc(4,a,1,1,"ng-template",13),e.YNc(5,r,4,4,"ng-template",14),e.qZA()}if(2&o){const i=e.oxw();e.Akn(e.DdM(5,d)),e.Q6J("columns",i.cols)("value",i.roleInfoList)("selection",i.selected)}}let m=(()=>{class o{constructor(i,_,u){this.tool=i,this.roleService=_,this.ref=u,this.cols=[],this.rowcount=0,this.selected=[],this.keyword="",this.roleInfoList=new Array}ngOnInit(){this.init()}init(){var i=this;return(0,t.Z)(function*(){const u=yield i.tool.getDict(["role_id","role_name","role_alias"]);i.cols=[{field:"roleId",header:u.role_id},{field:"roleName",header:u.role_name},{field:"roleAlias",header:u.role_alias}],i.queryRoleMappingList("init")})()}queryRoleMappingList(i){("search"!==i||this.keyword.trim())&&(this.roleInfoList=[],this.rowcount=this.roleInfoList.length,this.roleService.queryRoleRoleList({keyword:this.keyword}).subscribe(u=>{this.tool.checkDpSuccess(u.ResHeader)&&(this.roleInfoList=u.RespBody.roleRoleMappingList,this.rowcount=this.roleInfoList.length)}))}moreData(){this.roleService.queryRoleRoleList({roleId:this.roleInfoList[this.roleInfoList.length-1].roleId,keyword:this.keyword}).subscribe(_=>{this.tool.checkDpSuccess(_.ResHeader)&&(this.roleInfoList=this.roleInfoList.concat(_.RespBody.roleRoleMappingList),this.rowcount=this.roleInfoList.length)})}chooseRole(){this.ref.close(this.selected)}}return o.\u0275fac=function(i){return new(i||o)(e.Y36(E.g),e.Y36(L.N),e.Y36(x.E7))},o.\u0275cmp=e.Xpm({type:o,selectors:[["app-role-mapping-list-lov"]],inputs:{close:"close"},decls:16,vars:15,consts:[[1,"py-1"],[1,"row"],[1,"col-12","col-sm-12"],[1,"ui-inputgroup"],["type","text",1,"form-control",3,"ngModel","placeholder","ngModelChange","keyup.enter"],["pButton","","type","button","icon","pi pi-search",1,"ml-1","tpi-primary",3,"click"],["styleClass","p-datatable-striped","responsiveLayout","scroll",3,"columns","value","selection","style","selectionChange",4,"ngIf"],[1,"text-center"],["type","button",1,"btn","tpi-btn","tpi-primary","mr-3",3,"disabled","click"],["styleClass","p-datatable-striped","responsiveLayout","scroll",3,"columns","value","selection","selectionChange"],["pTemplate","colgroup"],["pTemplate","header"],["pTemplate","body"],["pTemplate","footer"],["pTemplate","emptymessage"],[2,"width","2.25em"],[3,"width",4,"ngFor","ngForOf"],["scope","col",2,"width","2.25em"],["scope","col",3,"width",4,"ngFor","ngForOf"],["scope","col"],[3,"value"],[4,"ngIf"],["type","button","icon","",1,"btn","tpi-header-return",3,"click"],[1,"fas","fa-angle-double-right",2,"margin-left","5px"]],template:function(i,_){1&i&&(e.TgZ(0,"div",0)(1,"div",1)(2,"div",2)(3,"div",3)(4,"input",4),e.NdJ("ngModelChange",function(I){return _.keyword=I})("keyup.enter",function(){return _.queryRoleMappingList("search")}),e.ALo(5,"translate"),e.ALo(6,"translate"),e.ALo(7,"translate"),e.qZA(),e.TgZ(8,"button",5),e.NdJ("click",function(){return _.queryRoleMappingList("search")}),e.qZA()()()(),e._UZ(9,"br"),e.YNc(10,h,6,6,"p-table",6),e._UZ(11,"br"),e.TgZ(12,"div",7)(13,"button",8),e.NdJ("click",function(){return _.chooseRole()}),e._uU(14),e.ALo(15,"translate"),e.qZA()()()),2&i&&(e.xp6(4),e.cQ8("placeholder","",e.lcZ(5,7,"role_id"),"\u3001",e.lcZ(6,9,"role_name"),"\u3001",e.lcZ(7,11,"role_alias"),""),e.Q6J("ngModel",_.keyword),e.xp6(6),e.Q6J("ngIf",_.cols),e.xp6(3),e.Q6J("disabled",0==_.rowcount),e.xp6(1),e.hij(" ",e.lcZ(15,13,"button.confirm")," "))},directives:[v.Fj,v.JJ,v.On,M.Hq,C.O5,f.iA,y.jx,C.sg,f.Mo,f.UA],pipes:[O.X$],styles:[""]}),o})()}}]);