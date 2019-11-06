package com.vsoontech.plugin.apigenerate;

import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.api.AndroidSourceDirectorySet;
import com.android.build.gradle.api.AndroidSourceSet;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Scanner;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

public class ApiConfig {

    public static final String IMPORT_HTTP_TYPE_GET = "import com.vsoontech.base.http.request.GetRequest;";
    public static final String IMPORT_HTTP_TYPE_POST = "import com.vsoontech.base.http.request.PostRequest;";
    public static final String IMPORT_LOGGER = "import com.linkin.base.debug.logger.L;";

    // 目标编译目录，即 ApiGenerate 生成Java 文件目录
    private static final String SRC_DIR_API_GENERATE = "build/generated/api/java";

    // 配置文件模版
    private static final String TMPL_API_PROPERTIES_PATH = "/META-INF/ApiProperties.tmpl";
    // 实体类必要输出模版
    public static final String TMPL_REQ_CLASS_PATH = "/META-INF/ReqClass.tmpl";
    public static final String TMPL_RESP_CLASS_PATH = "/META-INF/RespClass.tmpl";
    public static final String TMPL_INNER_CLASS_PATH = "/META-INF/InnerClass.tmpl";
    public static final String TMPL_CONSTRUCT_METHOD_PATH = "/META-INF/ConstructMethod.tmpl";
    public static final String TMPL_CONSTS_CLASS_PATH = "/META-INF/ConstsClass.tmpl";

    // ApiParseManager 文档缓存输出路径
    private static final String SOURCE_DIR_API_CACHE = "build/generated/api/source";

    // comm.file

    public static final String PROJECT = "project.json";
    public static final String PROJECT_CONTENT = "projectContent.json";
    public static final String JSON_SUFFIX = ".json";
    public static final String JAVA_SUFFIX = ".java";
    public static final String REQ = "Req";
    public static final String RESP = "Resp";
    public static final String CONSTS = "Consts";
    public static final String FILE_NAME_API_PROP = "api.properties";

    private static AppPlugin appPlugin;
    private static String projectDir;
    public static Prop sProp;
    public static String apiCacheSourceDir = "";
    private static String applicationId = "";
    private static String apiJavaOutputDir = "";
    public static String assetsDir = "";

    public static void init(Project project) {
        try {
            appPlugin = project.getPlugins().findPlugin(AppPlugin.class);
            sProp = project.getExtensions().create("apiGenerate", Prop.class);
            projectDir = project.getProjectDir().getAbsolutePath();
            apiCacheSourceDir = projectDir + File.separator + SOURCE_DIR_API_CACHE + File.separator;
            assetsDir = projectDir + "/src/main/assets/";
            loadInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadInfo() {
        try {
            if (appPlugin != null) {
                AndroidConfig androidConfig = appPlugin.getExtension();
                // 设置 AndroidSourceSet
                AndroidSourceSet androidSourceSet = androidConfig.getSourceSets().findByName("main");
                assert androidSourceSet != null;
                AndroidSourceDirectorySet androidSourceDirectorySet = androidSourceSet.getJava();
                androidSourceDirectorySet.srcDir(SRC_DIR_API_GENERATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getApplicationId() {
        if (isEmpty(applicationId)) {
            try {
                if (appPlugin != null) {
                    AndroidConfig androidConfig = appPlugin.getExtension();
                    applicationId = androidConfig.getDefaultConfig().getApplicationId();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applicationId;
    }

    public static String getApiJavaOutputDir() {
        if (isEmpty(apiJavaOutputDir)) {
            apiJavaOutputDir = projectDir + File.separator
                + SRC_DIR_API_GENERATE + File.separator
                + sProp.genOutSrc.replace(".", File.separator) + File.separator;
        }

        return apiJavaOutputDir;
    }

    public static boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
        }
    }

    public static void saveProp() {
        try {
            File propFile = new File(apiCacheSourceDir, FILE_NAME_API_PROP);
            sProp.genOutSrcPath = "app/" + SRC_DIR_API_GENERATE;
            String content = getResourceAsStream(TMPL_API_PROPERTIES_PATH);
            content = content.replace("%date%", new Date().toString())
                .replace("%project_id%", sProp.apiProjectID)
                .replace("%project%", sProp.apiProject)
                .replace("%version%", sProp.apiVersion)
                .replace("%outsrc%", sProp.genOutSrc)
                .replace("%outsrcpath%", "app/" + SRC_DIR_API_GENERATE);
            FileUtils.writeStringToFile(propFile, content, Charsets.UTF_8, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getResourceAsStream(String filePath) {
        InputStream is = ApiConfig.class.getResourceAsStream(filePath);
        Scanner scanner = new Scanner(is);
        return scanner.useDelimiter("\\A").next();
    }

    public static String getProjectUrl() {
        if (sProp == null) {
            return "";
        }
        return sProp.serviceUrl + "?apiProject=" + sProp.apiProjectID + "&apiVersion=" + sProp.apiVersion;
    }

    /**
     * apiGenerate {
     * apiProject = 'gradle-api'
     * apiVersion = '1560852447'
     * genOutSrc = 'com.vsoontech.plugin.api'
     * }
     */
    public static class Prop {

        public String apiProject;
        public String apiProjectID;
        public String apiVersion;
        public String genOutSrc;

        // 隐藏可配置参数
        public String serviceUrl = "http://115.28.12.111:9090/v2/api/detail";
        public String genOutSrcPath;
        public boolean genSimulationData;
        public boolean openLog;
        public boolean openParseLog;
        public boolean openConvertLog;
        public boolean openSimulationLog;

        @Override
        public String toString() {
            return "Prop{" +
                "apiProject='" + apiProject + '\'' +
                ", apiProjectID='" + apiProjectID + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", genOutSrc='" + genOutSrc + '\'' +
                ", openLog=" + openLog +
                '}';
        }
    }

    public static void print() {
        Logc.d("applicationId = " + getApplicationId());
        Logc.d(sProp.toString());
        Logc.d("apiCacheSourceDir = " + apiCacheSourceDir);
        Logc.d("apiJavaOutputDir = " + getApiJavaOutputDir());
        Logc.d("apiSimulationDataDir = " + assetsDir);
    }

}
