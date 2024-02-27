package com.example.andmoduleads.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.MKAd;
import com.ads.control.applovin.AppLovin;
import com.ads.control.applovin.AppOpenMax;
import com.ads.control.config.AirBridgeConfig;
import com.ads.control.config.MKAdConfig;
import com.ads.control.funtion.AdCallback;
import com.example.andmoduleads.BuildConfig;
import com.example.andmoduleads.MyApplication;
import com.example.andmoduleads.R;
import com.google.android.gms.ads.nativead.NativeAd;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private List<String> list = new ArrayList<>();
    private String idAdSplash;

    private void initAds() {
        String environment = BuildConfig.env_dev ? MKAdConfig.ENVIRONMENT_DEVELOP : MKAdConfig.ENVIRONMENT_PRODUCTION;
        MyApplication app = MyApplication.getApplication();
        app.mkAdConfig = new MKAdConfig(app, MKAdConfig.PROVIDER_ADMOB, environment);

        // Optional: setup Airbridge
        AirBridgeConfig airBridgeConfig = new AirBridgeConfig();
        airBridgeConfig.setEnableAirBridge(true);
        airBridgeConfig.setAppNameAirBridge("calculator");
        airBridgeConfig.setTokenAirBridge("22e9f842075b4fb3a0412debe07f6cdd");
        MyApplication.getApplication().mkAdConfig.setAirBridgeConfig(airBridgeConfig);
        // Optional: enable ads resume
        MyApplication.getApplication().mkAdConfig.setIdAdResume(BuildConfig.ads_open_app);

        // Optional: setup list device test - recommended to use

        app.listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431");
        app.mkAdConfig.setListDeviceTest(app.listTestDevice);
        app.mkAdConfig.setIntervalInterstitialAd(15);

        MKAd.getInstance().init(this, app, app.mkAdConfig, false);

        // Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        AppLovin.getInstance().setDisableAdResumeWhenClickAds(true);
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);

        if (MKAd.getInstance().getMediationProvider() == app.mkAdConfig.PROVIDER_ADMOB) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        } else {
            AppOpenMax.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initAds();
        if (MKAd.getInstance().getMediationProvider() == MKAdConfig.PROVIDER_ADMOB)
            idAdSplash = BuildConfig.ad_interstitial_splash;
        else
            idAdSplash = getString(R.string.applovin_test_inter);
        AppOpenManager.getInstance().loadAdOpenSplash2id(SplashActivity.class, this,
                "ca-app-pub-3940256099942544/3419835294",
                "ca-app-pub-3940256099942544/3419835294", 25000, new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startMain();
                    }
                });

        Admob.getInstance().loadNativeAd(this, BuildConfig.ad_native, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(NativeAd unifiedNativeAd) {
                MyApplication.setNativeAd(unifiedNativeAd);
            }
            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        }, null);
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppOpenManager.getInstance().onCheckShowAppOpenSplashWhenFail(this, new AdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startMain();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "Splash onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "Splash onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}