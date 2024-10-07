package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpReportUrlVo;

@Service
public class TsmpReportUrlTableInitializer {
	
	private  List<TsmpReportUrlVo> tsmpReportUrlList = new LinkedList<>();
	
	public List<TsmpReportUrlVo> insertTsmpReportUrl() {
    	try {
        	String reportId;
        	String timeRange;
        	String reportUrl;
        	
        	//digiRunner儀表板
        	createTsmpReportUrl((reportId = "AC0502"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/5dee56b0-0fb9-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now%2FM,to:now%2FM))&_a=(description:'',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:12,i:c29bac49-1a5e-4cca-9f22-5b91ffb9f430,w:48,x:0,y:0),id:ebeb5730-0e59-11ec-9863-558fe90a2da1,panelIndex:c29bac49-1a5e-4cca-9f22-5b91ffb9f430,title:Uptime,type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'664bc58d-3c28-4951-bf69-5a3814ef6794',w:24,x:0,y:12),id:ce110140-0e56-11ec-9863-558fe90a2da1,panelIndex:'664bc58d-3c28-4951-bf69-5a3814ef6794',title:'Heart%20beat',type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'490259e5-c201-44ab-a8fe-c11cb3f6d515',w:24,x:24,y:12),id:cce62060-0e57-11ec-9863-558fe90a2da1,panelIndex:'490259e5-c201-44ab-a8fe-c11cb3f6d515',title:'Heap%20total',type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'7697781e-ea0a-45f9-9073-50779be7c42f',w:24,x:0,y:27),id:'1a375eb0-0e58-11ec-9863-558fe90a2da1',panelIndex:'7697781e-ea0a-45f9-9073-50779be7c42f',title:CPU,type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:b934c6b4-7a70-48ca-ab52-4fb39bc3fd90,w:24,x:24,y:27),id:'7ee9df90-0e58-11ec-9863-558fe90a2da1',panelIndex:b934c6b4-7a70-48ca-ab52-4fb39bc3fd90,title:ThreadPool,type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:14,i:'031a643d-79c5-47ba-aab4-b9d7f5a7ef3e',w:48,x:0,y:42),id:'471cfb90-0e5a-11ec-9863-558fe90a2da1',panelIndex:'031a643d-79c5-47ba-aab4-b9d7f5a7ef3e',title:Connections,type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'digiRunner%20Monitor',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0502"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/063a0020-34db-11ed-a002-d1c9da076bfc?embed=true&_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now/d,to:now/d))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0502"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/5dee56b0-0fb9-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:12,i:c29bac49-1a5e-4cca-9f22-5b91ffb9f430,w:48,x:0,y:0),id:ebeb5730-0e59-11ec-9863-558fe90a2da1,panelIndex:c29bac49-1a5e-4cca-9f22-5b91ffb9f430,title:Uptime,type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'664bc58d-3c28-4951-bf69-5a3814ef6794',w:24,x:0,y:12),id:ce110140-0e56-11ec-9863-558fe90a2da1,panelIndex:'664bc58d-3c28-4951-bf69-5a3814ef6794',title:'Heart%20beat',type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'490259e5-c201-44ab-a8fe-c11cb3f6d515',w:24,x:24,y:12),id:cce62060-0e57-11ec-9863-558fe90a2da1,panelIndex:'490259e5-c201-44ab-a8fe-c11cb3f6d515',title:'Heap%20total',type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'7697781e-ea0a-45f9-9073-50779be7c42f',w:24,x:0,y:27),id:'1a375eb0-0e58-11ec-9863-558fe90a2da1',panelIndex:'7697781e-ea0a-45f9-9073-50779be7c42f',title:CPU,type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:b934c6b4-7a70-48ca-ab52-4fb39bc3fd90,w:24,x:24,y:27),id:'7ee9df90-0e58-11ec-9863-558fe90a2da1',panelIndex:b934c6b4-7a70-48ca-ab52-4fb39bc3fd90,title:ThreadPool,type:visualization,version:'7.13.4'),(embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:14,i:'031a643d-79c5-47ba-aab4-b9d7f5a7ef3e',w:48,x:0,y:42),id:'471cfb90-0e5a-11ec-9863-558fe90a2da1',panelIndex:'031a643d-79c5-47ba-aab4-b9d7f5a7ef3e',title:Connections,type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'digiRunner%20Monitor',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0502"),(timeRange = "Y"),(reportUrl = ""));
        	
    		createTsmpReportUrl((reportId = "AC0508"),(timeRange = "M"),(reportUrl = "/kibana/app/monitoring#/overview?_g=(cluster_uuid:'-PDqCRVSSsOLybUcUx-sfw',refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))"));
            createTsmpReportUrl((reportId = "AC0508"),(timeRange = "T"),(reportUrl = "/kibana/app/monitoring#/overview?_g=(cluster_uuid:'-PDqCRVSSsOLybUcUx-sfw',refreshInterval:(pause:!f,value:10000),time:(from:now%2Fd,to:now%2Fd))"));
            createTsmpReportUrl((reportId = "AC0508"),(timeRange = "W"),(reportUrl = "/kibana/app/monitoring#/overview?_g=(cluster_uuid:'-PDqCRVSSsOLybUcUx-sfw',refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))"));
            createTsmpReportUrl((reportId = "AC0508"),(timeRange = "Y"),(reportUrl = ""));
        	
            createTsmpReportUrl((reportId = "AC0510"),(timeRange = "M"),(reportUrl = ""));
        	createTsmpReportUrl((reportId = "AC0510"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/417c1b20-33d0-11ed-a002-d1c9da076bfc?embed=true&_g=(filters%3A!()%2Cquery%3A(language%3Akuery%2Cquery%3A'')%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0510"),(timeRange = "W"),(reportUrl = ""));
        	createTsmpReportUrl((reportId = "AC0510"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0901"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/287b1540-0bf9-11ec-ae66-f1cd066f898e?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'3f2049ff-2a78-49a5-b416-92ffec10152f',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'3f2049ff-2a78-49a5-b416-92ffec10152f',w:24,x:0,y:0),id:'0eced140-0bf9-11ec-ae66-f1cd066f898e',panelIndex:'3f2049ff-2a78-49a5-b416-92ffec10152f',title:'API%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88(%E5%88%86%E6%88%90%E5%8A%9F%2F%E5%A4%B1%E6%95%97)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'1.API%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88(%E5%88%86%E6%88%90%E5%8A%9F%2F%E5%A4%B1%E6%95%97)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0901"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/7fade889-ebb3-4701-9430-052d8cd0374d?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0901"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/287b1540-0bf9-11ec-ae66-f1cd066f898e?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'3f2049ff-2a78-49a5-b416-92ffec10152f',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'3f2049ff-2a78-49a5-b416-92ffec10152f',w:24,x:0,y:0),id:'0eced140-0bf9-11ec-ae66-f1cd066f898e',panelIndex:'3f2049ff-2a78-49a5-b416-92ffec10152f',title:'API%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88(%E5%88%86%E6%88%90%E5%8A%9F%2F%E5%A4%B1%E6%95%97)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'1.API%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88(%E5%88%86%E6%88%90%E5%8A%9F%2F%E5%A4%B1%E6%95%97)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0901"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0902"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/390fd550-0fbc-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'91bffd5d-83dd-4bad-b390-6c149c62b801',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'91bffd5d-83dd-4bad-b390-6c149c62b801',w:24,x:0,y:0),id:'13fe4c70-0e53-11ec-9863-558fe90a2da1',panelIndex:'91bffd5d-83dd-4bad-b390-6c149c62b801',title:'API%20%E6%AC%A1%E6%95%B8-%E6%99%82%E9%96%93%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'2.API%20%E6%AC%A1%E6%95%B8-%E6%99%82%E9%96%93%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0902"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/efe19006-fd6d-4732-9942-96c4b45fe52a?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0902"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/390fd550-0fbc-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'91bffd5d-83dd-4bad-b390-6c149c62b801',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'91bffd5d-83dd-4bad-b390-6c149c62b801',w:24,x:0,y:0),id:'13fe4c70-0e53-11ec-9863-558fe90a2da1',panelIndex:'91bffd5d-83dd-4bad-b390-6c149c62b801',title:'API%20%E6%AC%A1%E6%95%B8-%E6%99%82%E9%96%93%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'2.API%20%E6%AC%A1%E6%95%B8-%E6%99%82%E9%96%93%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0902"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0903"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/dbcaebe0-0fbc-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'3e7698b4-a310-495a-92b0-38228f8c1c38',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'3e7698b4-a310-495a-92b0-38228f8c1c38',w:24,x:0,y:0),id:b1b2eb10-0e53-11ec-9863-558fe90a2da1,panelIndex:'3e7698b4-a310-495a-92b0-38228f8c1c38',title:'API%E5%B9%B3%E5%9D%87%E6%99%82%E9%96%93%E8%A8%88%E7%AE%97%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'3.API%E5%B9%B3%E5%9D%87%E6%99%82%E9%96%93%E8%A8%88%E7%AE%97%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0903"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/5af49bdd-ec60-498a-b08b-66d297f03c0f?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0903"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/dbcaebe0-0fbc-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'3e7698b4-a310-495a-92b0-38228f8c1c38',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'3e7698b4-a310-495a-92b0-38228f8c1c38',w:24,x:0,y:0),id:b1b2eb10-0e53-11ec-9863-558fe90a2da1,panelIndex:'3e7698b4-a310-495a-92b0-38228f8c1c38',title:'API%E5%B9%B3%E5%9D%87%E6%99%82%E9%96%93%E8%A8%88%E7%AE%97%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'3.API%E5%B9%B3%E5%9D%87%E6%99%82%E9%96%93%E8%A8%88%E7%AE%97%E5%88%86%E6%9E%90(%E5%83%85%E6%88%90%E5%8A%9F)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0903"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0904"),(timeRange = "M"),(reportUrl = "/kibana/app/kibana#/discover?_g=(refreshInterval:(pause:!f,value:30000),time:(from:now%2FM,mode:quick,to:now%2FM))&_a=(columns:!(_source),index:'91ae93b0-d372-11e8-9cad-796349acce0a',interval:auto,query:(language:lucene,query:''),sort:!(ts,desc))"));
        	createTsmpReportUrl((reportId = "AC0904"),(timeRange = "T"),(reportUrl = "/kibana/app/discover#/?embed=true&_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now%2Fd,to:now%2Fd))&_a=(columns:!(),filters:!(),index:'7f32e780-33f5-11ed-a002-d1c9da076bfc',interval:auto,query:(language:kuery,query:''),sort:!(!(ts,desc)))"));
        	createTsmpReportUrl((reportId = "AC0904"),(timeRange = "W"),(reportUrl = "/kibana/app/discover#/?_g=(filters:!(),query:(language:kuery,query:''),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(columns:!(),filters:!(),index:'65243770-0bf8-11ec-ae66-f1cd066f898e',interval:auto,query:(language:kuery,query:''),sort:!(!(ts,desc)))"));
        	createTsmpReportUrl((reportId = "AC0904"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0905"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/45c7fd20-0fbe-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:bd5b123f-6d75-4999-be4a-eb6fba5bdfc2,filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:bd5b123f-6d75-4999-be4a-eb6fba5bdfc2,w:24,x:0,y:0),id:'019b35b0-0e59-11ec-9863-558fe90a2da1',panelIndex:bd5b123f-6d75-4999-be4a-eb6fba5bdfc2,title:API%E6%B5%81%E9%87%8F%E5%88%86%E6%9E%90,type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'7.API%E6%B5%81%E9%87%8F%E5%88%86%E6%9E%90',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0905"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/0db8a383-fb82-4cf8-872c-41e9f1b3bcef?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0905"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/45c7fd20-0fbe-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:bd5b123f-6d75-4999-be4a-eb6fba5bdfc2,filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:bd5b123f-6d75-4999-be4a-eb6fba5bdfc2,w:24,x:0,y:0),id:'019b35b0-0e59-11ec-9863-558fe90a2da1',panelIndex:bd5b123f-6d75-4999-be4a-eb6fba5bdfc2,title:API%E6%B5%81%E9%87%8F%E5%88%86%E6%9E%90,type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'7.API%E6%B5%81%E9%87%8F%E5%88%86%E6%9E%90',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0905"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0906"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/b40d82f0-0fbe-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'4b0d22a8-98e4-4b1f-921e-dd0ea0f492a1',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'4b0d22a8-98e4-4b1f-921e-dd0ea0f492a1',w:24,x:0,y:0),id:'84fb8d50-0e55-11ec-9863-558fe90a2da1',panelIndex:'4b0d22a8-98e4-4b1f-921e-dd0ea0f492a1',title:'Bad%20Attempt%E9%80%A3%E7%B7%9A%E5%A0%B1%E5%91%8A',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'6.Bad%20Attempt%E9%80%A3%E7%B7%9A%E5%A0%B1%E5%91%8A',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0906"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/c27a2010-586b-47f8-bea7-bb04c1001ffb?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0906"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/b40d82f0-0fbe-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'4b0d22a8-98e4-4b1f-921e-dd0ea0f492a1',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'4b0d22a8-98e4-4b1f-921e-dd0ea0f492a1',w:24,x:0,y:0),id:'84fb8d50-0e55-11ec-9863-558fe90a2da1',panelIndex:'4b0d22a8-98e4-4b1f-921e-dd0ea0f492a1',title:'Bad%20Attempt%E9%80%A3%E7%B7%9A%E5%A0%B1%E5%91%8A',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'6.Bad%20Attempt%E9%80%A3%E7%B7%9A%E5%A0%B1%E5%91%8A',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0906"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0907"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/ee42f090-0fbe-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'838aa258-9699-4b97-988c-fb17d8a2ccb5',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'838aa258-9699-4b97-988c-fb17d8a2ccb5',w:24,x:0,y:0),id:'1e227860-0e54-11ec-9863-558fe90a2da1',panelIndex:'838aa258-9699-4b97-988c-fb17d8a2ccb5',title:'Clients%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'4.Clients%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0907"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/9411a6c8-7443-4e67-85e7-8b261f19221b?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0907"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/ee42f090-0fbe-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'838aa258-9699-4b97-988c-fb17d8a2ccb5',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'838aa258-9699-4b97-988c-fb17d8a2ccb5',w:24,x:0,y:0),id:'1e227860-0e54-11ec-9863-558fe90a2da1',panelIndex:'838aa258-9699-4b97-988c-fb17d8a2ccb5',title:'Clients%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'4.Clients%20%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0907"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0908"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/2ad735c0-0fbf-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'108dbf93-a705-4539-94c5-67c2a267f93a',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'108dbf93-a705-4539-94c5-67c2a267f93a',w:24,x:0,y:0),id:'959626d0-0e54-11ec-9863-558fe90a2da1',panelIndex:'108dbf93-a705-4539-94c5-67c2a267f93a',title:'Client%20-%20API%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'5.Client%20-%20API%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0908"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/25735b48-463f-4aab-b9dd-54710496d2e0?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0908"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/2ad735c0-0fbf-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'108dbf93-a705-4539-94c5-67c2a267f93a',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'108dbf93-a705-4539-94c5-67c2a267f93a',w:24,x:0,y:0),id:'959626d0-0e54-11ec-9863-558fe90a2da1',panelIndex:'108dbf93-a705-4539-94c5-67c2a267f93a',title:'Client%20-%20API%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'5.Client%20-%20API%E4%BD%BF%E7%94%A8%E6%AC%A1%E6%95%B8%E7%B5%B1%E8%A8%88',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0908"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0909"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/7e49aad0-0fbf-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:c811c6f0-ccce-457a-a097-33ef2859e99b,filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:c811c6f0-ccce-457a-a097-33ef2859e99b,w:24,x:0,y:0),id:a7719600-0e59-11ec-9863-558fe90a2da1,panelIndex:c811c6f0-ccce-457a-a097-33ef2859e99b,title:%E4%BA%A4%E6%98%93%E5%AE%8C%E6%95%B4%E6%80%A7%E5%A0%B1%E8%A1%A8,type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'9.%20%E4%BA%A4%E6%98%93%E5%AE%8C%E6%95%B4%E6%80%A7%E5%A0%B1%E8%A1%A8',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0909"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/bd0d57fa-7a66-4202-b2b3-fb08ed3e8ed5?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0909"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/7e49aad0-0fbf-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:c811c6f0-ccce-457a-a097-33ef2859e99b,filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:c811c6f0-ccce-457a-a097-33ef2859e99b,w:24,x:0,y:0),id:a7719600-0e59-11ec-9863-558fe90a2da1,panelIndex:c811c6f0-ccce-457a-a097-33ef2859e99b,title:%E4%BA%A4%E6%98%93%E5%AE%8C%E6%95%B4%E6%80%A7%E5%A0%B1%E8%A1%A8,type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'9.%20%E4%BA%A4%E6%98%93%E5%AE%8C%E6%95%B4%E6%80%A7%E5%A0%B1%E8%A1%A8',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0909"),(timeRange = "Y"),(reportUrl = ""));
        	
        	createTsmpReportUrl((reportId = "AC0910"),(timeRange = "M"),(reportUrl = "/kibana/app/dashboards#/view/bdbd4230-0fbf-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2FM,to:now%2FM))&_a=(description:'',expandedPanelId:'8ecc9653-6585-4cf3-8a8c-e2f1a95ccafd',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'8ecc9653-6585-4cf3-8a8c-e2f1a95ccafd',w:24,x:0,y:0),id:'5ca024c0-0e59-11ec-9863-558fe90a2da1',panelIndex:'8ecc9653-6585-4cf3-8a8c-e2f1a95ccafd',title:'API%E5%9B%9E%E6%87%89%E6%99%82%E9%96%93(Max%2FMin)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'8.API%E5%9B%9E%E6%87%89%E6%99%82%E9%96%93(Max%2FMin)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0910"),(timeRange = "T"),(reportUrl = "/kibana/app/dashboards#/view/39b71824-3c36-4c5b-9b30-a71c5c6ddfd9?embed=true&_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cto%3Anow%2Fd))&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0910"),(timeRange = "W"),(reportUrl = "/kibana/app/dashboards#/view/bdbd4230-0fbf-11ec-9863-558fe90a2da1?embed=true&_g=(filters:!(),refreshInterval:(pause:!f,value:10000),time:(from:now%2Fw,to:now%2Fw))&_a=(description:'',expandedPanelId:'8ecc9653-6585-4cf3-8a8c-e2f1a95ccafd',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(enhancements:(),hidePanelTitles:!f),gridData:(h:15,i:'8ecc9653-6585-4cf3-8a8c-e2f1a95ccafd',w:24,x:0,y:0),id:'5ca024c0-0e59-11ec-9863-558fe90a2da1',panelIndex:'8ecc9653-6585-4cf3-8a8c-e2f1a95ccafd',title:'API%E5%9B%9E%E6%87%89%E6%99%82%E9%96%93(Max%2FMin)',type:visualization,version:'7.13.4')),query:(language:kuery,query:''),tags:!(),timeRestore:!f,title:'8.API%E5%9B%9E%E6%87%89%E6%99%82%E9%96%93(Max%2FMin)',viewMode:view)&show-time-filter=true"));
        	createTsmpReportUrl((reportId = "AC0910"),(timeRange = "Y"),(reportUrl = ""));
        	
		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
    	return tsmpReportUrlList;
	}
    
	protected void createTsmpReportUrl(String reportId, String timeRange, String reportUrl) {
			TsmpReportUrlVo tsmpReportUrl = new TsmpReportUrlVo();
			tsmpReportUrl.setReportId(reportId);
			tsmpReportUrl.setTimeRange(timeRange);
			tsmpReportUrl.setReportUrl(reportUrl);	
			tsmpReportUrlList.add(tsmpReportUrl);
		
	}
	
  
}
