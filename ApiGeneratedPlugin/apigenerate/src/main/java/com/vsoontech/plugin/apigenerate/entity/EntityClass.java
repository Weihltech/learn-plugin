package com.vsoontech.plugin.apigenerate.entity;

import java.util.ArrayList;

public class EntityClass {

    public String className;

    public String classDesc;

    // 所有字段
    private ArrayList<EntityField> mFields;

    // 所有内部类
    private ArrayList<EntityClass> mInnerClss;


    public EntityClass(String className) {
        this.className = className;
        this.mFields = new ArrayList<>();
        this.mInnerClss = new ArrayList<>();
    }


    public ArrayList<EntityField> getFields() {
        return mFields;
    }

    public ArrayList<EntityClass> getInnerClss() {
        return mInnerClss;
    }
}
