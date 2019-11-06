package com.vsoontech.plugin.apigenerate.entity;


import java.util.ArrayList;

/**
 * "desc": "教师列表",
 * "name": "teacherList",
 * "notNull": true,
 * "target": "teacher",
 * "type": "array-object"
 */
public class EntityField {


    public String desc;
    public String name;
    public boolean notNull;
    public String target;
    public FieldType type;
    public ArrayList<FieldLink> links;


    public static class FieldLink{
        public String desc;
        public String name;
        public String value;
        public String type;

        @Override
        public String toString() {
            return "FieldLink{" +
                "desc='" + desc + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                '}';
        }
    }

    @Override
    public String toString() {
        return "EntityField{" +
            "desc='" + desc + '\'' +
            ", name='" + name + '\'' +
            ", notNull=" + notNull +
            ", target='" + target + '\'' +
            ", type=" + type +
            ", links=" + links +
            '}';
    }
}
