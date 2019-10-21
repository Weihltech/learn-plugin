package com.vsoontech.plugin.apigenerate.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.entity.EntityClass;
import com.vsoontech.plugin.apigenerate.entity.EntityField;
import com.vsoontech.plugin.apigenerate.entity.FieldType;
import com.vsoontech.plugin.apigenerate.utils.HumpHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

class ClassParseManager {

    public interface CallBack {

        void onResult(ArrayList<ApiDetail> apiDetails);
    }

    public ClassParseManager() {
    }

    // 暂时没有做所有对比操作，只判断了数量、版本号
    private boolean needCreateUpdate(ArrayList<File> apiDetailList) {
//        File[] subFiles = mJavaFileOutputDir.listFiles();
//        return !(subFiles != null && apiDetailList.size() == subFiles.length);
        return true;
    }

    private String subEndString(String s, String content) {
        return content.substring(content.lastIndexOf(s), content.length()).replace(s, "_");
    }

    public void run(ArrayList<File> apiDetailList, CallBack callBack) {
        if (needCreateUpdate(apiDetailList) && apiDetailList != null && !apiDetailList.isEmpty()) {
            try {
                ArrayList<ApiDetail> apiDetails = new ArrayList<>();
                for (File file : apiDetailList) {
                    if (file != null && file.exists()) {
                        ApiDetail apiDetail = analyJsonFile(file);
                        if (apiDetail != null) {
                            apiDetails.add(apiDetail);
                        }
                    }
                }

                if (callBack != null) {
                    callBack.onResult(apiDetails);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ApiDetail analyJsonFile(File file) throws IOException {
        String jsonStr = FileUtils.readFileToString(file, Charsets.UTF_8);
        JsonObject apiDetailJsonObj = (JsonObject) new JsonParser().parse(jsonStr);
        if (apiDetailJsonObj != null
            && apiDetailJsonObj.get("desc") != null) {
            ApiDetail apiDetail = new ApiDetail();
            collectApiDeatilInfo(apiDetail, apiDetailJsonObj);
            String className = HumpHelper.lineToHump(subEndString("/", apiDetail.httpUrl));

            JsonObject paramsObj = apiDetailJsonObj.getAsJsonObject("params");
            if (paramsObj != null) {
                apiDetail.reqEntityCls = new EntityClass(className + "Req");
                apiDetail.reqEntityCls.classDesc = apiDetail.desc;
                JsonArray fieldArr = paramsObj.getAsJsonArray("obj");
                // Req 参数不会有嵌套 ？ ; 如果参数不为空，则创建内部类
                if (fieldArr != null && fieldArr.size() > 0) {
                    EntityClass reqInnerCls = new EntityClass("Params");
                    for (JsonElement fieldJson : fieldArr) {
                        EntityField reqInnerClsFields = collectFields(fieldJson);
                        if (reqInnerClsFields != null) {
                            reqInnerCls.getFields().add(reqInnerClsFields);
                        }
                    }
                    apiDetail.reqEntityCls.getInnerClss().add(reqInnerCls);
                }
            }

            JsonObject respTargetJsonObj = apiDetailJsonObj.getAsJsonObject("response");
            if (respTargetJsonObj != null) {
                apiDetail.respEntityCls = new EntityClass(className + "Resp");
                apiDetail.respEntityCls.classDesc = apiDetail.desc;
                // 这里做递归读取 target - obj;
                collectFieldsAndInnerClass(apiDetail.respEntityCls, apiDetail.respEntityCls, apiDetailJsonObj,
                    respTargetJsonObj, "obj");
            }
            return apiDetail;
        }
        return null;
    }

    private void collectFieldsAndInnerClass(EntityClass sourceEntityClass, EntityClass newEnityClass,
        JsonObject sourceJsonObj, JsonObject targetJsonObj,
        String target) {
        JsonArray fieldArr = targetJsonObj.getAsJsonArray(target);
        if (fieldArr != null && fieldArr.size() > 0) {
            for (JsonElement fieldJson : fieldArr) {
                EntityField reqInnerClsField = collectFields(fieldJson);
                if (reqInnerClsField != null) {
                    newEnityClass.getFields().add(reqInnerClsField);
                    // 收集 内部类 的 target 类
                    if (reqInnerClsField.type.isObj()
                        && !isEmpty(reqInnerClsField.target)
                        && !hasTargetEntityClass(sourceEntityClass.getInnerClss(), reqInnerClsField.target)) {
                        EntityClass entityClass = new EntityClass(HumpHelper.lineToHump("_" + reqInnerClsField.target));
                        collectFieldsAndInnerClass(sourceEntityClass, entityClass, sourceJsonObj, sourceJsonObj,
                            reqInnerClsField.target);
                        sourceEntityClass.getInnerClss().add(entityClass);
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

    private EntityField collectFields(JsonElement fieldJson) {
        JsonObject jsonObject = fieldJson.getAsJsonObject();
        if (jsonObject != null) {
            EntityField entityField = new EntityField();
            entityField.desc = getString(jsonObject, "desc");
            entityField.name = getString(jsonObject, "name");
            entityField.notNull = getBoolean(jsonObject, "notNull");
            entityField.target = getString(jsonObject, "target");
            entityField.type = new FieldType(getString(jsonObject, "type"));
            return entityField;
        }
        return null;
    }

    private void collectApiDeatilInfo(ApiDetail apiDetail, JsonObject apiDetailJsonObj) {
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
