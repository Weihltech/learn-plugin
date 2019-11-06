package com.vsoontech.plugin.apimodel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Logc {


    private static File apiModelLogFile;
    private static final String LOG_FILE = "genModel.log";

    public static void d(String txt) {
        System.out.println("ApiModelClass :" + txt);

//        writeLogFile(txt);
    }

    private static void writeLogFile(String txt) {
        if (apiModelLogFile != null) {
            try {
                FileUtils.writeStringToFile(apiModelLogFile, txt, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void outputLogFile(String path) {
        apiModelLogFile = new File(path, LOG_FILE);
        System.out.println("outputLogFile :" + apiModelLogFile.getPath());
    }


}
