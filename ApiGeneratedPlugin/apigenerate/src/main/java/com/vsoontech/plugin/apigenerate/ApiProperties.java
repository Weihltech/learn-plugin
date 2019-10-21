package com.vsoontech.plugin.apigenerate;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

/**
 * .\app\api.properties
 * 读取 配置信息，向Config赋值
 */
public class ApiProperties {

    private final String PROPERTIES_PATH = "/META-INF/ApiProperties.tmpl";
    private File propertiesFile = null;
    private Project mProject;

    public ApiProperties(Project project) {
        mProject = project;
        propertiesFile = new File(mProject.getProjectDir(), "api.properties");
    }


    public void read() {

        try {
            // 不存在则生成，存在则取值
            if (propertiesFile != null
                && !propertiesFile.exists()) {
                String content = getResourceAsStream(PROPERTIES_PATH);
                FileUtils.writeStringToFile(propertiesFile, content, Charsets.UTF_8);
            }
            //
            Properties properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            BufferedReader bufferedReader = new BufferedReader(new FileReader(propertiesFile.getAbsolutePath()));
            // 使用properties对象加载输入流
            properties.load(bufferedReader);
            //获取key对应的value值
            Config.apiVersion = properties.getProperty("api.version");
            Config.apiProject = properties.getProperty("api.project");
            Config.genOutSrc = properties.getProperty("gen.outsrc");
            Config.genOutSrcPath = properties.getProperty("gen.outsrc.path");
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getResourceAsStream(String filePath) {
        InputStream is = ApiProperties.class.getResourceAsStream(filePath);
        Scanner scanner = new Scanner(is);
        return scanner.useDelimiter("\\A").next();
    }

    public void save() {
        try {
            Properties properties = new Properties();
            properties.setProperty("api.version", Config.apiVersion);
            properties.setProperty("api.project", Config.apiProject);
            properties.setProperty("gen.outsrc", Config.genOutSrc);
            properties.setProperty("gen.outsrc.path", Config.genOutSrcPath);
            FileOutputStream outputStream = new FileOutputStream(propertiesFile);
            properties.store(outputStream, null);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
