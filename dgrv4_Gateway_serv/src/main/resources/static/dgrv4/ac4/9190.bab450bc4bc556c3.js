"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[9190],{69190:(P,y,n)=>{n.r(y),n.d(y,{Ac1305Module:()=>j});var T=n(88893),v=n(69808),c=n(99291),x=n(58348),A=n(14525),i=n(93075),d=n(71764),Z=n(85794),I=n(57553),t=n(87587),M=n(56435),C=n(45682),Y=n(46877),b=n(23745),q=n(40704),N=n(63710),S=n(14036),U=n(75652),O=n(51062);function w(o,s){if(1&o&&(t.TgZ(0,"div",19)(1,"small",20),t._uU(2),t.qZA(),t.TgZ(3,"small",20),t._uU(4),t.qZA(),t.TgZ(5,"small",20),t._uU(6),t.qZA(),t.TgZ(7,"small",20),t._uU(8),t.qZA(),t.TgZ(9,"small",20),t._uU(10),t.qZA(),t.TgZ(11,"small",20),t._uU(12),t.qZA()()),2&o){const e=t.oxw();t.xp6(2),t.Oqu(e.timeType.errors.isRequired),t.xp6(2),t.Oqu(e.timeType.errors.maxlength),t.xp6(2),t.Oqu(e.timeType.errors.minlength),t.xp6(2),t.Oqu(e.timeType.errors.max),t.xp6(2),t.Oqu(e.timeType.errors.min),t.xp6(2),t.Oqu(e.timeType.errors.pattern)}}const p=function(){return{width:"100%"}},u=function(){return{width:"95%"}};function H(o,s){if(1&o&&t._UZ(0,"p-calendar",21),2&o){const e=t.oxw();t.Akn(t.DdM(7,p)),t.Q6J("inputStyle",t.DdM(8,u))("showIcon",!0)("readonlyInput",!0)("minDate",e.minDate)("maxDate",e.maxDate)}}function B(o,s){if(1&o&&t._UZ(0,"p-calendar",22),2&o){const e=t.oxw();t.Akn(t.DdM(7,p)),t.Q6J("inputStyle",t.DdM(8,u))("showIcon",!0)("readonlyInput",!0)("minDate",e.minDate)("maxDate",e.maxDate)}}function J(o,s){if(1&o&&(t.TgZ(0,"div",19)(1,"small",20),t._uU(2),t.qZA(),t.TgZ(3,"small",20),t._uU(4),t.qZA(),t.TgZ(5,"small",20),t._uU(6),t.qZA(),t.TgZ(7,"small",20),t._uU(8),t.qZA(),t.TgZ(9,"small",20),t._uU(10),t.qZA(),t.TgZ(11,"small",20),t._uU(12),t.qZA()()),2&o){const e=t.oxw();t.xp6(2),t.Oqu(e.startDate.errors.isRequired),t.xp6(2),t.Oqu(e.startDate.errors.maxlength),t.xp6(2),t.Oqu(e.startDate.errors.minlength),t.xp6(2),t.Oqu(e.startDate.errors.max),t.xp6(2),t.Oqu(e.startDate.errors.min),t.xp6(2),t.Oqu(e.startDate.errors.pattern)}}function R(o,s){if(1&o&&t._UZ(0,"p-calendar",23),2&o){const e=t.oxw();t.Akn(t.DdM(7,p)),t.Q6J("inputStyle",t.DdM(8,u))("showIcon",!0)("readonlyInput",!0)("minDate",e.minDate)("maxDate",e.maxDate)}}function Q(o,s){if(1&o&&t._UZ(0,"p-calendar",24),2&o){const e=t.oxw();t.Akn(t.DdM(7,p)),t.Q6J("inputStyle",t.DdM(8,u))("showIcon",!0)("readonlyInput",!0)("minDate",e.minDate)("maxDate",e.maxDate)}}function _(o,s){if(1&o&&(t.TgZ(0,"div"),t._UZ(1,"p-dropdown",25),t.ALo(2,"translate"),t.TgZ(3,"label",26),t._uU(4,"H ~ "),t.qZA(),t._UZ(5,"p-dropdown",27),t.ALo(6,"translate"),t.TgZ(7,"label",26),t._uU(8,"H"),t.qZA()()),2&o){const e=t.oxw();t.xp6(1),t.s9C("placeholder",t.lcZ(2,4,"plz_chs")),t.Q6J("options",e.hourData),t.xp6(4),t.s9C("placeholder",t.lcZ(6,6,"plz_chs")),t.Q6J("options",e.hourData)}}function E(o,s){if(1&o&&(t.TgZ(0,"div",19)(1,"small",20),t._uU(2),t.qZA(),t.TgZ(3,"small",20),t._uU(4),t.qZA(),t.TgZ(5,"small",20),t._uU(6),t.qZA(),t.TgZ(7,"small",20),t._uU(8),t.qZA(),t.TgZ(9,"small",20),t._uU(10),t.qZA(),t.TgZ(11,"small",20),t._uU(12),t.qZA()()),2&o){const e=t.oxw();t.xp6(2),t.Oqu(e.endDate.errors.isRequired),t.xp6(2),t.Oqu(e.endDate.errors.maxlength),t.xp6(2),t.Oqu(e.endDate.errors.minlength),t.xp6(2),t.Oqu(e.endDate.errors.max),t.xp6(2),t.Oqu(e.endDate.errors.min),t.xp6(2),t.Oqu(e.endDate.errors.pattern)}}let F=(()=>{class o extends A.H{constructor(e,a,r,m,l,h,D){super(e,a),this.fb=r,this.toolService=m,this.list=l,this.reportService=h,this.alertService=D,this.timeTypes=[],this.today=new Date,this.minDate=new Date,this.maxDate=new Date,this.displayStartDate="",this.displayEndDate="",this.hourData=[]}ngOnInit(){this.acConf=this.toolService.getAcConf(),this.canvas=document.getElementById("reportChart"),this.ctx=this.canvas.getContext("2d"),this.form=this.fb.group({timeType:new i.NI("DAY"),startDate:new i.NI(""),endDate:new i.NI(""),startHour:new i.NI("00"),endHour:new i.NI("23")}),this.reportService.queryBadattemptConnection_before().subscribe(a=>{this.toolService.checkDpSuccess(a.ResHeader)&&this.addFormValidator(this.form,a.RespBody.constraints)});let e={encodeItemNo:this.toolService.Base64Encoder(this.toolService.BcryptEncoder("REPORT_TIME_TYPE"))+",37",isDefault:"N"};this.list.querySubItemsByItemNo(e).subscribe(a=>{if(this.toolService.checkDpSuccess(a.ResHeader)){let r=[];if(a.RespBody.subItems)for(let m of a.RespBody.subItems)r.push({label:m.subitemName,value:m.subitemNo});this.timeTypes=r}}),this.timeType.valueChanges.subscribe(a=>{this.minDate=new Date,this.startDate.setValue(""),this.endDate.setValue(""),"DAY"==a&&this.minDate.setDate(this.today.getDate()-90),"MONTH"==a&&this.minDate.setMonth(this.today.getMonth()+1-13),"MINUTE"==a&&(this.minDate.setDate(this.today.getDate()-2),this.startHour.setValue("00"),this.endHour.setValue("23")),this.startDate.setValue(new Date),this.endDate.setValue(new Date)});for(let a=0;a<24;a++){let r=("0"+a).slice(-2);this.hourData.push({label:r,value:r})}}submitForm(){$("#reportChart").remove(),$("#div_canvas").append('<canvas id="reportChart"></canvas>'),this.canvas=document.getElementById("reportChart"),this.canvas.height="100%",this.ctx=this.canvas.getContext("2d");let e={timeType:this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.timeType.value))+","+this.timeTypes.findIndex(a=>a.value==this.timeType.value)};if("DAY"==this.timeType.value)e.startDate=d(this.startDate.value).format("YYYY/MM/DD"),e.endDate=d(this.endDate.value).format("YYYY/MM/DD");else if("MONTH"==this.timeType.value){let a=new Date(this.startDate.value);a.setDate(1);let r=new Date(this.endDate.value);r.setMonth(r.getMonth()+1,1),r.setDate(r.getDate()-1),r>this.today&&(r=this.today),e.startDate=d(a).format("YYYY/MM/DD"),e.endDate=d(r).format("YYYY/MM/DD")}else e.startDate=d(this.startDate.value).format("YYYY/MM/DD"),e.endDate=d(this.startDate.value).format("YYYY/MM/DD"),e.startHour=this.startHour.value,e.endHour=this.endHour.value;this.reportService.queryBadattemptConnection(e).subscribe(a=>{if(this.toolService.checkDpSuccess(a.ResHeader)){let r=[];a.RespBody.datasets.map(m=>{let l={};const g=`rgb(${Math.round(255*Math.random())}, ${Math.round(255*Math.random())}, ${Math.round(255*Math.random())})`;l.label=m.label,l.data=m.data,l.backgroundColor=g,l.borderColor=g,l.borderWidth=1,l.fill=!1,r.push(l)}),new Z.kL(this.ctx,{type:"line",data:{labels:a.RespBody.labels,datasets:r},options:{maintainAspectRatio:!1,responsive:!0,plugins:{title:{display:!0,text:a.RespBody.reportName},tooltip:{mode:"index",intersect:!1},legend:{position:"right"}},scales:{x:{title:{display:!0,text:a.RespBody.xLable}},y:{title:{display:!0,text:a.RespBody.yLable}}}}})}})}exportReport(){this.alertService.ok("","Only Available in Enterprise version",I.NK.warning)}get timeType(){return this.form.get("timeType")}get startDate(){return this.form.get("startDate")}get endDate(){return this.form.get("endDate")}get startHour(){return this.form.get("startHour")}get endHour(){return this.form.get("endHour")}}return o.\u0275fac=function(e){return new(e||o)(t.Y36(c.gz),t.Y36(M.W),t.Y36(i.qu),t.Y36(C.g),t.Y36(Y.X),t.Y36(b.r),t.Y36(q.c))},o.\u0275cmp=t.Xpm({type:o,selectors:[["app-ac1305"]],features:[t._Bn([x.s]),t.qOj],decls:36,vars:36,consts:[[3,"title"],[3,"formGroup","ngSubmit"],[1,"form-group","row"],[1,"col-4"],["id","timeType_label",1,"control-label"],["formControlName","timeType",3,"showClear","options","filter","placeholder"],["class","text-danger",4,"ngIf"],["id","startDate_label",1,"control-label"],[2,"display","block"],["appendTo","body","formControlName","startDate","dateFormat","yy/mm/dd",3,"style","inputStyle","showIcon","readonlyInput","minDate","maxDate",4,"ngIf"],["view","month","appendTo","body","formControlName","startDate","dateFormat","yy/mm",3,"style","inputStyle","showIcon","readonlyInput","minDate","maxDate",4,"ngIf"],["id","endDate_label",1,"control-label"],["appendTo","body","formControlName","endDate","dateFormat","yy/mm/dd",3,"style","inputStyle","showIcon","readonlyInput","minDate","maxDate",4,"ngIf"],["view","month","appendTo","body","formControlName","endDate","dateFormat","yy/mm",3,"style","inputStyle","showIcon","readonlyInput","minDate","maxDate",4,"ngIf"],[4,"ngIf"],[1,"col-12"],["type","submit",1,"btn","tpi-btn","tpi-primary","float-left","mr-3",3,"disabled"],["id","div_canvas",1,"col-12","zoom"],["id","reportChart"],[1,"text-danger"],[1,"form-text"],["appendTo","body","formControlName","startDate","dateFormat","yy/mm/dd",3,"inputStyle","showIcon","readonlyInput","minDate","maxDate"],["view","month","appendTo","body","formControlName","startDate","dateFormat","yy/mm",3,"inputStyle","showIcon","readonlyInput","minDate","maxDate"],["appendTo","body","formControlName","endDate","dateFormat","yy/mm/dd",3,"inputStyle","showIcon","readonlyInput","minDate","maxDate"],["view","month","appendTo","body","formControlName","endDate","dateFormat","yy/mm",3,"inputStyle","showIcon","readonlyInput","minDate","maxDate"],["formControlName","startHour",3,"options","placeholder"],[2,"padding","5px"],["formControlName","endHour",3,"options","placeholder"]],template:function(e,a){1&e&&(t.TgZ(0,"app-container",0)(1,"form",1),t.NdJ("ngSubmit",function(){return a.submitForm()}),t.TgZ(2,"div",2)(3,"div",3)(4,"label",4),t._uU(5),t.ALo(6,"translate"),t.qZA(),t._UZ(7,"p-dropdown",5),t.ALo(8,"translate"),t.YNc(9,w,13,6,"div",6),t.qZA(),t.TgZ(10,"div",3)(11,"label",7),t._uU(12),t.ALo(13,"translate"),t.ALo(14,"translate"),t.qZA(),t.TgZ(15,"div",8),t.YNc(16,H,1,9,"p-calendar",9),t.YNc(17,B,1,9,"p-calendar",10),t.qZA(),t.YNc(18,J,13,6,"div",6),t.qZA(),t.TgZ(19,"div",3)(20,"label",11),t._uU(21),t.ALo(22,"translate"),t.ALo(23,"translate"),t.qZA(),t.TgZ(24,"div",8),t.YNc(25,R,1,9,"p-calendar",12),t.YNc(26,Q,1,9,"p-calendar",13),t.YNc(27,_,9,8,"div",14),t.qZA(),t.YNc(28,E,13,6,"div",6),t.qZA()(),t.TgZ(29,"div",2)(30,"div",15)(31,"button",16),t._uU(32),t.ALo(33,"translate"),t.qZA()()()(),t.TgZ(34,"div",17),t._UZ(35,"canvas",18),t.qZA()()),2&e&&(t.Q6J("title",a.title),t.xp6(1),t.Q6J("formGroup",a.form),t.xp6(4),t.Oqu(t.lcZ(6,21,"time_unit")),t.xp6(2),t.Akn(t.DdM(35,p)),t.s9C("placeholder",t.lcZ(8,23,"plz_chs")),t.Q6J("showClear",!1)("options",a.timeTypes)("filter",!1),t.xp6(2),t.Q6J("ngIf",a.timeType.invalid&&(a.timeType.dirty||a.timeType.touched)),t.xp6(3),t.Oqu("MINUTE"==a.timeType.value?t.lcZ(13,25,"query_date"):t.lcZ(14,27,"start_date")),t.xp6(4),t.Q6J("ngIf","MONTH"!=a.timeType.value),t.xp6(1),t.Q6J("ngIf","MONTH"==a.timeType.value),t.xp6(1),t.Q6J("ngIf",a.startDate.invalid&&(a.startDate.dirty||a.startDate.touched)),t.xp6(3),t.Oqu("MINUTE"==a.timeType.value?t.lcZ(22,29,"date_range"):t.lcZ(23,31,"end_date")),t.xp6(4),t.Q6J("ngIf","DAY"==a.timeType.value),t.xp6(1),t.Q6J("ngIf","MONTH"==a.timeType.value),t.xp6(1),t.Q6J("ngIf","MINUTE"==a.timeType.value),t.xp6(1),t.Q6J("ngIf",a.endDate.invalid&&(a.endDate.dirty||a.endDate.touched)),t.xp6(3),t.Q6J("disabled",a.form.invalid),t.xp6(1),t.Oqu(t.lcZ(33,33,"button.search")))},directives:[N.e,i._Y,i.JL,i.sg,S.Lt,i.JJ,i.u,v.O5,U.f],pipes:[O.X$],styles:[".zoom[_ngcontent-%COMP%]{zoom:125%}"]}),o})();var f=n(86347);const L=[{path:"",component:F,canActivate:[f.u6]}];let z=(()=>{class o{}return o.\u0275fac=function(e){return new(e||o)},o.\u0275mod=t.oAB({type:o}),o.\u0275inj=t.cJS({imports:[[c.Bz.forChild(L)],c.Bz]}),o})();var V=n(24783);let j=(()=>{class o{}return o.\u0275fac=function(e){return new(e||o)},o.\u0275mod=t.oAB({type:o}),o.\u0275inj=t.cJS({providers:[f.u6],imports:[[v.ez,z,T.W,V.m,i.UX,i.u5]]}),o})()}}]);