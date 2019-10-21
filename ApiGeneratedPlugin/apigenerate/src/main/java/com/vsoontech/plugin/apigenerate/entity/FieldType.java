package com.vsoontech.plugin.apigenerate.entity;


import com.vsoontech.plugin.apigenerate.utils.HumpHelper;

public class FieldType {

    private String mValue;

    public FieldType(String value) {
        this.mValue = value;
    }

    public String value(String target) {
        if ("object".equals(mValue)) {
            return HumpHelper.lineToHump("_" + target);
        } else if ("array-object".equals(mValue)) {
            String targetC = HumpHelper.lineToHump("_" + target);
            return " java.util.ArrayList<" + targetC + "> ";
        } else if ("enum-string".equals(mValue) || "string".equals(mValue)) {
            return "String";
        } else if ("int".equals(mValue)) {
            return mValue;
        } else if ("boolean".equals(mValue)) {
            return mValue;
        }
        return "";
    }

    @Override
    public String toString() {
        return "FieldType{" +
            "mValue='" + mValue + '\'' +
            '}';
    }

    public boolean isObj() {
        return "array-object".equals(mValue) || "object".equals(mValue);
    }

    public boolean isNumber() {
        return "int".equals(mValue) || "long".equals(mValue);
    }

    public boolean isString() {
        return "string".equals(mValue) || "enum-string".equals(mValue);
    }

    public boolean isBoolean() {
        return "boolean".equals(mValue);
    }

    public boolean isArrayObj() {
        return "array-object".equals(mValue);
    }
}
