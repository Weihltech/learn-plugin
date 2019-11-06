package com.vsoontech.plugin.apimodel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

public class ApiProperties {

    public String apiProject;
    public String apiProjectID;
    public String apiVersion;
    public String genOutSrc;
    public String genOutSrcPath;


    public ApiProperties read() {
        try {
            String projectPath = AnActionHelper.getProject().getBasePath();
            String pPath = (projectPath + "/app/build/generated/api/source/api.properties");
            Logc.d("ApiProperties : " + pPath);
            Properties properties = new Properties();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(pPath));
            properties.load(bufferedReader);
            apiVersion = properties.getProperty("api.version");
            apiProject = properties.getProperty("api.project");
            apiProjectID = properties.getProperty("api.project.id");
            genOutSrc = properties.getProperty("gen.outsrc");
            genOutSrcPath = properties.getProperty("gen.outsrc.path");
            bufferedReader.close();
            Logc.d(toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public String toString() {
        return "ApiProperties{" +
                "apiProject='" + apiProject + '\'' +
                ", apiProjectID='" + apiProjectID + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", genOutSrc='" + genOutSrc + '\'' +
                ", genOutSrcPath='" + genOutSrcPath + '\'' +
                '}';
    }

    String getGenOutSrcPath() {
        return genOutSrcPath.replaceAll("/", "\\.") + "." + genOutSrc;
    }

    public String getApiOutSrc() {
        return genOutSrc;
    }
}
