package com.vsoontech.plugin.apigenerate.utils;

import com.vsoontech.plugin.apigenerate.ApiConfig;

public class Logc {

    public static void d(String log) {
        System.out.println("[ApiGenerate]: " + log);
    }

    public static void e(String log) {
        System.err.println("[ApiGenerate]: " + log);
    }

    public static boolean openLog() {
        return ApiConfig.sProp == null || ApiConfig.sProp.openLog;
    }

    public static void emptyLine() {
        System.out.println();
    }
}
