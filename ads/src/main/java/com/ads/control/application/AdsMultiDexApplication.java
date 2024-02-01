package com.ads.control.application;

import androidx.multidex.MultiDexApplication;

import com.ads.control.config.MKAdConfig;
import com.ads.control.util.AppUtil;
import com.ads.control.util.SharePreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {

    public MKAdConfig mkAdConfig;
    public List<String> listTestDevice ;
    @Override
    public void onCreate() {
        super.onCreate();
        listTestDevice = new ArrayList<String>();
        mkAdConfig = new MKAdConfig(this);
        if (SharePreferenceUtils.getInstallTime(this) == 0) {
            SharePreferenceUtils.setInstallTime(this);
        }
        AppUtil.currentTotalRevenue001Ad = SharePreferenceUtils.getCurrentTotalRevenue001Ad(this);
    }


}
