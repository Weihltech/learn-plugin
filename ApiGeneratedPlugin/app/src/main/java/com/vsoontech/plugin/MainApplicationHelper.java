package com.vsoontech.plugin;

import com.linkin.base.app.BaseApplicationHelper;
import com.vsoontech.base.http.request.presenter.RequestBuilder;
import com.vsoontech.base.reporter.bean.ActionReportPage;
import java.util.HashMap;

/**
 * @author Ngai
 * @since 2017/12/21
 * Des:
 */
public class MainApplicationHelper extends BaseApplicationHelper {

    @Override
    public void onChangeHostStatus(int hostStatus) {
        super.onChangeHostStatus(hostStatus);
    }

    @Override
    protected RequestBuilder initHost() {
        return new RequestBuilder()
            .setDomainName(MainServer.RELEASE_SERVER)
            .setTestDomainName(MainServer.TEST_SERVER)
            //不需要开启全局域名替换
            .setEnableHostFilter(false)
            // 项目与那种方式启动
//                .setHostStatus(switchHostStatus()) HostStatus.TESTLINE
            //设置全局线下域名
            .setOfflineDomainName(MainServer.DEBUG_SERVER);
    }

    @Override
    public ActionReportPage initReportPage() {
        // 事件上传的公共数据
        return new ActionReportPage(new HashMap<String, Object>());
    }

    @Override
    public boolean isBindVService() {
        return true;
    }

}
