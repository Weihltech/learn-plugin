package com.vsoontech.plugin.apigenerate.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vsoontech.plugin.apigenerate.ApiConfig;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.entity.EntityClass;
import com.vsoontech.plugin.apigenerate.entity.EntityField;
import com.vsoontech.plugin.apigenerate.entity.EntityField.FieldLink;
import com.vsoontech.plugin.apigenerate.entity.FieldType;
import com.vsoontech.plugin.apigenerate.utils.HumpHelper;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

// 分析缓存Json文件，收集域名、字段、内部类等基础信息
class ClassParseManager {


    private ArrayList<ApiDetail> illegalApiDetail;

    public interface CallBack {

        void illegalApi(ArrayList<ApiDetail> illegalApiDetails);

        void onResult(ArrayList<ApiDetail> apiDetails);
    }

    ClassParseManager() {
        illegalApiDetail = new ArrayList<>();
    }

    void parse(ArrayList<File> apiDetailList, CallBack callBack) {
        if (apiDetailList != null
            && !apiDetailList.isEmpty()) {
            try {
                ArrayList<ApiDetail> apiDetails = new ArrayList<>();
                for (File file : apiDetailList) {
                    if (file != null && file.exists()) {
                        ApiDetail apiDetail = parseJsonFile(file);
                        if (apiDetail != null) {
                            apiDetails.add(apiDetail);
                        }
                    }
                }

                if (callBack != null) {
                    callBack.illegalApi(illegalApiDetail);
                    callBack.onResult(apiDetails);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ApiDetail parseJsonFile(File file) throws IOException {
        String jsonStr = FileUtils.readFileToString(file, Charsets.UTF_8);
        JsonObject apiDetailJsonObj = (JsonObject) new JsonParser().parse(jsonStr);
        if (apiDetailJsonObj != null
            && apiDetailJsonObj.get("desc") != null) {
            ApiDetail apiDetail = new ApiDetail();
            apiDetail.group = file.getParentFile().getName();
            collectApiDetailInfo(apiDetail, apiDetailJsonObj);
            apiDetail.desc = file.getParentFile().getName() + " - " + apiDetail.desc;

            if (apiDetail.isIllegal()) {
                illegalApiDetail.add(apiDetail);
                return null;
            }

            String className = convertClassName(apiDetail.httpUrl);
            JsonObject paramsJsonObj = apiDetailJsonObj.getAsJsonObject("params");
            if (paramsJsonObj != null) {
                apiDetail.reqEntityCls = new EntityClass(className + ApiConfig.REQ);
                apiDetail.reqEntityCls.classDesc = apiDetail.desc;

                JsonElement paramsJsonElement = paramsJsonObj.get("obj");
                if (paramsJsonElement.isJsonArray()) {
                    JsonArray fieldArr = paramsJsonElement.getAsJsonArray();
                    // 先创建 参数 内部对象，再收集可用内部类
                    if (fieldArr != null && fieldArr.size() > 0) {
                        EntityClass reqInnerCls = new EntityClass("Params");
                        collectFieldsAndInnerClass(apiDetail.reqEntityCls, reqInnerCls, apiDetailJsonObj,
                            paramsJsonObj, "obj");
                        apiDetail.reqEntityCls.getInnerClss().add(reqInnerCls);
                    }
                }
            }

            JsonObject responseJsonObj = apiDetailJsonObj.getAsJsonObject("response");
            if (responseJsonObj != null) {
                apiDetail.respEntityCls = new EntityClass(className + ApiConfig.RESP);
                apiDetail.respEntityCls.classDesc = apiDetail.desc;
                collectFieldsAndInnerClass(apiDetail.respEntityCls, apiDetail.respEntityCls, apiDetailJsonObj,
                    responseJsonObj, "obj");
            }

            return apiDetail;
        }
        return null;
    }

//    private void collectApiDetailConstsInfo(ApiDetail apiDetail, JsonObject apiDetailJsonObj) {
//
//        Iterator<String> keySet = apiDetailJsonObj.keySet().iterator();
//        while (keySet.hasNext()) {
//            String key = keySet.next();
//            if (!isEmpty(key) && key.startsWith("enum")) {
//                JsonElement constsJsonElement = apiDetailJsonObj.get(key);
//                if (constsJsonElement.isJsonArray()) {
//                    JsonArray fieldArr = constsJsonElement.getAsJsonArray();
//                    if (fieldArr != null && fieldArr.size() > 0) {
//                        apiDetail.constsFileds = new ArrayList<>();
//                        for (JsonElement fieldJson : fieldArr) {
//                            if (fieldJson.isJsonObject()) {
//                                EntityConsts entityConsts = new EntityConsts();
//                                JsonObject fieldJsonObj = fieldJson.getAsJsonObject();
//                                entityConsts.desc = getString(fieldJsonObj, "desc");
//                                entityConsts.name = getString(fieldJsonObj, "name");
//                                entityConsts.type = getString(fieldJsonObj, "type");
//                                entityConsts.value = getString(fieldJsonObj, "value");
//                                apiDetail.constsFileds.add(entityConsts);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//    }

    private String convertClassName(String httpUrl) {
        httpUrl = httpUrl.substring(
            httpUrl.lastIndexOf("/"))
            .replace("/", "_");
        return HumpHelper.lineToHump(httpUrl);
    }

    private void collectFieldsAndInnerClass(EntityClass sourceEntityClass, EntityClass newEntityClass,
        JsonObject sourceJsonObj, JsonObject targetJsonObj,
        String target) {
        JsonElement targetJsonElement = targetJsonObj.get(target);
        if (targetJsonElement.isJsonArray()) {
            JsonArray fieldArr = targetJsonElement.getAsJsonArray();
            if (fieldArr != null && fieldArr.size() > 0) {
                for (JsonElement fieldJson : fieldArr) {
                    EntityField reqInnerClsField = collectFields(sourceJsonObj, fieldJson);
                    if (reqInnerClsField != null) {
                        newEntityClass.getFields().add(reqInnerClsField);
                        // 收集 内部类 的 target 类
                        if ((reqInnerClsField.type.isObj() || reqInnerClsField.type.isArrayObj())
                            && !isEmpty(reqInnerClsField.target)) {
                            String targetName = HumpHelper.convertTargetName(reqInnerClsField.target);
                            if (!hasTargetEntityClass(sourceEntityClass.getInnerClss(), targetName)) {
                                EntityClass entityClass = new EntityClass(targetName);
                                collectFieldsAndInnerClass(sourceEntityClass, entityClass, sourceJsonObj, sourceJsonObj,
                                    reqInnerClsField.target);
                                sourceEntityClass.getInnerClss().add(entityClass);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean hasTargetEntityClass(ArrayList<EntityClass> innerClss, String target) {
        for (EntityClass cls : innerClss) {
            if (cls.className.toLowerCase().equals(target.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private EntityField collectFields(JsonObject sourceJsonObj, JsonElement fieldJson) {
        JsonObject jsonObject = fieldJson.getAsJsonObject();
        if (jsonObject != null) {
            EntityField entityField = new EntityField();
            entityField.desc = getString(jsonObject, "desc");
            entityField.name = getString(jsonObject, "name");
            entityField.notNull = getBoolean(jsonObject, "notNull");
            entityField.target = getString(jsonObject, "target");
            entityField.type = new FieldType(getString(jsonObject, "type"));

            // links
            if (entityField.type.isEnum()) {
                entityField.links = new ArrayList<>();
                String targetName = "enum" + HumpHelper.lineToHump("_" + entityField.name);
                JsonElement targetJsonElement = sourceJsonObj.get(targetName);
                if (targetJsonElement != null && targetJsonElement.isJsonArray()) {
                    JsonArray fieldLinks = (JsonArray) targetJsonElement;
                    for (JsonElement linkObj : fieldLinks) {
                        if (linkObj.isJsonObject()) {
                            JsonObject linkJsonObj = linkObj.getAsJsonObject();
                            FieldLink fieldLink = new FieldLink();
                            fieldLink.desc = getString(linkJsonObj, "desc");
                            fieldLink.name = getString(linkJsonObj, "name");
                            fieldLink.type = getString(linkJsonObj, "type");
                            fieldLink.value = getString(linkJsonObj, "value");
                            entityField.links.add(fieldLink);
                        }
                    }
                }
            }

            return entityField;
        }
        return null;
    }

    private void collectApiDetailInfo(ApiDetail apiDetail, JsonObject apiDetailJsonObj) {
        apiDetail.desc = getString(apiDetailJsonObj, "desc");
        apiDetail.domain = getString(apiDetailJsonObj, "domain");
        apiDetail.httpType = getString(apiDetailJsonObj, "httpType");
        apiDetail.httpUrl = getString(apiDetailJsonObj, "httpUrl");
        apiDetail.version = getInt(apiDetailJsonObj, "version");
    }

    private boolean getBoolean(JsonObject jsonObject, String name) {
        if (jsonObject.get(name) != null) {
            return jsonObject.get(name).getAsBoolean();
        }
        return false;
    }

    private int getInt(JsonObject jsonObject, String name) {
        if (jsonObject.get(name) != null) {
            return jsonObject.get(name).getAsInt();
        }
        return 0;
    }

    private String getString(JsonObject jsonObject, String name) {
        if (jsonObject.get(name) != null) {
            return jsonObject.get(name).getAsString();
        }
        return "";
    }

    private boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
        }
    }
}
