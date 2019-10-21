package com.vsoontech.plugin.apimodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class ApiProperties {

    private String apiVersion = "";
    private String apiId = "";
    private String genOutSrc = "";
    private String genOutSrcPath = "";

    public ApiProperties read() {
        try {
            String projectPath = AnActionHelper.getProject().getBasePath()
                    .replaceAll("/", "\\\\");
            String pPath = (projectPath + File.separator + "app\\api.properties");
            Logc.d("ApiProperties : " + pPath);
            Properties properties = new Properties();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(pPath));
            properties.load(bufferedReader);
            apiVersion = properties.getProperty("api.version");
            apiId = properties.getProperty("api.project");
            genOutSrc = properties.getProperty("gen.outsrc");
            genOutSrcPath = properties.getProperty("gen.outsrc.path");
            bufferedReader.close();
//            genOutSrcPath = projectPath + "\\src\\main\\java\\"
//                    + apiOutSrc.replaceAll("\\.", "\\\\");
            Logc.d(toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public String toString() {
        return "ApiProperties{" +
                "genOutSrc='" + genOutSrc + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", apiId='" + apiId + '\'' +
                ", outSrcPath='" + genOutSrcPath + '\'' +
                '}';
    }

    public String getGenOutSrcPath() {
        return (genOutSrcPath + "." + genOutSrc).replaceAll("\\\\", "\\.");
    }

    public String getApiOutSrc() {
        return genOutSrc;
    }
}
