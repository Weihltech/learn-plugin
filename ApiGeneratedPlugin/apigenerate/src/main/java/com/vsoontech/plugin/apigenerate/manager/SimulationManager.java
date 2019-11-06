package com.vsoontech.plugin.apigenerate.manager;

import static org.apache.http.util.TextUtils.isEmpty;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vsoontech.plugin.apigenerate.ApiConfig;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.entity.EntityClass;
import com.vsoontech.plugin.apigenerate.entity.EntityField;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

class SimulationManager {

    private File assetsFileDir;
    private HashMap<String, File> assetsJsonFiles;
    private int tValue;

    SimulationManager() {
        assetsJsonFiles = new HashMap<>();

        // 创建 assets 文件夹
        // D:\Android\workspace\OwlClass\app\src\main\assets
        assetsFileDir = new File(ApiConfig.assetsDir);
        if (!assetsFileDir.exists() && assetsFileDir.mkdirs()) {
            Logc.d(assetsFileDir.getAbsolutePath());
        }

        loadAssetsJsonFiles();

    }

    void logStart() {
        if (openLog()) {
            Logc.d("");
            Logc.d("@ SimulationManager <-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-");
        }
    }

    private void loadAssetsJsonFiles() {
        // 获取 assets 文件夹下所有 json 文件
        File[] files = assetsFileDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().endsWith(".json")) {
                    assetsJsonFiles.put(file.getName().replace(".json", ""), file);
                }
            }
        }
    }


    void generate(ApiDetail apiDetail) throws IOException {

        if (apiDetail != null
            && apiDetail.reqEntityCls != null
            && !isEmpty(apiDetail.reqEntityCls.className)) {
            String reqName = apiDetail.reqEntityCls.className.toLowerCase();
            if (assetsJsonFiles.containsKey(reqName)) {
                if (openLog()) {
                    Logc.d("Has [ " + reqName + " ] Simulation Data !");
                }
            } else {
                if (openLog()) {
                    Logc.d("New [ " + reqName + " ] Simulation Data !");
                }
                File jsonFile = new File(assetsFileDir, reqName + ".json");
                String jsonContent = createJsonContent(apiDetail.respEntityCls);
                FileUtils.writeStringToFile(jsonFile, jsonContent, Charsets.UTF_8, false);
            }
        }

    }

    private String createJsonContent(EntityClass respEntityCls) {
        if (respEntityCls != null) {
            JsonObject jsonObject = new JsonObject();
            collectFields(jsonObject, respEntityCls, respEntityCls);
            return jsonObject.toString();
        }
        return "Resp Is Null !";
    }

    private void collectFields(JsonObject jsonObject, EntityClass respEntityCls, EntityClass targetEntityCls) {
        if (targetEntityCls != null
            && targetEntityCls.getFields() != null
            && !targetEntityCls.getFields().isEmpty()) {
            for (EntityField field : targetEntityCls.getFields()) {
                if (field.type.isArrayFloat()) {
                    JsonArray subArray = new JsonArray();
                    for (int i = 0; i < 3; i++) {
                        // 数组默认填充三个
                        subArray.add((float) i + 0.1);
                    }
                    jsonObject.add(field.name, subArray);
                } else if (field.type.isArrayInteger()) {
                    JsonArray subArray = new JsonArray();
                    for (int i = 0; i < 3; i++) {
                        // 数组默认填充三个
                        subArray.add(i);
                    }
                    jsonObject.add(field.name, subArray);
                } else if (field.type.isArrayString()) {
                    JsonArray subArray = new JsonArray();
                    for (int i = 0; i < 3; i++) {
                        // 数组默认填充三个
                        subArray.add(i + "_" + field.name);
                    }
                    jsonObject.add(field.name, subArray);
                } else if (field.type.isArrayObj()) {
                    JsonArray subArray = new JsonArray();
                    JsonObject subObj = new JsonObject();
                    EntityClass findTargetECls = findTargetEntityCls(field.target, respEntityCls);

                    collectFields(subObj, respEntityCls, findTargetECls);
                    for (int i = 0; i < 3; i++) {
                        // 数组默认填充三个
                        subArray.add(subObj);
                    }
                    jsonObject.add(field.name, subArray);
                } else if (field.type.isObj()) {
                    JsonObject subObj = new JsonObject();
                    collectFields(subObj, respEntityCls, findTargetEntityCls(field.target, respEntityCls));
                    jsonObject.add(field.name, subObj);
                } else if (field.type.isNumber()) {
                    tValue++;
                    jsonObject.addProperty(field.name, tValue);
                } else if (field.type.isString()) {
                    jsonObject.addProperty(field.name, field.desc + "-" + tValue);
                } else if (field.type.isBoolean()) {
                    jsonObject.addProperty(field.name, (tValue % 3 == 0));
                }

            }
        }
    }

    private EntityClass findTargetEntityCls(String target, EntityClass respEntityCls) {
        if (!isEmpty(target)
            && respEntityCls != null
            && respEntityCls.getInnerClss() != null
            && !respEntityCls.getInnerClss().isEmpty()) {
            for (EntityClass cls : respEntityCls.getInnerClss()) {
                if (target.toLowerCase().equals(cls.className.toLowerCase())) {
                    return cls;
                }
            }
        }
        return null;
    }

    private boolean openLog() {
        return ApiConfig.sProp == null || ApiConfig.sProp.openLog || ApiConfig.sProp.openSimulationLog;
    }
}
