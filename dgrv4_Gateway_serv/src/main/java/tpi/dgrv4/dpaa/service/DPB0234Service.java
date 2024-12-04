package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.*;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.*;

@Service
public class DPB0234Service {
    private TPILogger logger = TPILogger.tl;

    @Autowired
    private DgrXApiKeyDao dgrXApiKeyDao;
    @Autowired
    private DgrXApiKeyMapDao dgrXApiKeyMapDao;
    @Autowired
    private TsmpClientDao tsmpClientDao;
    @Autowired
    private TsmpClientGroupDao tsmpClientGroupDao;
    @Autowired
    private TsmpGroupDao tsmpGroupDao;
    @Autowired
    private TsmpGroupApiDao tsmpGroupApiDao;
    @Autowired
    private TsmpApiDao tsmpApiDao;
    private static final String FLAG_KEYWORDS = "keyWords";
    private static final String FLAG_X_API_KEY = "xApiKey";

    public static List<String> safelyConvertToStringList(Object obj) {
        if (obj instanceof List<?>) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .toList();
        }
        throw new IllegalArgumentException("傳入的物件不是 List 類型");
    }

    public DPB0234Resp getApiStatusByGroup(TsmpAuthorization authorization, DPB0234Req req) {
        String flag = req.getFlag();

        // 檢查flag值是否合法
        if (!FLAG_KEYWORDS.equals(flag) && !FLAG_X_API_KEY.equals(flag)) {
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        // 空值檢查
        if (FLAG_KEYWORDS.equals(flag)) {
            String keyWords = req.getKeyWords();
            if (keyWords == null || keyWords.trim().isEmpty()) {
                throw TsmpDpAaRtnCode._2025.throwing(FLAG_KEYWORDS);
            }
        } else if (FLAG_X_API_KEY.equals(flag)) {
            String xApiKey = req.getxApiKey();
            if (xApiKey == null || xApiKey.trim().isEmpty()) {
                throw TsmpDpAaRtnCode._2025.throwing(FLAG_X_API_KEY);
            }
        }

        String xApiKey = null;
        DPB0234Resp resp = null;

        switch (flag) {
            case FLAG_KEYWORDS:
                Object obj = this.parseString(req.getKeyWords());
                //判斷單值或多值
                if (obj instanceof String) {
                    // getByOneKeyWord
                    List<DPB0234RespItem> dataList = this.getApiStatusByOneKeyWord(String.valueOf(obj));
                    if (!dataList.isEmpty()) {
                        resp = new DPB0234Resp();
                        resp.setDataList(dataList);
                        int totalApis = this.calculateTotalApis(resp);
                        resp.setTotalApi(Integer.toString(totalApis));
                    } else {
                        throw TsmpDpAaRtnCode._1298.throwing();
                    }
                } else if (obj instanceof List) {
                    // getByManyKeyWords
                    List<String> keyWordsList = safelyConvertToStringList(obj);
                    List<DPB0234RespItem> resultList = new ArrayList<>();
                    keyWordsList.forEach(keyword -> resultList.addAll(this.getApiStatusByOneKeyWord(keyword)));

                    if (!resultList.isEmpty()) {
                        resp = new DPB0234Resp();
                        resp.setDataList(resultList);
                        int totalApis = this.calculateTotalApis(resp);
                        resp.setTotalApi(Integer.toString(totalApis));
                    } else {
                        throw TsmpDpAaRtnCode._1298.throwing();
                    }
                }
                break;
            case FLAG_X_API_KEY:
                //getBy xApiKey
                xApiKey = req.getxApiKey().trim();
                List<DPB0234RespItemFromXapiKey> dataListFromXapiKey = this.getGroupAndApiStatusByXApiKey(xApiKey);
                if (!dataListFromXapiKey.isEmpty()) {
                    resp = new DPB0234Resp();
                    resp.setDataListFromXapiKey(dataListFromXapiKey);
                    int totalApis = this.calculateTotalApis(resp);
                    resp.setTotalApi(Integer.toString(totalApis));
                } else {
                    throw TsmpDpAaRtnCode._1298.throwing();
                }
                break;
        }

        return resp;
    }

    public List<DPB0234RespItem> getApiStatusByOneKeyWord(String keyWord) {
        List<TsmpGroup> groupList = getGroupDao().
                findByGroupIdContainingOrGroupNameContainingOrGroupAliasContaining(keyWord, keyWord, keyWord);
        if (groupList.isEmpty())
            return Collections.emptyList();

        // 將所有GroupID進行查詢，傳回 1~多筆結果
        try {
            // 將多個 group 分別查詢
            List<DPB0234RespItem> dataList = new ArrayList<>();
            groupList.forEach(groupItem -> {
                DPB0234RespItem item = this.getOneApiStatusByGroupId(groupItem);
                dataList.add(item);
            });
            return dataList;
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    public DPB0234RespItem getOneApiStatusByGroupId(TsmpGroup group) {
        DPB0234RespItem respItem = null;
        try {
            String groupId = group.getGroupId();
            //使用 GROUP_ID 取 CLIENT_GROUP
            List<TsmpClientGroup> clientGroupList = getTsmpClientGroupDao().findByGroupId(groupId);

            //使用 CLIENT_GROUP 取 CLIENT
            List<TsmpClient> clientList = clientGroupList.stream()
                    .map(client -> getTsmpClientDao().findById(client.getClientId()))
                    .map(optional -> optional.orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            //使用 GROUP_ID 取其API
            List<TsmpApi> tsmpApiList = getTsmpApiDao().queryByTsmpGroupAPiGroupId(groupId);

            // apiDataList for DPB0234ClientDataItem
            List<DPB0234ApiDataItem> apiDataList = new ArrayList<>();
            tsmpApiList.forEach(tsmpApiElement -> {
                DPB0234ApiDataItem item = new DPB0234ApiDataItem();
                item.setApiName(tsmpApiElement.getApiName());
                item.setApiStatus(tsmpApiElement.getApiStatus());
                item.setApiPath(tsmpApiElement.getApiKey());
                apiDataList.add(item);
            });

            // clientDataList for DPB0234RespItem
            List<DPB0234ClientDataItem> clientDataList = new ArrayList<>();
            clientList.forEach(client -> {
                DPB0234ClientDataItem item = new DPB0234ClientDataItem();
                item.setClientId(client.getClientId());
                item.setClientName(client.getClientName());
                item.setApiDataList(new ArrayList<>(apiDataList));
                clientDataList.add(item);
            });

            // DPB0234RespItem
            respItem = new DPB0234RespItem();
            respItem.setGroupId(group.getGroupId());
            respItem.setGroupAlias(group.getGroupAlias());
            respItem.setGroupName(group.getGroupName());
            respItem.setClientDataList(clientDataList);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        return respItem;
    }

    public List<DPB0234RespItemFromXapiKey> getGroupAndApiStatusByXApiKey(String xApiKey) {
        try {
            List<DPB0234RespItemFromXapiKey> resultList = new ArrayList<>();

            // 檢查xApiKey合法性
            String xApiKeyEn = TokenHelper.getXApiKeyEn(xApiKey);
            DgrXApiKey dgrXApiKey = this.getDgrXApiKeyDao().findFirstByApiKeyEn(xApiKeyEn);

            if (dgrXApiKey == null) {
                return resultList; // 返回空列表
            }

            // 透過Map取xApiKey與Group的關聯
            Long apiKeyId = dgrXApiKey.getApiKeyId();
            List<DgrXApiKeyMap> xApiKeyMapList = this.getDgrXApiKeyMapDao()
                    .findByRefApiKeyId(apiKeyId);

            // 取屬於xApiKey所有對應的Group
            List<TsmpGroup> groupList = xApiKeyMapList.stream()
                    .map(xApiKeyMap -> getGroupDao().findById(xApiKeyMap.getGroupId()))
                    .flatMap(Optional::stream)
                    .distinct() // 確保Group不重複
                    .toList();

            // 為每個Group建立一個回應項目
            for (TsmpGroup group : groupList) {
                DPB0234RespItemFromXapiKey respItem = new DPB0234RespItemFromXapiKey();

                // 設置基本的XApiKey資訊
                respItem.setApiKeyId(dgrXApiKey.getApiKeyId().toString());
                respItem.setApiKeyMask(dgrXApiKey.getApiKeyMask());
                respItem.setApiKeyAlias(dgrXApiKey.getApiKeyAlias());
                respItem.setEffectiveAt(dgrXApiKey.getEffectiveAt().toString());
                respItem.setExpiredAt(dgrXApiKey.getExpiredAt().toString());

                // 取得該Group的API列表
                List<TsmpApi> tsmpApiList = getTsmpApiDao()
                        .queryByTsmpGroupAPiGroupId(group.getGroupId())
                        .stream()
                        .distinct() // 確保API不重複
                        .toList();

                // 取得該Group的Client列表
                List<TsmpClient> clientList = getTsmpClientGroupDao()
                        .findByGroupId(group.getGroupId())
                        .stream()
                        .map(clientGroup -> getTsmpClientDao().findById(clientGroup.getClientId()))
                        .flatMap(Optional::stream)
                        .distinct() // 確保Client不重複
                        .toList();

                // 建立API資料列表
                List<DPB0234ApiDataItem> apiDataList = tsmpApiList.stream()
                        .map(api -> {
                            DPB0234ApiDataItem item = new DPB0234ApiDataItem();
                            item.setApiName(api.getApiName());
                            item.setApiStatus(api.getApiStatus());
                            item.setApiPath(api.getApiKey());
                            return item;
                        }).toList();

                // 建立Client資料列表
                List<DPB0234ClientDataItem> clientDataList = clientList.stream()
                        .map(client -> {
                            DPB0234ClientDataItem item = new DPB0234ClientDataItem();
                            item.setApiKeyId(dgrXApiKey.getApiKeyId().toString());
                            item.setApiKeyMask(dgrXApiKey.getApiKeyMask());
                            item.setApiKeyAlias(dgrXApiKey.getApiKeyAlias());
                            item.setClientId(client.getClientId());
                            item.setClientName(client.getClientName());
                            item.setApiDataList(new ArrayList<>(apiDataList));
                            return item;
                        }).toList();

                respItem.setClientDataList(clientDataList);
                resultList.add(respItem);
            }

            return resultList;

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    public Object parseString(String input) {
        // 移除字串開頭和結尾的空白
        input = input.trim();

        // 檢查是否包含空格
        if (!input.contains(" ")) {
            // 如果沒有空格，直接返回原字串
            return input;
        } else {
            // 如果有空格，將字串分割並放入 List<String>
            List<String> data = Arrays.asList(input.split("\\s+"));
            return data;
        }
    }

    public int calculateTotalApis(DPB0234Resp response) {
        int totalApis = 0;

        if (response == null){
            return 0;
        }

        // 處理 dataList
        if (response.getDataList() != null) {
            totalApis += (int) response.getDataList().stream()
                    .flatMap(group -> group.getClientDataList().stream())
                    .mapToLong(client -> client.getApiDataList().size())
                    .sum();
        }

        // 處理 dataListFromXapiKey
        if (response.getDataListFromXapiKey() != null) {
            totalApis += (int) response.getDataListFromXapiKey().stream()
                    .flatMap(xapiKey -> xapiKey.getClientDataList().stream())
                    .mapToLong(client -> client.getApiDataList().size())
                    .sum();
        }

        return totalApis;
    }

    protected DgrXApiKeyDao getDgrXApiKeyDao() {
        return dgrXApiKeyDao;
    }

    protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
        return dgrXApiKeyMapDao;
    }

    protected TsmpClientDao getTsmpClientDao() {
        return tsmpClientDao;
    }

    protected TsmpClientGroupDao getTsmpClientGroupDao() {
        return tsmpClientGroupDao;
    }

    protected TsmpGroupDao getGroupDao() {
        return tsmpGroupDao;
    }

    protected TsmpGroupApiDao getTsmpGroupApiDao() {
        return tsmpGroupApiDao;
    }

    protected TsmpApiDao getTsmpApiDao() {
        return tsmpApiDao;
    }
}
