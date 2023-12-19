package com.example.andmoduleads;

import com.ads.control.ads.MKAd;
import com.ads.control.config.AirBridgeConfig;
import com.ads.control.config.MKAdConfig;
import com.ads.control.application.AdsMultiDexApplication;
import com.ads.control.applovin.AppLovin;
import com.ads.control.applovin.AppOpenMax;
import com.ads.control.billing.AppPurchase;
import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.example.andmoduleads.activity.MainActivity;
import com.example.andmoduleads.activity.SplashActivity;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends AdsMultiDexApplication {
    private final String ADJUST_TOKEN = "cc4jvudppczk";
    private final String EVENT_PURCHASE_ADJUST = "gzel1k";
    private final String EVENT_AD_IMPRESSION_ADJUST = "gzel1k";

    protected StorageCommon storageCommon;
    private static MyApplication context;

    public static MyApplication getApplication() {
        return context;
    }

    public StorageCommon getStorageCommon() {
        return storageCommon;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Admob.getInstance().setNumToShowAds(0);

        storageCommon = new StorageCommon();
        initBilling();
        initAds();

    }

    private void initAds() {
        String environment = BuildConfig.env_dev ? MKAdConfig.ENVIRONMENT_DEVELOP : MKAdConfig.ENVIRONMENT_PRODUCTION;
        mkAdConfig = new MKAdConfig(this, MKAdConfig.PROVIDER_ADMOB, environment);

        // Optional: setup Airbridge
        AirBridgeConfig airBridgeConfig = new AirBridgeConfig();
        airBridgeConfig.setEnableAirBridge(true);
        airBridgeConfig.setAppNameAirBridge("calculator");
        airBridgeConfig.setTokenAirBridge("22e9f842075b4fb3a0412debe07f6cdd");
        mkAdConfig.setAirBridgeConfig(airBridgeConfig);
        // Optional: enable ads resume
        mkAdConfig.setIdAdResume(BuildConfig.ads_open_app);

        // Optional: setup list device test - recommended to use
        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        mkAdConfig.setListDeviceTest(listTestDevice);
        mkAdConfig.setIntervalInterstitialAd(15);

        MKAd.getInstance().init(this, mkAdConfig, false);

        // Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true);
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);

        if (MKAd.getInstance().getMediationProvider() == mkAdConfig.PROVIDER_ADMOB) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        } else {
            AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        }
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(MainActivity.PRODUCT_ID);
        List<String> listSubsId = new ArrayList<>();

        AppPurchase.getInstance().initBilling(getApplication(), listINAPId, listSubsId);
    }

}
