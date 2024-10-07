"use strict";(self.webpackChunksrcAngular=self.webpackChunksrcAngular||[]).push([[3076],{13076:(z,f,e)=>{e.r(f),e.d(f,{LoginModule:()=>T});var M=e(24783),O=e(69808),u=e(99291),A=e(96614),h=e(55679),m=e(24300),C=e(27896),b=e(4924),v=e(40704),_=e(45682),x=e(89709),s=e(93075),y=e(18505),P=e(68306),n=e(87587),w=e(3937),k=e(51062);const S=["username"],Z=[{path:"",component:(()=>{class i{constructor(t,o,r,g,a,d,c,l,B,U,R){this.fb=t,this.router=o,this.ngxService=r,this.tokenService=g,this.toolService=a,this.alertService=d,this.signBlockService=c,this.util=l,this.userService=B,this.funcService=U,this.route=R,this.isReady=!1,this.relogin=!1,this.form=this.fb.group({uname:new s.NI(""),pwd:new s.NI("")})}ngOnInit(){this.route.queryParams.subscribe(t=>{this.relogin=null==t.re}),setTimeout(()=>{this.isReady=!0,setTimeout(()=>{this.username.nativeElement.focus()},0)},1e3)}ngAfterViewInit(){this.username.nativeElement.focus()}submitForm(){var t,o;this.ngxService.start(),this.tokenService.auth(null===(t=this.form.get("uname"))||void 0===t?void 0:t.value,this.toolService.Base64Encoder(null===(o=this.form.get("pwd"))||void 0===o?void 0:o.value)).subscribe(r=>{if(r&&r.access_token)this.toolService.setTokenInfo(r),this.toolService.writeToken(r.access_token),this.toolService.writeToken(JSON.stringify(this.toolService.decodeToken()),"decode_token"),this.signBlockService.getSignBlock().subscribe(g=>{this.toolService.checkSuccess(g.ResHeader)&&(this.toolService.writeSignBlock(g.Res_getSignBlock.signBlock),this.util.getAcConf().subscribe(a=>{this.toolService.checkDpSuccess(a.ResHeader)&&(this.toolService.writeAcConf(JSON.stringify(a.RespBody)),this.auth(["AC0004","AC0005","AC0102","AC0203","AC0204","AC0205","AC0221","AC0223","AC0224","AC0225","AC0226","AC0302","AC0304","AC0305","AC0318","AC0505"]).subscribe(c=>{this.toolService.setHyperLinkAuth(c)}),this.userService.queryUserDataByLoginUser().subscribe(c=>{this.toolService.checkDpSuccess(c.ResHeader)&&(this.toolService.setUserID(c.RespBody.userID),this.toolService.setUserAlias(c.RespBody.userAlias?c.RespBody.userAlias:""),this.setFuncList().pipe((0,y.b)(l=>{l&&this.router.navigateByUrl("/dashboard")})).subscribe())}))}))});else{let g=r.ResHeader;this.alertService.ok(g.rtnCode,g.rtnMsg)}this.ngxService.stopAll()})}setFuncList(){return new P.y(t=>{this.funcService.queryAllFunc().subscribe(o=>{this.toolService.checkDpSuccess(o.ResHeader)&&(this.toolService.setFuncList(o.RespBody.funcList),t.next(!0))})})}auth(t){let o=[];return new P.y(r=>{this.userService.queryFuncByLoginUser().subscribe(g=>{if(this.toolService.checkDpSuccess(g.ResHeader)){this.toolService.writeRoleFuncCodeList(g.RespBody.funcCodeList);for(let a=0;a<t.length;a++){const d=t[a];let c=g.RespBody.funcCodeList.findIndex(l=>l===d);o.findIndex(l=>l.funCode==d)<0?o.push({funCode:d,canExecute:c>=0}):c>=0&&(o[o.findIndex(l=>l.funCode==d)].canExecute=!0)}r.next(o)}})})}ssologin(t){window.location.href=`${location.protocol}//${location.host}/dgrv4/ssotoken/acidp/${t}/acIdPAuth`}goLdapPage(){this.router.navigate(["/ldap"],{queryParams:{type:"LDAP"}})}goMLdapPage(){this.router.navigate(["/ldap"],{queryParams:{type:"MLDAP"}})}goAPIPage(){this.router.navigate(["/ldap"],{queryParams:{type:"API"}})}}return i.\u0275fac=function(t){return new(t||i)(n.Y36(s.qu),n.Y36(u.F0),n.Y36(w.LA),n.Y36(x.B),n.Y36(_.g),n.Y36(v.c),n.Y36(b.T),n.Y36(C.f),n.Y36(m.K),n.Y36(h.B),n.Y36(u.gz))},i.\u0275cmp=n.Xpm({type:i,selectors:[["app-login"]],viewQuery:function(t,o){if(1&t&&n.Gf(S,7),2&t){let r;n.iGM(r=n.CRH())&&(o.username=r.first)}},features:[n._Bn([x.B,v.c,_.g,b.T,C.f,m.K,h.B,A.K])],decls:67,vars:25,consts:[[1,"login-page",3,"hidden"],[1,"row","col-12","h-100","p-0"],[1,"col-7","h-100"],["src","assets/images/login_left_base.png","alt","",1,"w-100","h-100","login-base"],["src","assets/images/img_login_visual.png","alt","",1,"w-100","h-100","login-visual"],[1,"col-5","h-100"],[1,"row","justify-content-md-center","bg",2,"padding-top","15vh"],[2,"width","380px"],[3,"formGroup","ngSubmit"],[1,"login_section"],[1,"logo_top"],["src","assets/images/DigiFusion_digiRunner_logo_horizontal.png","alt","","title","v202102251030-v3"],[1,"line",3,"hidden"],[2,"color","#EF622D","font-weight","600","font-size","24px"],[1,"sign_box"],["for","uname",1,"control-label","float-left"],[1,"sign_row",2,"position","relative"],[2,"position","absolute","left","7px"],["src","assets/images/i_user.png","alt","number"],["type","text","formControlName","uname",1,"form-control",3,"placeholder"],["username",""],[2,"margin-left","8px","position","absolute","left","7px"],["src","assets/images/i_pwd.png","alt","pass",2,"width","25px","height","30px"],["type","password","formControlName","pwd",1,"form-control",3,"placeholder"],[1,"sign_row"],["type","submit",1,"btn","btn-login",2,"width","250px"],[1,"row",2,"justify-content","center"],[1,"sign_row","mr-2"],["type","button",1,"btn","btn-login",3,"click"],[1,"pi","pi-google"],[1,"pi","pi-microsoft"],["type","button",1,"btn","btn-login",2,"width","250px",3,"click"],[1,"footer"],[1,"container"]],template:function(t,o){1&t&&(n.TgZ(0,"div",0)(1,"div",1)(2,"div",2),n._UZ(3,"img",3)(4,"img",4),n.qZA(),n.TgZ(5,"div",5)(6,"div",6)(7,"div",7)(8,"form",8),n.NdJ("ngSubmit",function(){return o.submitForm()}),n.TgZ(9,"section",9)(10,"div",10),n._UZ(11,"img",11),n.qZA(),n.TgZ(12,"div",12)(13,"label",13),n._uU(14,"Welcome Back!"),n.qZA()(),n.TgZ(15,"div",12)(16,"div")(17,"label",13),n._uU(18),n.ALo(19,"translate"),n.qZA()(),n.TgZ(20,"div")(21,"label",13),n._uU(22),n.ALo(23,"translate"),n.qZA()()(),n.TgZ(24,"div",14)(25,"label",15),n._uU(26),n.ALo(27,"translate"),n.qZA(),n.TgZ(28,"div",16)(29,"span",17),n._UZ(30,"img",18),n.qZA(),n._UZ(31,"input",19,20),n.ALo(33,"translate"),n.qZA(),n.TgZ(34,"label",15),n._uU(35),n.ALo(36,"translate"),n.qZA(),n.TgZ(37,"div",16)(38,"span",21),n._UZ(39,"img",22),n.qZA(),n._UZ(40,"input",23),n.ALo(41,"translate"),n.qZA(),n.TgZ(42,"div",24)(43,"button",25),n._uU(44),n.ALo(45,"translate"),n.qZA()(),n.TgZ(46,"div",26)(47,"div",27)(48,"button",28),n.NdJ("click",function(){return o.ssologin("GOOGLE")}),n._UZ(49,"i",29),n.qZA()(),n.TgZ(50,"div",24)(51,"button",28),n.NdJ("click",function(){return o.ssologin("MS")}),n._UZ(52,"i",30),n.qZA()()(),n.TgZ(53,"div",26)(54,"div",27)(55,"button",28),n.NdJ("click",function(){return o.goLdapPage()}),n._uU(56," LDAP"),n.qZA()(),n.TgZ(57,"div",24)(58,"button",28),n.NdJ("click",function(){return o.goMLdapPage()}),n._uU(59," MLDAP"),n.qZA()()(),n.TgZ(60,"div",24)(61,"button",31),n.NdJ("click",function(){return o.goAPIPage()}),n._uU(62,"API"),n.qZA()()()()()()()()()(),n.TgZ(63,"footer",32)(64,"div",33)(65,"span"),n._uU(66,"Copyright\xa9 TPIsoftware. All Rights Reserved. "),n.qZA()()()),2&t&&(n.Q6J("hidden",!o.isReady),n.xp6(8),n.Q6J("formGroup",o.form),n.xp6(4),n.Q6J("hidden",!o.relogin),n.xp6(3),n.Q6J("hidden",o.relogin),n.xp6(3),n.Oqu(n.lcZ(19,11,"logoutBySystem")),n.xp6(4),n.Oqu(n.lcZ(23,13,"plz_login_again")),n.xp6(4),n.Oqu(n.lcZ(27,15,"user_name")),n.xp6(5),n.Q6J("placeholder",n.lcZ(33,17,"user_name")),n.xp6(4),n.Oqu(n.lcZ(36,19,"user_password")),n.xp6(5),n.Q6J("placeholder",n.lcZ(41,21,"user_password")),n.xp6(4),n.Oqu(n.lcZ(45,23,"login")))},directives:[s._Y,s.JL,s.sg,s.Fj,s.JJ,s.u],pipes:[k.X$],styles:['div.login[_ngcontent-%COMP%]{height:100%;width:100%;background:#ffffff}form.login_form[_ngcontent-%COMP%]{background-color:#fff;height:475px;width:381px;box-shadow:0 0 40px #bc7730}div.bg_img[_ngcontent-%COMP%]{font-family:PingFangSC-Regular,sans-serif;font-size:16px;margin:0;padding:0;background-color:#fbf9f6;font-weight:400;color:#5a5541;height:100%;background:#ff8f20;background:linear-gradient(135deg,#ffae22 0%,#ff8f20 100%);filter:progid:DXImageTransform.Microsoft.gradient(startColorstr="#ffae22",endColorstr="#ff8f20",GradientType=1);min-height:100%;display:flex;flex-direction:column}section.bg[_ngcontent-%COMP%], div.bg[_ngcontent-%COMP%]{height:100%;margin-left:0!important;margin-right:0!important}.sign[_ngcontent-%COMP%]{width:20%;height:475px;background-color:#fff;margin:100px auto 0;box-shadow:0 0 40px #bc7730;display:table}.logo_top[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:350px}div.logo_top[_ngcontent-%COMP%]{padding-top:65px;background-color:#fff}div.logo_top[_ngcontent-%COMP%], i.logo_top[_ngcontent-%COMP%]{text-align:center;display:inherit;width:100%}.line[_ngcontent-%COMP%]{text-align:center;padding-top:30px;background-color:#fff}.line[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:275px}.sign_row[_ngcontent-%COMP%]{text-align:center;padding:12px 0}.sign_row[_ngcontent-%COMP%]   img[_ngcontent-%COMP%]{width:30px;padding-top:1px}.sign_box[_ngcontent-%COMP%]{padding:28px 0;text-align:center;background:#fff}.date[_ngcontent-%COMP%]   input[_ngcontent-%COMP%], .sign_box[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]{height:46px;width:100%;font-size:20px;display:inline-block;padding:14px 10px 13px 50px;background-color:#fff;color:#666;-webkit-box-shadow:inset 0 1px 1px rgba(0,0,0,.075);transition:border-color ease-in-out .15s,box-shadow ease-in-out .15s;border:1px solid #B7B7B7;box-shadow:0 0 15px 10px #f8f8f8;border-radius:30px}.date[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]:active{border:1px #27BEC5 solid}.date[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]:focus{border:1px #27BEC5 solid;box-shadow:inset 0 0 #00000013,0 0 #66afe999}.sign_row[_ngcontent-%COMP%]   span[_ngcontent-%COMP%]{position:absolute;margin-left:7px;margin-top:7px;align-items:center}.footer[_ngcontent-%COMP%]{position:fixed;text-align:center;bottom:0;width:100%;height:30px;line-height:30px;color:#666;background-color:#dedede;z-index:10}.index_footer[_ngcontent-%COMP%]{text-align:center;background-color:#5a5541;position:fixed;bottom:0}.login_bg[_ngcontent-%COMP%]{background-image:linear-gradient(to right,#ff8f20,#ffae22)}.btn-login[_ngcontent-%COMP%]{color:#fff;width:120px;height:35px;background:#666666;border-radius:50px;flex:none;order:2;flex-grow:0}.btn-login[_ngcontent-%COMP%]:hover{background:#EF622D}[_ngcontent-%COMP%]::-webkit-input-placeholder{color:#999}[_ngcontent-%COMP%]:-moz-placeholder{color:#999}[_ngcontent-%COMP%]::-moz-placeholder{color:#999}[_ngcontent-%COMP%]:-ms-input-placeholder{color:#999}[_nghost-%COMP%]{display:block}.login-page[_ngcontent-%COMP%]{position:absolute;top:0;left:0;right:0;bottom:0;overflow:auto;background:#ffffff;text-align:center}.login-page[_ngcontent-%COMP%]   .col-lg-4[_ngcontent-%COMP%]{padding:0}.login-page[_ngcontent-%COMP%]   .input-lg[_ngcontent-%COMP%]{height:46px;padding:10px 16px;font-size:18px;line-height:1.3333333;border-radius:0}.login-page[_ngcontent-%COMP%]   .input-underline[_ngcontent-%COMP%]{background:0 0;border:none;box-shadow:none;border-bottom:2px solid rgba(255,255,255,.5);color:#fff;border-radius:0}.login-page[_ngcontent-%COMP%]   .input-underline[_ngcontent-%COMP%]:focus{border-bottom:2px solid #fff;box-shadow:none}.login-page[_ngcontent-%COMP%]   .rounded-btn[_ngcontent-%COMP%]{border-radius:50px;color:#fffc;background:#fff;border:2px solid rgba(255,255,255,.8);font-size:18px;line-height:40px;padding:0 25px}.login-page[_ngcontent-%COMP%]   .rounded-btn[_ngcontent-%COMP%]:hover, .login-page[_ngcontent-%COMP%]   .rounded-btn[_ngcontent-%COMP%]:focus, .login-page[_ngcontent-%COMP%]   .rounded-btn[_ngcontent-%COMP%]:active, .login-page[_ngcontent-%COMP%]   .rounded-btn[_ngcontent-%COMP%]:visited{color:#fff;border:2px solid white;outline:none}.login-page[_ngcontent-%COMP%]   h1[_ngcontent-%COMP%]{font-weight:400;margin-top:20px;margin-bottom:10px;font-size:36px}.login-page[_ngcontent-%COMP%]   h1[_ngcontent-%COMP%]   small[_ngcontent-%COMP%]{color:#ffffffb3}.login-page[_ngcontent-%COMP%]   .form-group[_ngcontent-%COMP%]{padding:8px 0}.login-page[_ngcontent-%COMP%]   .form-group[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]::-webkit-input-placeholder{color:#fff9!important}.login-page[_ngcontent-%COMP%]   .form-group[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]:-moz-placeholder{color:#fff9!important}.login-page[_ngcontent-%COMP%]   .form-group[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]::-moz-placeholder{color:#fff9!important}.login-page[_ngcontent-%COMP%]   .form-group[_ngcontent-%COMP%]   input[_ngcontent-%COMP%]:-ms-input-placeholder{color:#fff9!important}.login-page[_ngcontent-%COMP%]   .form-content[_ngcontent-%COMP%]{padding:40px 0}.login-page[_ngcontent-%COMP%]   .user-avatar[_ngcontent-%COMP%]{border-radius:50%;border:2px solid #fff}.login_section[_ngcontent-%COMP%]{background:#fff;font-size:16px;margin:0;background-color:#fbf9f6;font-weight:400}.login-base[_ngcontent-%COMP%]{position:absolute;z-index:1;left:0}.login-visual[_ngcontent-%COMP%]{position:absolute;z-index:3;left:6%}']}),i})()}];let L=(()=>{class i{}return i.\u0275fac=function(t){return new(t||i)},i.\u0275mod=n.oAB({type:i}),i.\u0275inj=n.cJS({imports:[[u.Bz.forChild(Z)],u.Bz]}),i})(),T=(()=>{class i{}return i.\u0275fac=function(t){return new(t||i)},i.\u0275mod=n.oAB({type:i}),i.\u0275inj=n.cJS({imports:[[O.ez,L,s.u5,s.UX,M.m]]}),i})()}}]);