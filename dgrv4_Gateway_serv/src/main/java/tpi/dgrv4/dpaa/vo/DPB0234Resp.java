package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0234Resp {
    private List<DPB0234RespItem> dataList;
    private List<DPB0234RespItemFromXapiKey> dataListFromXapiKey;
    private String totalApi;

    public List<DPB0234RespItem> getDataList() {
        return dataList;
    }

    public void setDataList(List<DPB0234RespItem> dataList) {
        this.dataList = dataList;
    }

    public List<DPB0234RespItemFromXapiKey> getDataListFromXapiKey() {
        return dataListFromXapiKey;
    }

    public void setDataListFromXapiKey(List<DPB0234RespItemFromXapiKey> dataListFromXapiKey) {
        this.dataListFromXapiKey = dataListFromXapiKey;
    }

    public String getTotalApi() {
        return totalApi;
    }

    public void setTotalApi(String totalApi) {
        this.totalApi = totalApi;
    }

    @Override
    public String toString() {
        return "DPB0234Resp{" +
                "dataList=" + dataList +
                ", dataListFromXapiKey=" + dataListFromXapiKey +
                ", totalApi=" + totalApi +
                '}';
    }
}
