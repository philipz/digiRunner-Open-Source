"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[7939],{67939:(T,x,c)=>{c.r(x),c.d(x,{Ac0012Module:()=>Re});var r=c(24300),d=c(69808),A=c(15861),m=c(59783),h=c(93075),g=c(57553),v=c(14525),u=c(68306),l=c(63900),a=c(1694),e=c(87587),f=c(99291),b=c(56435),L=c(45682),O=c(94319),R=c(51062),q=c(1955),D=c(63710),H=c(23099),B=c(40845),E=c(4119);function U(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"div",15)(1,"input",16),e.NdJ("change",function(n){const _=e.CHM(t).$implicit;return e.oxw(3).menuChange(n,_.name)}),e.qZA(),e.TgZ(2,"label",17),e._uU(3),e.ALo(4,"transform_menu"),e.qZA()()}if(2&i){const t=p.$implicit;e.xp6(1),e.Q6J("id",t.name)("formControlName",t.name),e.xp6(1),e.Q6J("for",t.name),e.xp6(1),e.AsE(" ",e.lcZ(4,5,t.name)," (",t.name,")")}}function F(i,p){if(1&i&&(e.TgZ(0,"div",13),e.YNc(1,U,5,7,"div",14),e.qZA()),2&i){const t=e.oxw().$implicit;e.Q6J("id","mains"+t.main),e.xp6(1),e.Q6J("ngForOf",t.subs)}}function J(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"ul",6)(1,"li",7)(2,"div",8)(3,"div")(4,"input",9),e.NdJ("change",function(n){const _=e.CHM(t).$implicit;return e.oxw().menuChange(n,_.main)}),e.qZA(),e.TgZ(5,"a",10),e._uU(6),e.ALo(7,"transform_menu"),e._UZ(8,"span",11),e.qZA()()(),e.YNc(9,F,2,2,"div",12),e._UZ(10,"hr"),e.qZA()()}if(2&i){const t=p.$implicit;e.xp6(4),e.Q6J("formControlName",t.main),e.xp6(1),e.Q6J("href","#mains"+t.main,e.LSH),e.xp6(1),e.AsE(" ",e.lcZ(7,5,t.main)," (",t.main,")"),e.xp6(3),e.Q6J("ngIf",t.subs&&t.subs.length)}}function $(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"div",18)(1,"button",19),e.NdJ("click",function(){return e.CHM(t),e.oxw().click()}),e._uU(2),e.ALo(3,"translate"),e.qZA()()}2&i&&(e.xp6(2),e.Oqu(e.lcZ(3,1,"button.update")))}const I=function(i){return{"max-height":i}};let S=(()=>{class i{constructor(t){this.fb=t,this.height="300px",this.disableCheckbox=!1,this.showCheckbox=!0,this.showButton=!0,this.updateHandler=new e.vpe,this.newMenus=[],this.form=this.fb.group([])}ngOnInit(){var t;this.menus&&(this.newMenus=JSON.parse(JSON.stringify(this.menus)),null===(t=this.newMenus)||void 0===t||t.forEach(o=>{var n;this.form.addControl(o.main,new h.NI({value:!1,disabled:this.disableCheckbox})),null===(n=o.subs)||void 0===n||n.forEach(s=>{let _=this.selected.includes(s.name);this.form.addControl(s.name,new h.NI({value:_,disabled:this.disableCheckbox}))})}),Object.keys(this.form.controls).filter(o=>4==o.length).forEach(o=>{let n=this.checkedGroup(o);this.form.controls[o].setValue(n)}))}reset(){Object.keys(this.form.controls).forEach(t=>{this.form.controls[t].setValue(!1)})}menuChange(t,o){if(4==o.length)Object.keys(this.form.controls).filter(n=>{switch(o){default:return n.indexOf(o)>=0;case"AC00":return n.indexOf(o)>=0||n.indexOf("AC10")>=0||n.indexOf("AC12")>=0;case"AC02":return n.indexOf(o)>=0||n.indexOf("AC11")>=0;case"AC03":return n.indexOf(o)>=0||n.indexOf("AC04")>=0||n.indexOf("AC08")>=0;case"AC05":return n.indexOf(o)>=0||n.indexOf("AC07")>=0;case"NP01":return n.indexOf(o)>=0||n.indexOf("NP12")>=0;case"NP03":return n.indexOf(o)>=0||n.indexOf("NP04")>=0}}).forEach(n=>{this.form.controls[n].setValue(t.target.checked)});else switch(o.substr(0,4)){default:this.form.controls[o.substr(0,4)].setValue(this.checkedGroup(o,t.target.value));break;case"AC10":case"AC12":this.form.controls.AC00.setValue(this.checkedGroup(o,t.target.value));break;case"AC11":this.form.controls.AC02.setValue(this.checkedGroup(o,t.target.value));break;case"AC04":case"AC08":this.form.controls.AC03.setValue(this.checkedGroup(o,t.target.value));break;case"AC07":this.form.controls.AC05.setValue(this.checkedGroup(o,t.target.value));break;case"NP12":this.form.controls.NP01.setValue(this.checkedGroup(o,t.target.value));break;case"NP04":this.form.controls.NP03.setValue(this.checkedGroup(o,t.target.value));break;case"LAB0":this.form.controls.LABS.setValue(this.checkedGroup(o,t.target.value))}this.showButton||this.updateHandler.emit(this.form.value)}checkedGroup(t,o){let n=!0;return t.length>4?(o?Object.keys(this.form.controls).filter(s=>{switch(t.substr(0,4)){case"AC00":case"AC10":case"AC12":return s.length>4&&("AC00"==s.substr(0,4)||"AC12"==s.substr(0,4)||"AC10"==s.substr(0,4));case"AC02":case"AC11":return s.length>4&&("AC02"==s.substr(0,4)||"AC11"==s.substr(0,4));case"AC04":case"AC08":return s.length>4&&("AC03"==s.substr(0,4)||"AC04"==s.substr(0,4)||"AC08"==s.substr(0,4));case"AC05":case"AC07":return s.length>4&&("AC05"==s.substr(0,4)||"AC07"==s.substr(0,4));case"NP01":case"NP12":return s.length>4&&("NP01"==s.substr(0,4)||"NP12"==s.substr(0,4));case"NP03":case"NP04":return s.length>4&&("NP03"==s.substr(0,4)||"NP04"==s.substr(0,4));default:return s.length>4&&s.substr(0,4)==t.substr(0,4)}}).forEach(s=>{let _=this.form.controls[s].value;_||(n=_)}):n=o||!1,n):(Object.keys(this.form.controls).filter(s=>{switch(t){default:return s.indexOf(t)>=0&&s!=t;case"AC00":return(s.indexOf(t)>=0||s.indexOf("AC10")>=0||s.indexOf("AC12")>=0)&&s!=t;case"AC02":return(s.indexOf(t)>=0||s.indexOf("AC11")>=0)&&s!=t;case"AC03":return(s.indexOf(t)>=0||s.indexOf("AC04")>=0||s.indexOf("AC08")>=0)&&s!=t;case"AC05":return(s.indexOf(t)>=0||s.indexOf("AC07")>=0)&&s!=t;case"NP01":return(s.indexOf(t)>=0||s.indexOf("NP12")>=0)&&s!=t;case"NP03":return(s.indexOf(t)>=0||s.indexOf("NP04")>=0)&&s!=t}}).forEach(s=>{let _=this.form.controls[s].value;_||(n=_)}),n)}click(){this.updateHandler.emit(this.form.value)}}return i.\u0275fac=function(t){return new(t||i)(e.Y36(h.qu))},i.\u0275cmp=e.Xpm({type:i,selectors:[["app-list-group"]],inputs:{height:"height",disableCheckbox:"disableCheckbox",showCheckbox:"showCheckbox",showButton:"showButton",menus:["menu","menus"],selected:"selected"},outputs:{updateHandler:"updateHandler"},decls:7,vars:6,consts:[[3,"formGroup"],[2,"overflow-y","auto","overflow-x","hidden",3,"ngStyle"],[1,"row",2,"display","block"],[1,"col"],["class","list-group list-group-flush",4,"ngFor","ngForOf"],["class","col text-center",4,"ngIf"],[1,"list-group","list-group-flush"],[1,"list-group-item","px-0"],[1,"checkbox"],["type","checkbox","value","",3,"formControlName","change"],["data-toggle","collapse","role","button","aria-expanded","true",1,"btn","collapsed",3,"href"],[1,"mr-3"],["class","collapse",3,"id",4,"ngIf"],[1,"collapse",3,"id"],["class","form-check","class","list-group-item",4,"ngFor","ngForOf"],[1,"list-group-item"],["type","checkbox",1,"form-check-input",3,"id","formControlName","change"],[1,"form-check-label",2,"margin-left","10px","position","absolute","bottom","18px",3,"for"],[1,"col","text-center"],["type","button",1,"btn","btn-warning",3,"click"]],template:function(t,o){1&t&&(e.TgZ(0,"form",0)(1,"div",1)(2,"div",2)(3,"div",3),e.YNc(4,J,11,7,"ul",4),e.qZA()()(),e.TgZ(5,"div",2),e.YNc(6,$,4,3,"div",5),e.qZA()()),2&t&&(e.Q6J("formGroup",o.form),e.xp6(1),e.Q6J("ngStyle",e.VKq(4,I,o.height)),e.xp6(3),e.Q6J("ngForOf",o.newMenus),e.xp6(2),e.Q6J("ngIf",o.showButton))},directives:[h._Y,h.JL,h.sg,d.PC,d.sg,h.Wl,h.JJ,h.u,d.O5],pipes:[b.W,R.X$],styles:[".btn[_ngcontent-%COMP%]{box-shadow:none!important;outline:0}a[_ngcontent-%COMP%]:link, a[_ngcontent-%COMP%]:visited, .list-group-item[_ngcontent-%COMP%]   label[_ngcontent-%COMP%]{color:#5a5541}a[_ngcontent-%COMP%]:hover, a[_ngcontent-%COMP%]:focus{color:orange}.list-group-item[_ngcontent-%COMP%]{margin-left:2rem;padding-top:10px!important;border:0px;background:#f6f6f6}hr[_ngcontent-%COMP%]{margin-bottom:0;margin-top:0}a[_ngcontent-%COMP%]{width:95%;text-align:left}.list-group-item[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{border:solid #222;border-width:0 1px 1px 0;display:inline;cursor:pointer;padding:3px;position:absolute;right:0}.list-group-item[_ngcontent-%COMP%]   a.btn.collapsed[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{transform:rotate(40deg);-webkit-transform:rotate(40deg);transition:.2s transform ease-in-out}.list-group-item[_ngcontent-%COMP%]   a[_ngcontent-%COMP%]{margin-bottom:15px;padding-left:10px}.list-group-item[_ngcontent-%COMP%]   a.btn[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{transform:rotate(-140deg);-webkit-transform:rotate(-140deg);transition:.2s transform ease-in-out}.list-group-item[_ngcontent-%COMP%]   input[type=checkbox][_ngcontent-%COMP%]{cursor:pointer;-webkit-appearance:none;appearance:none;background:#fff;border-radius:1px;box-sizing:border-box;position:relative;box-sizing:content-box;border:1px solid orange;width:20px;height:20px;transition:all .2s linear}.list-group-item[_ngcontent-%COMP%]   input[type=checkbox][_ngcontent-%COMP%]:checked{background-color:#ea7448}.list-group-item[_ngcontent-%COMP%]   input[type=checkbox][_ngcontent-%COMP%]:focus{outline:0 none;box-shadow:none}.list-group-item[_ngcontent-%COMP%]   input[type=checkbox][_ngcontent-%COMP%]:disabled{border:1px solid rgb(149,149,149)}.list-group-item[_ngcontent-%COMP%]   input[type=checkbox][_ngcontent-%COMP%]:checked:disabled{background-color:#959595}"],changeDetection:0}),i})();var G=c(17773),Q=c(86561),Y=c(19114);const j=["dialog"],W=["listgroup"];function K(i,p){1&i&&e._UZ(0,"col"),2&i&&e.Udp("width",p.$implicit.width)}function V(i,p){if(1&i&&(e.TgZ(0,"colgroup"),e.YNc(1,K,1,2,"col",21),e.qZA(),e.TgZ(2,"colgroup",22),e._uU(3),e.ALo(4,"translate"),e.qZA()),2&i){const t=p.$implicit;e.xp6(1),e.Q6J("ngForOf",t),e.xp6(2),e.Oqu(e.lcZ(4,2,"action"))}}function z(i,p){if(1&i&&(e.TgZ(0,"th",25),e._uU(1),e.qZA()),2&i){const t=p.$implicit;e.xp6(1),e.hij(" ",t.header," ")}}function X(i,p){if(1&i&&(e.TgZ(0,"tr"),e.YNc(1,z,2,1,"th",23),e.TgZ(2,"th",24),e._uU(3),e.ALo(4,"translate"),e.qZA()()),2&i){const t=p.$implicit;e.xp6(1),e.Q6J("ngForOf",t),e.xp6(2),e.Oqu(e.lcZ(4,2,"action"))}}function ee(i,p){if(1&i&&(e.TgZ(0,"td"),e._uU(1),e.qZA()),2&i){const t=p.$implicit,o=e.oxw().$implicit;e.Udp("width",t.width),e.xp6(1),e.hij(" ",o[t.field]," ")}}function te(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"tr"),e.YNc(1,ee,2,3,"td",21),e.TgZ(2,"td",26)(3,"button",27),e.NdJ("click",function(){const s=e.CHM(t).$implicit,_=e.oxw();return _.showDialog(s,_.formOperate.detail)}),e.ALo(4,"translate"),e.qZA(),e.TgZ(5,"button",28),e.NdJ("click",function(){const s=e.CHM(t).$implicit,_=e.oxw();return _.showDialog(s,_.formOperate.update)}),e.ALo(6,"translate"),e.qZA(),e.TgZ(7,"button",29),e.NdJ("click",function(){const s=e.CHM(t).$implicit,_=e.oxw();return _.showDialog(s,_.formOperate.delete)}),e.ALo(8,"translate"),e.qZA()()()}if(2&i){const t=p.columns;e.xp6(1),e.Q6J("ngForOf",t),e.xp6(2),e.Q6J("pTooltip",e.lcZ(4,4,"button.detail")),e.xp6(2),e.Q6J("pTooltip",e.lcZ(6,6,"button.edit")),e.xp6(2),e.Q6J("pTooltip",e.lcZ(8,8,"button.delete"))}}function oe(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"tr")(1,"td",30)(2,"span",31),e._uU(3),e.ALo(4,"translate"),e.qZA(),e.TgZ(5,"button",32),e.NdJ("click",function(){return e.CHM(t),e.oxw(2).moreData()}),e._uU(6),e.ALo(7,"translate"),e._UZ(8,"i",33),e.qZA()()()}if(2&i){const t=e.oxw().$implicit,o=e.oxw();e.xp6(1),e.uIk("colspan",t.length+1),e.xp6(2),e.AsE("",e.lcZ(4,4,"row_count"),": ",o.rowcount,""),e.xp6(3),e.hij("",e.lcZ(7,6,"button.more")," ")}}function ie(i,p){if(1&i&&e.YNc(0,oe,9,8,"tr",15),2&i){const t=e.oxw();e.Q6J("ngIf",t.rowcount)}}function ne(i,p){if(1&i&&(e.TgZ(0,"tr")(1,"td"),e._uU(2),e.ALo(3,"translate"),e.qZA()()),2&i){const t=p.$implicit;e.xp6(1),e.uIk("colspan",t.length+1),e.xp6(1),e.hij(" ",e.lcZ(3,2,"no_rec")," ")}}function se(i,p){if(1&i&&(e.TgZ(0,"div",48)(1,"small",49),e._uU(2),e.ALo(3,"translate"),e.qZA(),e.TgZ(4,"small",49),e._uU(5),e.ALo(6,"translate"),e.qZA(),e.TgZ(7,"small",49),e._uU(8),e.ALo(9,"translate"),e.qZA()()),2&i){const t=e.oxw(2);e.xp6(2),e.Oqu(e.xi3(3,3,t.roleName.errors.stringname,t.roleNameLimitChar)),e.xp6(3),e.Oqu(e.xi3(6,6,t.roleName.errors.maxlength,t.roleNameLimitChar)),e.xp6(3),e.Oqu(e.lcZ(9,9,t.roleName.errors.required))}}function re(i,p){if(1&i&&(e.TgZ(0,"div",48)(1,"small",49),e._uU(2),e.ALo(3,"translate"),e.qZA(),e.TgZ(4,"small",49),e._uU(5),e.ALo(6,"translate"),e.qZA(),e.TgZ(7,"small",49),e._uU(8),e.ALo(9,"translate"),e.qZA()()),2&i){const t=e.oxw(2);e.xp6(2),e.Oqu(e.xi3(3,3,t.roleAlias.errors.stringalias,t.roleAliasLimitChar)),e.xp6(3),e.Oqu(e.xi3(6,6,t.roleAlias.errors.maxlength,t.roleAliasLimitChar)),e.xp6(3),e.Oqu(e.lcZ(9,9,t.roleAlias.errors.required))}}function ae(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"app-list-group",50,51),e.NdJ("updateHandler",function(n){return e.CHM(t),e.oxw(2).update(n)}),e.qZA()}if(2&i){const t=e.oxw(2);e.Q6J("menu",t.menus)("showButton",!1)("selected",t.selected)}}function le(i,p){if(1&i&&(e.TgZ(0,"div",48)(1,"small",49),e._uU(2),e.ALo(3,"translate"),e.qZA()()),2&i){const t=e.oxw(2);e.xp6(2),e.Oqu(e.lcZ(3,1,t.funcCodeList.errors.required))}}const k=function(){return{marginTop:"60px"}};function ce(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"div")(1,"form",2,34),e.NdJ("ngSubmit",function(){e.CHM(t);const n=e.MAs(2);return e.oxw().submitForm_page2(n)}),e.TgZ(3,"div")(4,"div",4)(5,"div",3)(6,"div",35)(7,"label",36),e._uU(8),e.ALo(9,"translate"),e.qZA(),e._UZ(10,"input",37),e.ALo(11,"translate"),e.YNc(12,se,10,11,"div",38),e.qZA(),e.TgZ(13,"div",35)(14,"label",39),e._uU(15),e.ALo(16,"translate"),e.qZA(),e._UZ(17,"input",40),e.ALo(18,"translate"),e.YNc(19,re,10,11,"div",38),e.qZA()(),e.TgZ(20,"div",3)(21,"div",41)(22,"label",42),e._uU(23),e.ALo(24,"translate"),e.qZA(),e.TgZ(25,"div",43),e.YNc(26,ae,2,3,"app-list-group",44),e.qZA(),e.YNc(27,le,4,3,"div",38),e.qZA()(),e.TgZ(28,"div",45)(29,"div",4)(30,"button",46),e._uU(31),e.ALo(32,"translate"),e.qZA(),e.TgZ(33,"button",47),e.NdJ("click",function(){return e.CHM(t),e.oxw().reture_to_page1()}),e._uU(34),e.ALo(35,"translate"),e.qZA()()()()()(),e._UZ(36,"p-toast",18),e.qZA()}if(2&i){const t=e.oxw();e.xp6(1),e.Q6J("formGroup",t.form_page2),e.xp6(7),e.Oqu(e.lcZ(9,15,"role_name")),e.xp6(2),e.s9C("placeholder",e.lcZ(11,17,"role_name")),e.xp6(2),e.Q6J("ngIf",(null==t.roleName?null:t.roleName.invalid)&&((null==t.roleName?null:t.roleName.dirty)||(null==t.roleName?null:t.roleName.touched))),e.xp6(3),e.Oqu(e.lcZ(16,19,"role_desc")),e.xp6(2),e.s9C("placeholder",e.lcZ(18,21,"role_desc")),e.xp6(2),e.Q6J("ngIf",(null==t.roleAlias?null:t.roleAlias.invalid)&&((null==t.roleAlias?null:t.roleAlias.dirty)||(null==t.roleAlias?null:t.roleAlias.touched))),e.xp6(4),e.Oqu(e.lcZ(24,23,"fun_permissions")),e.xp6(3),e.Q6J("ngIf",t.menus),e.xp6(1),e.Q6J("ngIf",(null==t.funcCodeList?null:t.funcCodeList.invalid)&&((null==t.funcCodeList?null:t.funcCodeList.dirty)||(null==t.funcCodeList?null:t.funcCodeList.touched))),e.xp6(3),e.Q6J("disabled",!t.form_page2.valid),e.xp6(1),e.Oqu(e.lcZ(32,25,"button.create")),e.xp6(3),e.Oqu(e.lcZ(35,27,"button.return_to_list")),e.xp6(2),e.Akn(e.DdM(29,k))}}function ue(i,p){if(1&i&&e._UZ(0,"app-list-group",57),2&i){const t=e.oxw(2);e.Q6J("height","10%")("disableCheckbox",!0)("showButton",!1)("menu",t.menus_page3)("selected",t.data_page3.funcCodeList)}}function pe(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"div")(1,"div",4)(2,"div",3)(3,"div",35)(4,"label",52),e._uU(5),e.ALo(6,"translate"),e.qZA(),e._UZ(7,"input",53),e.qZA(),e.TgZ(8,"div",35)(9,"label",54),e._uU(10),e.ALo(11,"translate"),e.qZA(),e._UZ(12,"input",53),e.qZA()(),e.TgZ(13,"div",3)(14,"div",41)(15,"div",43),e.YNc(16,ue,1,5,"app-list-group",55),e.qZA()()(),e.TgZ(17,"div",45)(18,"div",4)(19,"button",56),e.NdJ("click",function(){return e.CHM(t),e.oxw().reture_to_page1()}),e._uU(20),e.ALo(21,"translate"),e.qZA()()()()()}if(2&i){const t=e.oxw();e.xp6(5),e.Oqu(e.lcZ(6,7,"role_name")),e.xp6(2),e.s9C("placeholder",t.roleName_page3),e.xp6(3),e.Oqu(e.lcZ(11,9,"role_desc")),e.xp6(2),e.s9C("placeholder",t.roleAlias_page3),e.xp6(4),e.Q6J("ngIf",t.data_page3),e.xp6(3),e.Q6J("disabled",!t.form.valid),e.xp6(1),e.Oqu(e.lcZ(21,11,"button.return_to_list"))}}function de(i,p){if(1&i&&(e.TgZ(0,"div",48)(1,"small",49),e._uU(2),e.ALo(3,"translate"),e.qZA(),e.TgZ(4,"small",49),e._uU(5),e.ALo(6,"translate"),e.qZA(),e.TgZ(7,"small",49),e._uU(8),e.ALo(9,"translate"),e.qZA()()),2&i){const t=e.oxw(2);e.xp6(2),e.Oqu(e.xi3(3,3,t.newRoleAlias.errors.stringalias,t.newRoleAliasLimitChar)),e.xp6(3),e.Oqu(e.xi3(6,6,t.newRoleAlias.errors.maxlength,t.newRoleAliasLimitChar)),e.xp6(3),e.Oqu(e.lcZ(9,9,t.newRoleAlias.errors.required))}}function he(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"app-list-group",65),e.NdJ("updateHandler",function(n){return e.CHM(t),e.oxw(2).update_page4(n)}),e.qZA()}if(2&i){const t=e.oxw(2);e.Q6J("height","100%")("menu",t.menus_page4)("showButton",!1)("selected",t.data_page4.funcCodeList)}}function _e(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"div")(1,"form",2),e.NdJ("ngSubmit",function(){return e.CHM(t),e.oxw().submitForm_page4()}),e.TgZ(2,"div",4)(3,"div",3)(4,"div",35)(5,"label",58),e._uU(6),e.ALo(7,"translate"),e.qZA(),e._UZ(8,"input",53),e.qZA(),e.TgZ(9,"div",35)(10,"label",59),e._uU(11),e.ALo(12,"translate"),e.qZA(),e._UZ(13,"input",60),e.ALo(14,"translate"),e.YNc(15,de,10,11,"div",38),e.qZA()(),e.TgZ(16,"div",3)(17,"div",41)(18,"label",61),e._uU(19),e.ALo(20,"translate"),e.qZA(),e.TgZ(21,"div",43),e.YNc(22,he,1,4,"app-list-group",62),e.qZA()()(),e.TgZ(23,"div",45)(24,"div",4)(25,"button",63),e._uU(26),e.ALo(27,"translate"),e.qZA(),e.TgZ(28,"button",64),e.NdJ("click",function(){return e.CHM(t),e.oxw().reture_to_page1()}),e._uU(29),e.ALo(30,"translate"),e.qZA()()()()()()}if(2&i){const t=e.oxw();e.xp6(1),e.Q6J("formGroup",t.form_page4),e.xp6(5),e.Oqu(e.lcZ(7,11,"role_name")),e.xp6(2),e.s9C("placeholder",t.roleName_page4),e.xp6(3),e.Oqu(e.lcZ(12,13,"role_desc")),e.xp6(2),e.s9C("placeholder",e.lcZ(14,15,"role_desc")),e.xp6(2),e.Q6J("ngIf",t.newRoleAlias.invalid&&(t.newRoleAlias.dirty||t.newRoleAlias.touched)),e.xp6(4),e.Oqu(e.lcZ(20,17,"fun_permissions")),e.xp6(3),e.Q6J("ngIf",t.data_page4),e.xp6(3),e.Q6J("disabled",!t.form_page4.valid),e.xp6(1),e.Oqu(e.lcZ(27,19,"button.update")),e.xp6(3),e.Oqu(e.lcZ(30,21,"button.return_to_list"))}}const me=function(){return{backgroundColor:"var(--red-300)","border-color":"var(--red-300)"}};function ge(i,p){if(1&i){const t=e.EpF();e.TgZ(0,"div",66)(1,"button",67),e.NdJ("click",function(){return e.CHM(t),e.oxw(),e.MAs(26).accept()}),e.ALo(2,"translate"),e.qZA(),e.TgZ(3,"button",68),e.NdJ("click",function(){return e.CHM(t),e.oxw(),e.MAs(26).reject()}),e.ALo(4,"translate"),e.qZA()()}2&i&&(e.xp6(1),e.s9C("label",e.lcZ(2,3,"button.confirm")),e.Q6J("ngStyle",e.DdM(7,me)),e.xp6(2),e.s9C("label",e.lcZ(4,5,"button.cancel")))}const fe=function(){return{"word-break":"break-word"}},be=function(){return{width:"50vw"}};let Ae=(()=>{class i extends v.H{constructor(t,o,n,s,_,y,P,M,Z){super(t,o),this.fb=n,this.messageService=s,this.tool=_,this.siderbar=y,this.translate=P,this.roleService=M,this.confirmationService=Z,this.formOperate=g.mi,this.cols=[],this.rowcount=0,this.data=[],this.roleDetailList=new Array,this.dialogTitle="",this.pageNum=1,this.selected=[],this.roleNameLimitChar={value:30},this.roleAliasLimitChar={value:30},this.selectedCities=[],this.selecteds=[],this.funcs=[],this.menus_page3=[],this.roleName_page3="",this.roleAlias_page3="",this.selectedFuncList=[],this.newRoleAliasLimitChar={value:30},this.menus_page4=[],this.roleName_page4="",this.isDefault=!0,this.display=!1,this.form=this.fb.group({keyword:new h.NI("")}),this.form_page2=this.fb.group({roleName:"",roleAlias:"",funcCodeList:""}),this.form_page4=this.fb.group({newRoleAlias:""}),this.translate.get(["role_name","role_desc"]).subscribe(N=>{this.cols=[{field:"roleName",header:N.role_name},{field:"roleAlias",header:N.role_desc}]})}ngOnInit(){this.roleDetailList=[],this.rowcount=this.roleDetailList.length;let t={keyword:this.form.get("keyword").value,funcFlag:!0,authorityFlag:!1};this.roleService.queryTRoleList_v3_ignore1298(t).subscribe(o=>{this.tool.checkDpSuccess(o.ResHeader)&&(this.roleDetailList=o.RespBody.roleDetailList,this.rowcount=this.roleDetailList.length)})}create(){var t=this;return(0,A.Z)(function*(){const n=yield t.tool.getDict(["dialog.create","message.role","message.create","message.success"]);t.form_page2=t.fb.group({roleName:new h.NI("",[a.np(),a._X(t.roleNameLimitChar.value)]),roleAlias:new h.NI("",[a.np(),a.Ad(t.roleAliasLimitChar.value)]),funcCodeList:new h.NI([],[a.np()])}),t.selected=[];let s=t.tool.getFuncList(),_=t.siderbar.transform(s);t.menus=_,t.title=`${t.title} > ${n["dialog.create"]}`,t.pageNum=2})()}submitForm(){this.roleDetailList=[],this.rowcount=this.roleDetailList.length;let t={keyword:this.form.get("keyword").value,funcFlag:!0,authorityFlag:!1};this.roleService.queryTRoleList_v3(t).subscribe(o=>{this.tool.checkDpSuccess(o.ResHeader)&&(this.roleDetailList=o.RespBody.roleDetailList,this.rowcount=this.roleDetailList.length)})}moreData(){let t={roleId:this.roleDetailList[this.roleDetailList.length-1].roleID,keyword:this.form.get("keyword").value,funcFlag:!0,authorityFlag:!1};this.roleService.queryTRoleList_v3(t).subscribe(o=>{this.tool.checkDpSuccess(o.ResHeader)&&(this.roleDetailList=this.roleDetailList.concat(o.RespBody.roleDetailList),this.rowcount=this.roleDetailList.length)})}showDialog(t,o){return this.translate.get(["fun_list","dialog.edit","role_detail","message.update","cfm_del","roles_name","role_desc","message.success"]).pipe((0,l.w)(s=>this.openDialog$(t,o,s))).subscribe(),!1}openDialog$(t,o,n){return u.y.create(s=>{switch(o){case g.mi.detail:this.translate.get(["role_detail"]).subscribe(C=>{this.data_page3=t,this.roleName_page3=t.roleName,this.roleAlias_page3=t.roleAlias;let N=this.tool.getFuncList(),ye=this.siderbar.transform(N);this.menus_page3=ye,this.title=`${this.title} > ${C.role_detail}`,this.pageNum=3});break;case g.mi.update:const y=["message.update"];this.data_page4=t,this.roleName_page4=t.roleName;let P=this.tool.getFuncList(),M=this.siderbar.transform(P);this.menus_page4=M;let Z={};this.data_page4.funcCodeList.map(C=>{Z[C]=!0}),this.form_page4=this.fb.group({newRoleAlias:new h.NI(this.data_page4.roleAlias,[a.np(),a.Ad(this.newRoleAliasLimitChar.value)]),funcCodeList:new h.NI([],a.np())}),this.update_page4(Z),this.translate.get(y).subscribe(C=>{this.title=`${this.title} > ${C["message.update"]}`,this.pageNum=4});break;case g.mi.delete:this.currentRole={roleId:t.roleID,roleName:t.roleName},this.confirmationService.confirm({header:n.cfm_del,message:`${n.roles_name} : ${this.currentRole.roleName} , ${n.role_desc} : ${t.roleAlias}`,accept:()=>{this.onDeleteConfirm()}})}})}onDeleteConfirm(){this.translate.get(["message.delete","message.role","message.success"]).subscribe(o=>{this.roleService.deleteTRole(this.currentRole).subscribe(n=>{this.tool.checkDpSuccess(n.ResHeader)&&(this.messageService.clear(),this.messageService.add({severity:"success",summary:`${o["message.delete"]} ${o["message.role"]}`,detail:`${o["message.delete"]} ${o["message.success"]}!`}),this.submitForm())})})}onReject(){this.messageService.clear()}submitForm_page2(t){this.translate.get(["message.create","message.role","message.success"]).subscribe(n=>{let s={roleName:this.form_page2.get("roleName").value,roleAlias:this.form_page2.get("roleAlias").value,funcCodeList:this.form_page2.get("funcCodeList").value};this.roleService.addTRole(s).subscribe(_=>{_&&this.tool.checkDpSuccess(_.ResHeader)&&(this.messageService.add({severity:"success",summary:`${n["message.create"]} ${n["message.role"]}`,detail:`${n["message.create"]} ${n["message.success"]}!`}),this.submitForm(),this.title=`${this.title.split(">")[0]}`,this.pageNum=1)})})}update(t){this.funcCodeList.markAsTouched(),this.selected=Object.keys(t).filter(o=>o.length>4&&1==t[o]),(!this.selected||0==this.selected.length)&&(this.selected=[],this.funcCodeList.setErrors({error:"required"})),this.funcCodeList.setValue(this.selected)}reture_to_page1(){this.title=`${this.title.split(">")[0]}`,this.pageNum=1}update_page4(t){var o;this.selectedFuncList=Object.keys(t).filter(n=>n.length>4&&1==t[n]).map(n=>n),null===(o=this.form_page4.get("funcCodeList"))||void 0===o||o.setValue(this.selectedFuncList)}submitForm_page4(){var t=this;return(0,A.Z)(function*(){const n=yield t.tool.getDict(["message.update","role_info","message.success"]);t.roleService.updateTRoleFunc({roleId:t.data_page4.roleID,roleName:t.data_page4.roleName,newRoleAlias:t.newRoleAlias.value,newFuncCodeList:t.selectedFuncList}).subscribe(_=>{_&&t.tool.checkDpSuccess(_.ResHeader)&&(t.messageService.add({severity:"success",summary:`${n["message.update"]} ${n.role_info}`,detail:`${n["message.update"]} ${n["message.success"]}!`}),t.title=`${t.title.split(">")[0]}`,t.submitForm(),t.pageNum=1)})})()}headerReturn(){this.reture_to_page1()}get roleName(){return this.form_page2.get("roleName")}get roleAlias(){return this.form_page2.get("roleAlias")}get funcCodeList(){return this.form_page2.get("funcCodeList")}get newRoleAlias(){return this.form_page4.get("newRoleAlias")}}return i.\u0275fac=function(t){return new(t||i)(e.Y36(f.gz),e.Y36(b.W),e.Y36(h.qu),e.Y36(m.ez),e.Y36(L.g),e.Y36(O.P),e.Y36(R.sK),e.Y36(q.N),e.Y36(m.YP))},i.\u0275cmp=e.Xpm({type:i,selectors:[["app-ac0012"]],viewQuery:function(t,o){if(1&t&&(e.Gf(j,5),e.Gf(W,5)),2&t){let n;e.iGM(n=e.CRH())&&(o._dialog=n.first),e.iGM(n=e.CRH())&&(o.listGroup=n.first)}},features:[e._Bn([m.ez,m.YP]),e.qOj],decls:28,vars:28,consts:[[3,"title","isDefault","headerReturn"],[3,"hidden"],[3,"formGroup","ngSubmit"],[1,"form-group","row"],[1,"col-12"],[1,"p-input-icon-right",2,"width","40vw"],[1,"pi","pi-search","tpi-i-search",3,"click"],["type","search","id","keyword","formControlName","keyword",1,"form-control","tpi-i-input",3,"placeholder"],["type","button",1,"btn","tpi-btn","tpi-second","float-right",3,"click"],["styleClass","p-datatable-striped","responsiveLayout","scroll",3,"columns","value"],["pTemplate","colgroup"],["pTemplate","header"],["pTemplate","body"],["pTemplate","footer"],["pTemplate","emptymessage"],[4,"ngIf"],[3,"title"],["dialog",""],["position","top-left"],["icon","pi pi-exclamation-triangle","styleClass","cHeader cContent cIcon"],["cd",""],[3,"width",4,"ngFor","ngForOf"],[2,"width","150px"],["scope","col",4,"ngFor","ngForOf"],["scope","col",2,"width","150px"],["scope","col"],[2,"text-align","center","width","150px"],["pButton","","pRipple","","type","button","icon","pi pi-eye","tooltipPosition","top",1,"p-button-rounded","p-button-text","p-button-plain",3,"pTooltip","click"],["pButton","","pRipple","","type","button","icon","fa fa-edit","tooltipPosition","top",1,"p-button-rounded","p-button-text","p-button-plain",3,"pTooltip","click"],["pButton","","pRipple","","type","button","icon","fa fa-trash-alt","tooltipPosition","top",1,"p-button-rounded","p-button-text","p-button-plain",3,"pTooltip","click"],[2,"color","#b7b7b7"],[2,"vertical-align","middle"],["type","button",1,"btn","tpi-header-return",3,"click"],[1,"fas","fa-angle-double-right",2,"margin-left","5px"],["formDirective","ngForm"],[1,"col-12","col-xl-6","col-lg-6"],["for","roleName",1,"control-label","required"],["type","text","id","roleName","formControlName","roleName",1,"form-control",3,"placeholder"],["class","text-danger",4,"ngIf"],["for","roleAlias",1,"control-label","required"],["type","text","id","roleAlias","formControlName","roleAlias",1,"form-control",3,"placeholder"],[1,"col-12","col-xl-12","col-lg-12"],[1,"required","control-label"],[1,"list-group"],["height","540px","style","width:99%;display:block;",3,"menu","showButton","selected","updateHandler",4,"ngIf"],[1,"form-group","row","text-center"],["type","submit",1,"btn","tpi-btn","tpi-second","mr-3",3,"disabled"],["type","button",1,"btn","tpi-btn","tpi-primary",3,"click"],[1,"text-danger"],[1,"form-text"],["height","540px",2,"width","99%","display","block",3,"menu","showButton","selected","updateHandler"],["listgroup",""],["for","roleName",1,"control-label"],["type","text","readonly","",1,"form-control",3,"placeholder"],["for","roleAlias",1,"control-label"],["style","width:99%;display:block;",3,"height","disableCheckbox","showButton","menu","selected",4,"ngIf"],["type","button",1,"btn","tpi-btn","tpi-primary","mr-3",3,"disabled","click"],[2,"width","99%","display","block",3,"height","disableCheckbox","showButton","menu","selected"],["for","roleName_page4",1,"control-label"],["for","newRoleAlias",1,"control-label","required"],["type","text","id","newRoleAlias","formControlName","newRoleAlias",1,"form-control",3,"placeholder"],[1,"control-label","required"],["style","width:99%;display:block;",3,"height","menu","showButton","selected","updateHandler",4,"ngIf"],["type","submit",1,"btn","tpi-btn","tpi-primary","mr-3",3,"disabled"],["type","button",1,"btn","tpi-btn","tpi-primary","mr-3",3,"click"],[2,"width","99%","display","block",3,"height","menu","showButton","selected","updateHandler"],[1,"row",2,"justify-content","center"],["type","button","pButton","","icon","pi pi-check",3,"ngStyle","label","click"],["type","button","pButton","","icon","pi pi-times",1,"p-button-secondary",3,"label","click"]],template:function(t,o){1&t&&(e.TgZ(0,"app-container",0),e.NdJ("headerReturn",function(){return o.headerReturn()}),e.TgZ(1,"div",1)(2,"form",2),e.NdJ("ngSubmit",function(){return o.submitForm()}),e.TgZ(3,"div",3)(4,"div",4)(5,"span",5)(6,"i",6),e.NdJ("click",function(){return o.submitForm()}),e.qZA(),e._UZ(7,"input",7),e.ALo(8,"translate"),e.ALo(9,"translate"),e.qZA(),e.TgZ(10,"button",8),e.NdJ("click",function(){return o.create()}),e._uU(11),e.ALo(12,"translate"),e.qZA()()()(),e.TgZ(13,"p-table",9),e.YNc(14,V,5,4,"ng-template",10),e.YNc(15,X,5,4,"ng-template",11),e.YNc(16,te,9,10,"ng-template",12),e.YNc(17,ie,1,1,"ng-template",13),e.YNc(18,ne,4,4,"ng-template",14),e.qZA()(),e.YNc(19,ce,37,30,"div",15),e.YNc(20,pe,22,13,"div",15),e.YNc(21,_e,31,23,"div",15),e.qZA(),e._UZ(22,"app-dialog",16,17)(24,"p-toast",18),e.TgZ(25,"p-confirmDialog",19,20),e.YNc(27,ge,5,8,"ng-template",13),e.qZA()),2&t&&(e.Q6J("title",o.title)("isDefault",1==o.pageNum),e.xp6(1),e.Q6J("hidden",1!=o.pageNum),e.xp6(1),e.Q6J("formGroup",o.form),e.xp6(5),e.hYB("placeholder","",e.lcZ(8,19,"role_name"),"\u3001",e.lcZ(9,21,"role_desc"),""),e.xp6(4),e.Oqu(e.lcZ(12,23,"button.create")),e.xp6(2),e.Akn(e.DdM(25,fe)),e.Q6J("columns",o.cols)("value",o.roleDetailList),e.xp6(6),e.Q6J("ngIf",2==o.pageNum),e.xp6(1),e.Q6J("ngIf",3==o.pageNum),e.xp6(1),e.Q6J("ngIf",4==o.pageNum),e.xp6(1),e.Q6J("title",o.dialogTitle),e.xp6(2),e.Akn(e.DdM(26,k)),e.xp6(1),e.Akn(e.DdM(27,be)))},directives:[D.e,h._Y,h.JL,h.sg,h.Fj,h.JJ,h.u,H.iA,m.jx,d.sg,B.Hq,E.u,d.O5,S,G.FN,Q.a,Y.Q,d.PC],pipes:[R.X$],styles:[".list-group[_ngcontent-%COMP%]{display:block;border:1px solid #ddd;border-radius:5px}"]}),i})();var w=c(86347);const xe=[{path:"",component:Ae,canActivate:[w.u6]}];let ve=(()=>{class i{}return i.\u0275fac=function(t){return new(t||i)},i.\u0275mod=e.oAB({type:i}),i.\u0275inj=e.cJS({imports:[[f.Bz.forChild(xe)],f.Bz]}),i})();var Ce=c(24783),Te=c(88893);let Re=(()=>{class i{}return i.\u0275fac=function(t){return new(t||i)},i.\u0275mod=e.oAB({type:i}),i.\u0275inj=e.cJS({providers:[r.K,w.u6],imports:[[d.ez,ve,Te.W,Ce.m,h.UX,h.u5]]}),i})()},63710:(T,x,c)=>{c.d(x,{e:()=>u});var r=c(87587),d=c(69808),A=c(51062);function m(l,a){if(1&l&&(r.TgZ(0,"div",9)(1,"h3",10),r._uU(2),r.ALo(3,"translate"),r.qZA()()),2&l){const e=r.oxw();r.xp6(2),r.Oqu(r.lcZ(3,1,e.title))}}function h(l,a){if(1&l){const e=r.EpF();r.TgZ(0,"div",11)(1,"button",12),r.NdJ("click",function(){return r.CHM(e),r.oxw().return()}),r._UZ(2,"i",13),r._uU(3),r.ALo(4,"translate"),r.qZA(),r.TgZ(5,"span",14),r._uU(6),r.qZA(),r.TgZ(7,"span",15),r._uU(8),r.qZA()()}if(2&l){const e=r.oxw();r.xp6(3),r.hij(" ",r.lcZ(4,3,"button.return_to_list")," "),r.xp6(3),r.hij("",e.getHead()," /"),r.xp6(2),r.Oqu(e.getTail())}}const g=[[["","center-view","center"]],"*"],v=["[center-view=center]","*"];let u=(()=>{class l{constructor(){this.title="",this.isDefault=!0,this.headerReturn=new r.vpe}ngOnInit(){}return(){this.headerReturn.emit(null)}getHead(){const e=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return e.pop(),e.join(" / ")}getTail(){const e=this.title.indexOf(">")>-1?this.title.split(">"):[this.title];return e[e.length-1]}}return l.\u0275fac=function(e){return new(e||l)},l.\u0275cmp=r.Xpm({type:l,selectors:[["app-container"]],inputs:{title:"title",isDefault:"isDefault"},outputs:{headerReturn:"headerReturn"},ngContentSelectors:v,decls:11,vars:2,consts:[[1,"h-100"],[1,"container-fluid","h-100",2,"padding-left","10px","margin-top","10px"],[1,"row","h-100","position-relative"],[1,"col","pb-5"],[1,"card-title","row"],["class","col-12 col-md-12",4,"ngIf"],["class","col-12 col-md-12","style","text-align: right;",4,"ngIf"],[1,"col","d-flex","justify-content-center"],[1,"my-0","mb-2"],[1,"col-12","col-md-12"],["id","content",1,"bd-title","mb-0"],[1,"col-12","col-md-12",2,"text-align","right"],["type","button","icon","",1,"btn","float-left","tpi-header-return",3,"click"],[1,"fas","fa-arrow-left",2,"margin-right","5px"],[1,"bd-title","mb-0",2,"color","#666464"],[1,"bd-title","mb-0",2,"color","#FF6E38","font-weight","bold"]],template:function(e,f){1&e&&(r.F$t(g),r.TgZ(0,"div",0)(1,"div",1)(2,"div",2)(3,"div",3)(4,"div",4),r.YNc(5,m,4,3,"div",5),r.YNc(6,h,9,5,"div",6),r.TgZ(7,"div",7),r.Hsn(8),r.qZA()(),r._UZ(9,"hr",8),r.Hsn(10,1),r.qZA()()()()),2&e&&(r.xp6(5),r.Q6J("ngIf",f.isDefault),r.xp6(1),r.Q6J("ngIf",!f.isDefault))},directives:[d.O5],pipes:[A.X$],styles:[".card.card-body[_ngcontent-%COMP%]   h3[_ngcontent-%COMP%]{font-size:20px;font-weight:400;color:#5a5541}"]}),l})()},86561:(T,x,c)=>{c.d(x,{a:()=>v});var r=c(87587),d=c(3937),A=c(15315);const m=["content"];function h(u,l){}const g=function(){return{height:"70vh"}};let v=(()=>{class u{constructor(a){this.ngxService=a,this.visible=!1,this.title="",this.closable=!0,this.width=300}open(a,e){this.contentRef.clear(),this.componentRef=this.contentRef.createComponent(a),this.componentRef.instance.data=e,this.componentRef.instance.close=this.onHide.bind(this),this.visible=!0}onHide(a){let e=this.componentRef.instance.data;this.componentRef.destroy(),this.visible=!1,a?a.subscribe(f=>{e&&e.afterCloseCallback&&e.afterCloseCallback(f),this.ngxService.stop()}):e&&e.afterCloseCallback&&(e.afterCloseCallback(),this.ngxService.stop())}setWidth(){return this.width+"px"}}return u.\u0275fac=function(a){return new(a||u)(r.Y36(d.LA))},u.\u0275cmp=r.Xpm({type:u,selectors:[["app-dialog"]],viewQuery:function(a,e){if(1&a&&r.Gf(m,7,r.s_b),2&a){let f;r.iGM(f=r.CRH())&&(e.contentRef=f.first)}},inputs:{visible:"visible",title:"title",closable:"closable",contentStyle:"contentStyle",width:"width"},decls:3,vars:10,consts:[["modal","true",3,"contentStyle","dismissableMask","header","visible","maximizable","draggable","closable","visibleChange","onHide"],["content",""]],template:function(a,e){1&a&&(r.TgZ(0,"p-dialog",0),r.NdJ("visibleChange",function(b){return e.visible=b})("onHide",function(){return e.onHide()}),r.YNc(1,h,0,0,"ng-template",null,1,r.W1O),r.qZA()),2&a&&(r.Akn(r.DdM(9,g)),r.Q6J("contentStyle",e.contentStyle)("dismissableMask",!0)("header",e.title)("visible",e.visible)("maximizable",!1)("draggable",!1)("closable",e.closable))},directives:[A.V],styles:[""]}),u})()},92718:(T,x,c)=>{c.d(x,{u:()=>a});var r=c(54004),d=c(70262),A=c(62843),m=c(87587),h=c(45682),g=c(89709),v=c(99291),u=c(3937),l=c(60991);let a=(()=>{class e{constructor(b,L,O,R,q){this.toolService=b,this.tokenService=L,this.router=O,this.ngxService=R,this.logoutService=q}canActivate(){return this.ngxService.stopAll(),!this.toolService.isTokenExpired()||this.toolService.refreshToken().pipe((0,r.U)(b=>!!b.access_token),(0,d.K)(this.handleError.bind(this)))}handleError(b){return setTimeout(()=>this.logoutService.logout()),(0,A._)(()=>b)}}return e.\u0275fac=function(b){return new(b||e)(m.LFG(h.g),m.LFG(g.B),m.LFG(v.F0),m.LFG(u.LA),m.LFG(l.P))},e.\u0275prov=m.Yz7({token:e,factory:e.\u0275fac}),e})()},86347:(T,x,c)=>{c.d(x,{DL:()=>r.D,u6:()=>A.u});var r=c(81233),A=(c(45240),c(92718));c(45682)},1955:(T,x,c)=>{c.d(x,{N:()=>h});var r=c(92340),d=c(57553),A=c(87587),m=c(96614);let h=(()=>{class g{constructor(u){this.api=u,this.api.baseUrl=r.N.dpPath}get basePath(){return r.N.isv4?"dgrv4/11":"tsmpdpaa/11"}addTRole(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.addTRole),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0011`,l)}updateTRoleFunc(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.updateTRoleFunc),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0013`,l)}deleteTRole(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.deleteTRole),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0014`,l)}addTRoleRoleMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.addTRoleRoleMap),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0016`,l)}deleteTRoleRoleMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.deleteTRoleRoleMap),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0017`,l)}updateTRoleRoleMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.updateTRoleRoleMap),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0018`,l)}queryTRoleList_v3_ignore1298(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryTRoleList_v3),ReqBody:u};return this.api.excuteNpPost_ignore1298(`${this.basePath}/AA0020`,l)}queryTRoleList_v3(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryTRoleList_v3),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0020`,l)}queryTRoleRoleMapDetail(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryTRoleRoleMapDetail),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0021`,l)}queryTRoleRoleMap_ignore1298(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryTRoleList_v3),ReqBody:u};return this.api.excuteNpPost_ignore1298(`${this.basePath}/AA0022`,l)}queryTRoleRoleMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryTRoleList_v3),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0022`,l)}queryRoleRoleList(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryRoleRoleList),ReqBody:u};return this.api.npPost(`${this.basePath}/AA0023`,l)}createRTMap_before(){let u={ReqHeader:this.api.getReqHeader(d.Nx.createRTMap),ReqBody:{}};return this.api.npPost(`${this.basePath}/DPB0110?before`,u)}createRTMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.createRTMap),ReqBody:u};return this.api.npPost(`${this.basePath}/DPB0110`,l)}queryRTMapList_ignore1298(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryRTMapList),ReqBody:u};return this.api.excuteNpPost_ignore1298(`${this.basePath}/DPB0111`,l)}queryRTMapList(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryRTMapList),ReqBody:u};return this.api.npPost(`${this.basePath}/DPB0111`,l)}queryRTMapByPk(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryRTMapByPk),ReqBody:u};return this.api.npPost(`${this.basePath}/DPB0112`,l)}updateRTMap_before(){let u={ReqHeader:this.api.getReqHeader(d.Nx.updateRTMap),ReqBody:{}};return this.api.npPost(`${this.basePath}/DPB0113?before`,u)}updateRTMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.updateRTMap),ReqBody:u};return this.api.npPost(`${this.basePath}/DPB0113`,l)}deleteRTMap(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.deleteRTMap),ReqBody:u};return this.api.npPost(`${this.basePath}/DPB0114`,l)}queryRTMapByUk(u){let l={ReqHeader:this.api.getReqHeader(d.Nx.queryRTMapByUk),ReqBody:u};return this.api.npPost(`${this.basePath}/DPB0115`,l)}}return g.\u0275fac=function(u){return new(u||g)(A.LFG(m.K))},g.\u0275prov=A.Yz7({token:g,factory:g.\u0275fac,providedIn:"root"}),g})()}}]);