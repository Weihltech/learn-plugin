package com.vsoontech.plugin.apigenerate.entity;


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


    @Override
    public String toString() {
        return "EntityField{" +
            "desc='" + desc + '\'' +
            ", name='" + name + '\'' +
            ", notNull=" + notNull +
            ", target='" + target + '\'' +
            ", type=" + type +
            '}';
    }
}
