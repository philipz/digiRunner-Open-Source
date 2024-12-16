package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Objects;

public class DPB0234RespItem {
    private String groupId;
    private String groupAlias;
    private String groupName;
    private List<DPB0234ClientDataItem> clientDataList;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupAlias() {
        return groupAlias;
    }

    public void setGroupAlias(String groupAlias) {
        this.groupAlias = groupAlias;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public List<DPB0234ClientDataItem> getClientDataList() {
        return clientDataList;
    }

    public void setClientDataList(List<DPB0234ClientDataItem> clientDataList) {
        this.clientDataList = clientDataList;
    }

    @Override
    public String toString() {
        return "DPB0234Resp{" +
                "groupId='" + groupId + '\'' +
                ", groupAlias='" + groupAlias + '\'' +
                ", groupName='" + groupName + '\'' +
                ", clientDataList=" + clientDataList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DPB0234RespItem item = (DPB0234RespItem) o;
        return Objects.equals(groupId, item.groupId) && Objects.equals(groupAlias, item.groupAlias) && Objects.equals(groupName, item.groupName) && Objects.equals(clientDataList, item.clientDataList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, groupAlias, groupName, clientDataList);
    }
}
