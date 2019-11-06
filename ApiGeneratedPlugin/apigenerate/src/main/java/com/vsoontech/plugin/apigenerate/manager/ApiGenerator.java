package com.vsoontech.plugin.apigenerate.manager;


import com.vsoontech.plugin.apigenerate.ApiConfig;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

public class ApiGenerator {

    private ArrayList<ApiDetail> tIllegalApiDetails;
    private ArrayList<String> tGenClsFailureApiDetails;
    private ArrayList<String> tGenSimuFailureApiDetails;
    private ApiParseManager parseManager;

    public ApiGenerator() {
        this.parseManager = new ApiParseManager();
        this.tIllegalApiDetails = new ArrayList<>();
        this.tGenClsFailureApiDetails = new ArrayList<>();
        this.tGenSimuFailureApiDetails = new ArrayList<>();
    }

    public void run() {

        // 增加调试模式，当没有版本缓存文件时， 取 一级缓存
        if (!parseManager.hasApiProjectConfig()) {
            try {
                String projectContentJson = readApiContent();
                if (!ApiConfig.isEmpty(projectContentJson)) {
                    parseManager.parse(projectContentJson);
                    saveApiContent(projectContentJson);
                    onClassParse(parseManager.loadCacheApiDetailJsonFileList());
                    return;
                }
            } catch (Exception ex) {
                Logc.d("Error - projectContentJson ! ");
                // need delete ?
            }
        }

        // 取 二级缓存
        if (parseManager.hasApiProjectConfig()
            && parseManager.effectiveApiCache()) {
            Logc.d("Version isEquals , Load Api Cache ! ");
            onClassParse(parseManager.loadCacheApiDetailJsonFileList());
            return;
        }

        // 取网络数据
        connectApiService(this::onClassParse);
    }

    private void onClassParse(ArrayList<File> loadCacheApiDetailJsonFileList) {

        ApiConfig.saveProp();
        new ClassParseManager().parse(loadCacheApiDetailJsonFileList,
            new ClassParseManager.CallBack() {
                @Override
                public void illegalApi(ArrayList<ApiDetail> illegalApiDetails) {
                    tIllegalApiDetails.addAll(illegalApiDetails);
                }

                @Override
                public void onResult(ArrayList<ApiDetail> apiDetails) {
                    if (apiDetails != null
                        && !apiDetails.isEmpty()) {
                        if (Logc.openLog()) {
                            Logc.d("Parsing Json-File and Collect-Message Success ! ");
                        }
                        ClassConvertManager classConvertManager = new ClassConvertManager();
                        classConvertManager.logStart();
                        classConvertManager.cleanApiJavaDir();
                        for (ApiDetail apiDetail : apiDetails) {
                            try {
                                classConvertManager.generate(apiDetail);
                            } catch (Exception e) {
                                tGenClsFailureApiDetails.add("[" + apiDetail.toString()
                                    + "] Convert " + e.getMessage());
                            }
                        }

                        if (ApiConfig.sProp.genSimulationData) {
                            SimulationManager simulationManager = new SimulationManager();
                            simulationManager.logStart();
                            for (ApiDetail apiDetail : apiDetails) {
                                try {
                                    simulationManager.generate(apiDetail);
                                } catch (Exception e) {
                                    tGenSimuFailureApiDetails.add("[" + apiDetail.toString()
                                        + "] Generate Simulation-Data " + e.getMessage());
                                }
                            }
                        }
                        printGeneratorMessage();
                        Logc.e("Api Generate 共：" + apiDetails.size() +
                            " ; 不合法接口：" + tIllegalApiDetails.size() +
                            " ; 不合法Class：" + tGenClsFailureApiDetails.size() +
                            (ApiConfig.sProp.genSimulationData
                                ? (" ; 虚拟数据创建失败：" + tGenClsFailureApiDetails.size())
                                : " ; 虚拟数据功能未开放(genSimulationData = true)"));
                    }
                    Logc.d("ApiGenerate Done .");
                }
            });
    }

    private void printGeneratorMessage() {
        if (!tIllegalApiDetails.isEmpty()) {
            Logc.emptyLine();
            Logc.e("Illegal 接口清单 ！[接口域名、链接、类型 .etc]");
            for (ApiDetail info : tIllegalApiDetails) {
                Logc.e(info.toString());
            }
        }
        if (!tGenClsFailureApiDetails.isEmpty()) {
            Logc.emptyLine();
            Logc.e("Generate-Class 失败清单 ！[字段类型、关键字、对列、内部类对象 .etc]");
            for (String info : tGenClsFailureApiDetails) {
                Logc.e(info);
            }
        }
        if (ApiConfig.sProp.genSimulationData
            && !tGenSimuFailureApiDetails.isEmpty()) {
            Logc.emptyLine();
            Logc.e("Generate-Simulation-Data 失败清单 ！[对照相应实体类 .etc]");
            for (String info : tGenSimuFailureApiDetails) {
                Logc.e(info);
            }
        }
    }

    public interface CallBack {

        void onResponse(ArrayList<File> apiDetailList);
    }

    private void connectApiService(CallBack callBack) {
        if (callBack != null) {
            try {
                String url = ApiConfig.getProjectUrl();
                if (!ApiConfig.isEmpty(url)) {
                    Logc.d("Connect Api Service ; Url = " + url);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    try (Response response = client.newCall(request).execute()) {
                        String contentJson = Objects.requireNonNull(response.body()).string();
                        parseManager.parse(contentJson);

                        saveApiContent(contentJson);

                        // 回调并解析Api
                        callBack.onResponse(parseManager.loadCacheApiDetailJsonFileList());
                    } catch (Exception e) {
                        Logc.d("Error - Response ! : " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Logc.d("Connect Api Service Failure ! " + e.getMessage());
            }
        }
    }

    // 缓存一份Api文档数据
    private void saveApiContent(String contentJson) throws IOException {
        File projectDataJson = new File(ApiConfig.apiCacheSourceDir, ApiConfig.PROJECT_CONTENT);
        FileUtils.writeStringToFile(projectDataJson, contentJson, Charsets.UTF_8, false);
        if (Logc.openLog()) {
            Logc.d("Save ProjectContent Success !");
        }
    }

    // 读取一份Api文档数据
    private String readApiContent() throws IOException {
        return FileUtils.readFileToString(
            new File(ApiConfig.apiCacheSourceDir, ApiConfig.PROJECT_CONTENT), Charsets.UTF_8);
    }

}
