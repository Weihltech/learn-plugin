package com.vsoontech.plugin.apigenerate.entity;

/**
 * "desc": "获取直播课程列表",
 * "domain": "mobile.owl.duoduokankan.com",
 * "httpType": "GET",
 * "httpUrl": "/v3/owlm/live_course_list",
 * "version": 123
 */
public class ApiDetail {

    public String desc;
    public String domain;
    public String httpType;
    public String httpUrl;
    public int version;

    public EntityClass reqEntityCls;
    public EntityClass respEntityCls;


    public String getSecondDomain() {
        if (domain != null && domain.length() > 0) {
            return domain.substring(0, domain.indexOf("."));
        }
        return "UnKnow";
    }

    public String getSampleDesc() {
        return desc ;
    }
}
