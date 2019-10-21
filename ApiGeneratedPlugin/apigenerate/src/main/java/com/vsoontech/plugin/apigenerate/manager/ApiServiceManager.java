package com.vsoontech.plugin.apigenerate.manager;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.ArrayList;

class ApiServiceManager {

    private ApiParseManager parseManager;

    ApiServiceManager(String projectDir) {
        String sourceRemoteCacheDir = projectDir + "\\api\\";
        this.parseManager = new ApiParseManager(sourceRemoteCacheDir);
    }

    public interface CallBack {

        void onResponse(ArrayList<File> apiDetailList);
    }

    void connect(CallBack callBack) {
        if (callBack != null) {

            // 读取缓存数据
            // 校验远程服务版本号
            // 根据返回数据，分全接口刷新/单一某一接口刷新（视后台考虑）

            // api cache data TODO
            // load api project
            JsonObject apiDataJsonObj = parseManager.readApiProjectData();
            // http connect remote TODO

            if (apiDataJsonObj != null) {
                // 此处采用目标更新某一接口时，需要单独处理 TODO
                parseManager.analy(apiDataJsonObj);
            }

            // 回调结束
            callBack.onResponse(parseManager.getApiDetailList());
        }
    }


}
