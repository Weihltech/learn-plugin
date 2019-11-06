package com.vsoontech.plugin;

import com.linkin.base.app.BaseApplication;
import com.linkin.base.app.BaseApplicationHelper;

/**
 * @author Ngai
 * @since 2017/12/21
 * Des:
 */
public class MainApplication extends BaseApplication {

    @Override
    public BaseApplicationHelper initApplicationListener() {
        return new MainApplicationHelper();
    }

    @Override
    protected void runOnce() {
        super.runOnce();

    }

}
