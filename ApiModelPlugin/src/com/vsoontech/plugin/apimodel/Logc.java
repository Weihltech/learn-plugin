package com.vsoontech.plugin.apimodel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Logc {

    static File logFile = new File("D:\\Android\\workspace\\ApiGeneratedPlugin\\model.log");

    public static void d(String txt) {
        System.out.println("ApiModelClass :" + txt);

        try {
            FileUtils.writeStringToFile(logFile, txt + "\n", "UTF-8", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
