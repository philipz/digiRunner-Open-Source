package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0234RespItemFromXapiKey {
    private String apiKeyId;
    private String apiKeyMask;
    private String apiKeyAlias;
    private String effectiveAt;
    private String expiredAt;
    private List<DPB0234ClientDataItem> clientDataList;

    public String getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(String apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getApiKeyMask() {
        return apiKeyMask;
    }

    public void setApiKeyMask(String apiKeyMask) {
        this.apiKeyMask = apiKeyMask;
    }

    public String getApiKeyAlias() {
        return apiKeyAlias;
    }

    public void setApiKeyAlias(String apiKeyAlias) {
        this.apiKeyAlias = apiKeyAlias;
    }

    public String getEffectiveAt() {
        return effectiveAt;
    }

    public void setEffectiveAt(String effectiveAt) {
        this.effectiveAt = effectiveAt;
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expiredAt = expiredAt;
    }

    public List<DPB0234ClientDataItem> getClientDataList() {
        return clientDataList;
    }

    public void setClientDataList(List<DPB0234ClientDataItem> clientDataList) {
        this.clientDataList = clientDataList;
    }

    @Override
    public String toString() {
        return "DPB0234RespItemFromXapiKey{" +
                "apiKeyId='" + apiKeyId + '\'' +
                ", apiKeyMask='" + apiKeyMask + '\'' +
                ", apiKeyAlias='" + apiKeyAlias + '\'' +
                ", effectiveAt='" + effectiveAt + '\'' +
                ", expiredAt='" + expiredAt + '\'' +
                ", clientDataList=" + clientDataList +
                '}';
    }
}
