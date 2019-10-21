package com.vsoontech.plugin.apigenerate.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vsoontech.plugin.apigenerate.entity.ApiProject;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

/**
 * 解析后台接口文档，并缓存数据
 */
class ApiParseManager {

    private final String PROJECT = "project";
    private final String SUFFIX = ".json";
    private File apiCacheDir;
    private File apiProjectFile;
    // 需要创建或更新的 ApiDetail
    private ArrayList<File> apiDetailFileList;
    private Project mProject;

    ApiParseManager(String sourceRemoteCacheDir) {
        apiDetailFileList = new ArrayList<>();
        verifyCacheDir(sourceRemoteCacheDir);
    }


    private void verifyCacheDir(String cacheDir) {
        // 校验或创建缓存目录
        apiCacheDir = new File(cacheDir);
        if (!apiCacheDir.exists() && apiCacheDir.mkdirs()) {
            Logc.d("api-cache-dir mkdirs success !");
        }
        apiProjectFile = new File(apiCacheDir.getAbsolutePath() + File.separator + PROJECT + SUFFIX);
    }

    // 解析当前文档接口数据文档信息
    void analy(JsonObject apiDataJsonObj) {

        if (apiDataJsonObj != null) {
            try {
                saveApiProject(apiDataJsonObj);
                JsonArray apiGroupList = apiDataJsonObj.getAsJsonArray("apiGroupList");
                analyApiGroupList(apiGroupList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Logc.d("------  ApiParseManager  ------");
        loadApiDetailList(apiCacheDir);
    }

    private void loadApiDetailList(File dirFile) {
        File[] tDirFiles = dirFile.listFiles();
        for (File subFile : tDirFiles) {
            if (subFile.exists()
                && subFile.isFile()
                && !subFile.getAbsolutePath().endsWith(PROJECT + SUFFIX)) {
                // TODO 目前全更新
                apiDetailFileList.add(subFile);
                Logc.d("> " + subFile.getName());
            } else if (subFile.isDirectory()) {
                Logc.d(subFile.getName());
                loadApiDetailList(subFile);
            }
        }
    }

    public ArrayList<File> getApiDetailList() {
        return apiDetailFileList;
    }

    private void analyApiGroupList(JsonArray apiGroupList) throws IOException {
        int index = 0;
        for (JsonElement jsonElement : apiGroupList) {
            JsonObject apiGroup = jsonElement.getAsJsonObject();
            String desc = apiGroup.get("desc").getAsString();

            File groupFile = new File(apiCacheDir.getAbsoluteFile() + File.separator + desc);
            if (!groupFile.exists() && groupFile.mkdir()) {
                Logc.d("类别(New) ：" + desc);
            } else {
                Logc.d("类别 ：" + desc);
            }

            JsonArray apiDetailList = apiGroup.getAsJsonArray("apiDetailList");
            analyApiDetailList(groupFile, apiDetailList);
        }
    }

    private void analyApiDetailList(File groupFile, JsonArray apiDetailList) throws IOException {
        for (JsonElement jsonElement : apiDetailList) {
            JsonObject apiDeatil = jsonElement.getAsJsonObject();
            if (apiDeatil != null
                && apiDeatil.get("desc") != null) {
                String desc = apiDeatil.get("desc").getAsString();
                String httpUrl = apiDeatil.get("httpUrl").getAsString();
                String reqName = httpUrl.substring(httpUrl.lastIndexOf("/"), httpUrl.length());
                Logc.d("> 接口描述 ：" + desc);
                for (JsonElement subElement : apiDetailList) {
                    if (subElement != null) {
                        String jsonData = subElement.toString();
//                        Logc.d("- ApiDetail ：" + jsonData);
                        File subFile = new File(groupFile, reqName + SUFFIX);
                        subFile.setWritable(true);
                        FileUtils.writeStringToFile(subFile, jsonData, Charsets.UTF_8, false);
                        subFile.setReadOnly();
                    }
                }
            }
        }
    }

    private void saveApiProject(JsonObject apiDataJsonObj) throws IOException {
        if (apiProjectFile != null) {
            ApiProject apiProject = new ApiProject();
            apiProject.version = apiDataJsonObj.get("version").getAsInt();
            apiProject.desc = apiDataJsonObj.get("desc").getAsString();
            apiProjectFile.setWritable(true);
            FileUtils.writeStringToFile(apiProjectFile, new Gson().toJson(apiProject), Charsets.UTF_8, false);
            apiProjectFile.setReadOnly();
        }
    }

    public JsonObject readApiProjectData() {
        try {
            File file = new File("D:\\Android\\workspace\\ApiGeneratedPlugin\\ApiSimulation.json");
            String apiDataJson = FileUtils.readFileToString(file, "UTF-8");
            return (JsonObject) new JsonParser().parse(apiDataJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
