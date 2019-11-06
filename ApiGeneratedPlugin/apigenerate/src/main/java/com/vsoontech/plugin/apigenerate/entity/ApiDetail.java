package com.vsoontech.plugin.apigenerate.entity;

import static com.vsoontech.plugin.apigenerate.ApiConfig.IMPORT_HTTP_TYPE_GET;
import static com.vsoontech.plugin.apigenerate.ApiConfig.IMPORT_HTTP_TYPE_POST;
import static com.vsoontech.plugin.apigenerate.ApiConfig.isEmpty;

import java.util.ArrayList;

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
    public String group;


    public String getSecondDomain() {
        if (domain != null && domain.length() > 0) {
            return domain.substring(0, domain.indexOf("."));
        }
        return "UnKnow";
    }

    public String getSampleDesc() {
        return desc;
    }

    public String getHttpType() {
        if (isEmpty(httpType)) {
            return "Get";
        }
        String typeLower = httpType.toLowerCase();
        return "get".equals(typeLower) ? "Get" : "Post";
    }

    public String getHttpTypeImport() {
        return "Get".equals(getHttpType()) ? IMPORT_HTTP_TYPE_GET : IMPORT_HTTP_TYPE_POST;
    }

    @Override
    public String toString() {
        return "ApiDetail{" +
            "'" + group + '\'' +
            ", '" + httpType + '\'' +
            ", '" + httpUrl + '\'' +
            '}';
    }

    // 增加判断当前Api是否合法接口
    public boolean isIllegal() {
        return isEmpty(domain)
            || isEmpty(httpType)
            || isEmpty(httpUrl)
            || !httpUrl.contains("/");
    }

}
