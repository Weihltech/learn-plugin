package com.vsoontech.plugin.apigenerate.entity;


import static com.vsoontech.plugin.apigenerate.ApiConfig.isEmpty;

import com.vsoontech.plugin.apigenerate.utils.HumpHelper;

public class FieldType {

    private String mValue;

    public FieldType(String value) {
        this.mValue = value;
    }

    public String value(String target) {
        if (isObj()) {
            return getTargetClassName(target);
        } else if (isArrayObj()) {
            return " java.util.ArrayList<" + getTargetClassName(target) + "> ";
        } else if (isArrayString()) {
            return " java.util.ArrayList<String> ";
        } else if (isArrayInteger()) {
            return " java.util.ArrayList<Integer> ";
        } else if (isArrayFloat()) {
            return " java.util.ArrayList<Float> ";
        } else if (isString()) {
            return "String";
        } else if (isNumber()) {
            return mValue;
        } else if (isBoolean()) {
            return mValue;
        } else if (isEnumInt()) {
            return "int";
        }
        return "";
    }

    private String getTargetClassName(String target) {
        return HumpHelper.convertTargetName(target);
    }

    @Override
    public String toString() {
        return "FieldType{" +
            "mValue='" + mValue + '\'' +
            '}';
    }

    public boolean isObj() {
        return "object".equals(mValue);
    }

    public boolean isNumber() {
        return "int".equals(mValue)
            || "long".equals(mValue)
            || "float".equals(mValue);
    }

    public boolean isDate() {
        return "date".equals(mValue);
    }

    public boolean isEnumInt() {
        return "enum-int".equals(mValue);
    }

    public boolean isString() {
        return "string".equals(mValue)
            || "date".equals(mValue) // 此类型实际返回格式化后的字段串
            || "enum-string".equals(mValue);
    }

    public boolean isBoolean() {
        return "boolean".equals(mValue);
    }

    public boolean isArrayObj() {
        return "array-object".equals(mValue);
    }

    public boolean isArrayFloat() {
        return "array-float".equals(mValue);
    }

    public boolean isArrayInteger() {
        return "array-int".equals(mValue);
    }

    public boolean isArrayString() {
        return "array-string".equals(mValue);
    }

    public boolean isEnum() {
        return !isEmpty(mValue) && mValue.startsWith("enum");
    }
}
