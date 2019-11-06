package com.vsoontech.plugin.apimodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HumpHelper {

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
//        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 驼峰转下划线
     */
    public static String humpToLine2(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String convertTargetName(String target) {
        if (!isEmpty(target)) {
            String tName = "_" + target;
            if (tName.endsWith("list")
                || tName.endsWith("List")) {
                tName = tName.substring(0, tName.length() - 4);
                if (tName.length() == 1) {
                    tName = "_item";
                }
            }
            return lineToHump(tName);
        }
        return "UnKnow";
    }

    private static boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
        }
    }
}
