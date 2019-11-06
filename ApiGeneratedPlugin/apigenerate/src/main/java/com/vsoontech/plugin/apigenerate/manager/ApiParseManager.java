package com.vsoontech.plugin.apigenerate.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vsoontech.plugin.apigenerate.ApiConfig;
import com.vsoontech.plugin.apigenerate.entity.ApiProject;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

/**
 * 解析后台接口文档，并缓存数据
 */
class ApiParseManager {

    private File apiCacheDir;
    private File apiProjectFile;
    private ApiProject apiProject;
    // 需要创建或更新的 ApiDetail
    private ArrayList<File> apiDetailJsonFileList;

    ApiParseManager() {
        apiDetailJsonFileList = new ArrayList<>();
        createApiCacheSourceDir();
    }


    private void createApiCacheSourceDir() {
        // 校验或创建缓存目录
        apiCacheDir = new File(ApiConfig.apiCacheSourceDir);
        if (!apiCacheDir.exists()
            && apiCacheDir.mkdirs()) {
            if (openLog()) {
                Logc.d("Api-Cache-Dir mkdirs success !");
            }
        }
        apiProjectFile = new File(apiCacheDir.getAbsolutePath() + File.separator + ApiConfig.PROJECT);
    }

    // 解析当前文档接口数据文档信息; 并输出缓存数据
    void parse(String contentJson) throws IOException {
        if (openLog()) {
            Logc.d("Parse Api Content Json .");
        }
        JsonObject apiDataJsonObj = (JsonObject) new JsonParser().parse(contentJson);
        if (apiDataJsonObj != null) {
            String version = apiDataJsonObj.get("version").getAsString();
            String desc = apiDataJsonObj.get("desc").getAsString();
            if (!ApiConfig.isEmpty(version)
                && !ApiConfig.isEmpty(desc)) {
                Logc.d("[Clean ApiSource Cache .]  " + apiCacheDir.getAbsolutePath());
                FileUtils.cleanDirectory(apiCacheDir);
                recordApiProject(version, desc);
                JsonArray apiGroupList = apiDataJsonObj.getAsJsonArray("apiGroupList");
                parseApiGroupList(apiGroupList);
            }
        } else {
            throw new RuntimeException("Error Api Content Json .");
        }

        apiDetailJsonFileList.clear();
        loadApiDetailList(apiCacheDir);
    }

    ArrayList<File> loadCacheApiDetailJsonFileList() {
        if (openLog()) {
            Logc.d("Load Cache ApiDetailJsonFileList .");
        }
        if (apiDetailJsonFileList.isEmpty()) {
            loadApiDetailList(apiCacheDir);
        }
        return new ArrayList<>(apiDetailJsonFileList);
    }

    private void loadApiDetailList(File dirFile) {
        File[] tDirFiles = dirFile.listFiles();
        assert tDirFiles != null;
        for (File subFile : tDirFiles) {
            if (subFile.exists()
                && subFile.isFile()
                && subFile.getName().endsWith(ApiConfig.JSON_SUFFIX)
                && !subFile.getName().endsWith(ApiConfig.PROJECT)
                && !subFile.getName().endsWith(ApiConfig.PROJECT_CONTENT)) {
                apiDetailJsonFileList.add(subFile);
                if (openLog()) {
                    Logc.d("+ Load File : " + subFile.getName());
                }
            } else if (subFile.isDirectory()) {
                if (openLog()) {
                    Logc.d("@ Scan Dir : " + subFile.getName() + "  <-<-<-<-<-<-");
                }
                loadApiDetailList(subFile);
            }
        }
    }

    private void parseApiGroupList(JsonArray apiGroupList) throws IOException {
        for (JsonElement jsonElement : apiGroupList) {
            JsonObject apiGroup = jsonElement.getAsJsonObject();
            String desc = apiGroup.get("desc").getAsString();

            File groupFile = new File(apiCacheDir.getAbsoluteFile() + File.separator + desc);
            if (!groupFile.exists() && groupFile.mkdir()) {
                if (openLog()) {
                    Logc.d("# 类别 [New] ：" + desc);
                }
            } else {
                if (openLog()) {
                    Logc.d("# 类别 ：" + desc);
                }
            }

            JsonElement apiDetailListElement = apiGroup.get("apiDetailList");
            if (apiDetailListElement.isJsonArray()) {
                JsonArray apiDetailList = apiDetailListElement.getAsJsonArray();
                parseApiDetailList(groupFile, apiDetailList);
            }
        }
    }

    private void parseApiDetailList(File groupFile, JsonArray apiDetailList) throws IOException {
        for (JsonElement jsonElement : apiDetailList) {
            JsonObject apiDetailJsonObj = jsonElement.getAsJsonObject();
            if (apiDetailJsonObj != null
                && apiDetailJsonObj.get("desc") != null) {
                String desc = apiDetailJsonObj.get("desc").getAsString();
                String httpUrl = apiDetailJsonObj.get("httpUrl").getAsString();
                String reqName = httpUrl.substring(httpUrl.lastIndexOf("/") + 1, httpUrl.length());
                if (openLog()) {
                    Logc.d("Desc ：" + desc);
                    Logc.d("httpUrl ：" + httpUrl + " ; ReqName = " + reqName);
                }

                String jsonData = jsonElement.toString();
                File subFile = new File(groupFile, reqName + ApiConfig.JSON_SUFFIX);
                FileUtils.writeStringToFile(subFile, jsonData, Charsets.UTF_8, false);
            }
        }
    }

    private void recordApiProject(String version, String desc) throws IOException {
        if (apiProjectFile != null) {
            apiProject = new ApiProject();
            apiProject.projectVersion = version;
            apiProject.desc = desc;
            apiProject.project = ApiConfig.sProp.apiProject;
            apiProject.projectId = ApiConfig.sProp.apiProjectID;
            FileUtils.writeStringToFile(apiProjectFile,
                new Gson().toJson(apiProject), Charsets.UTF_8, false);
        }
    }

    private ApiProject readApiProject() {
        try {
            String apiProjectJson = FileUtils.readFileToString(apiProjectFile, Charsets.UTF_8);
            return new Gson().fromJson(apiProjectJson, ApiProject.class);
        } catch (Exception e) {
            if (openLog()) {
                Logc.d("Non ApiProject Cache Config ");
            }
        }
        return null;
    }

    JsonObject readApiProjectData() {
        try {
            File file = new File("D:\\Android\\workspace\\ApiGeneratedPlugin\\ApiSimulation.json");
            String apiDataJson = FileUtils.readFileToString(file, "UTF-8");
            return (JsonObject) new JsonParser().parse(apiDataJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean openLog() {
        return ApiConfig.sProp == null || ApiConfig.sProp.openLog || ApiConfig.sProp.openParseLog;
    }

    boolean effectiveApiCache() {

        // 项目版本、ID 相同取缓存
        ApiProject apiProject = readApiProject();
        if (apiProject != null) {
            if (!ApiConfig.isEmpty(apiProject.projectId)
                && apiProject.projectId.equals(ApiConfig.sProp.apiProjectID)) {
                return !ApiConfig.isEmpty(apiProject.projectVersion)
                    && apiProject.projectVersion.equals(ApiConfig.sProp.apiVersion);
            }
            Logc.e("!! 项目接口存在配置疏漏，核实项目ID、版本 ！！");
        }

        return false;
    }

    boolean hasApiProjectConfig() {
        return apiProjectFile != null && apiProjectFile.exists();
    }
}
