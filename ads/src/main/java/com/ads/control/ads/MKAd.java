package com.ads.control.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.control.R;
import com.ads.control.admob.Admob;
import com.ads.control.admob.AppOpenManager;
import com.ads.control.ads.nativeAds.MKAdAdapter;
import com.ads.control.ads.nativeAds.MKAdPlacer;
import com.ads.control.ads.wrapper.ApAdError;
import com.ads.control.ads.wrapper.ApAdValue;
import com.ads.control.ads.wrapper.ApInterstitialAd;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.ads.control.ads.wrapper.ApRewardAd;
import com.ads.control.ads.wrapper.ApRewardItem;
import com.ads.control.applovin.AppLovin;
import com.ads.control.applovin.AppLovinCallback;
import com.ads.control.applovin.AppOpenMax;
import com.ads.control.billing.AppPurchase;
import com.ads.control.config.MKAdConfig;
import com.ads.control.event.MKAirBridge;
import com.ads.control.event.MKLogEventManager;
import com.ads.control.funtion.AdCallback;
import com.ads.control.funtion.RewardCallback;
import com.ads.control.util.AppUtil;
import com.ads.control.util.SharePreferenceUtils;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacer;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class MKAd {
    public static final String TAG_ADJUST = "MKAdjust";
    public static final String TAG = "MKAd";
    private static volatile MKAd INSTANCE;
    private MKAdConfig adConfig;
    private MKInitCallback initCallback;
    private Boolean initAdSuccess = false;

    public static synchronized MKAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MKAd();
        }
        return INSTANCE;
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds - default = 3
     */
    public void setCountClickToShowAds(int countClickToShowAds) {
        Admob.getInstance().setNumToShowAds(countClickToShowAds);
        AppLovin.getInstance().setNumShowAds(countClickToShowAds);
    }

    /**
     * Set count click to show ads interstitial when call showInterstitialAdByTimes()
     *
     * @param countClickToShowAds Default value = 3
     * @param currentClicked      Default value = 0
     */
    public void setCountClickToShowAds(int countClickToShowAds, int currentClicked) {
        Admob.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
        AppLovin.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
    }


    /**
     * @param context
     * @param adConfig MKAdConfig object used for SDK initialisation
     */
    public void init(Application context, MKAdConfig adConfig) {
        init(context, adConfig, false);
    }

    /**
     * @param context
     * @param adConfig             MKAdConfig object used for SDK initialisation
     * @param enableDebugMediation set show Mediation Debugger - use only for Max Mediation
     */
    public void init(Application context, MKAdConfig adConfig, Boolean enableDebugMediation) {
        if (adConfig == null) {
            throw new RuntimeException("cant not set MKAdConfig null");
        }
        this.adConfig = adConfig;
        AppUtil.VARIANT_DEV = adConfig.isVariantDev();
        Log.i(TAG, "Config variant dev: " + AppUtil.VARIANT_DEV);
        if (adConfig.isEnableAirBridge()) {
            Log.i(TAG, "init airbridge");
            MKAirBridge.enableAirBridge = true;
            MKAirBridge.getInstance().init(context, adConfig.getAirBridgeConfig().getAppNameAirBridge(),adConfig.getAirBridgeConfig().getTokenAirBridge(), true);
        }
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().init(context, new AppLovinCallback() {
                    @Override
                    public void initAppLovinSuccess() {
                        super.initAppLovinSuccess();
                        initAdSuccess = true;
                        if (initCallback != null)
                            initCallback.initAdSuccess();
                        if (adConfig.isEnableAdResume()) {
                            AppOpenMax.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());
                        }
                    }
                }, enableDebugMediation);
                break;
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().init(context, adConfig.getListDeviceTest());
                if (adConfig.isEnableAdResume())
                    AppOpenManager.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());

                initAdSuccess = true;
                if (initCallback != null)
                    initCallback.initAdSuccess();
                break;
        }
    }

    public int getMediationProvider() {
        return adConfig.getMediationProvider();
    }

    public void setInitCallback(MKInitCallback initCallback) {
        this.initCallback = initCallback;
        if (initAdSuccess)
            initCallback.initAdSuccess();
    }

    public MKAdConfig getAdConfig() {
        return adConfig;
    }

    public void loadBanner(final Activity mActivity, String id) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadBanner(mActivity, id, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadBanner(mActivity, id);
        }
    }

    public void loadBanner(final Activity mActivity, String id, final MKAdCallback adCallback) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadBanner(mActivity, id, new AdCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adCallback.onAdLoaded();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        adCallback.onAdClicked();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adCallback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        adCallback.onAdImpression();
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadBanner(mActivity, id, new AdCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adCallback.onAdLoaded();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        adCallback.onAdClicked();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adCallback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        adCallback.onAdImpression();
                    }
                });
        }
    }

    public void loadCollapsibleBanner(final Activity activity, String id, String gravity, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBanner(activity, id, gravity, adCallback, adConfig.getAdjustTokenTiktok());
    }

    public void loadCollapsibleBannerSizeMedium(final Activity activity, String id, String gravity, AdSize sizeBanner, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBannerSizeMedium(activity, id, gravity, sizeBanner, adCallback, adConfig.getAdjustTokenTiktok());
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadBannerFragment(mActivity, id, rootView, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadBannerFragment(mActivity, id, rootView);
        }
    }

    public void loadInlineBanner(final Activity mActivity, String idBanner, String inlineStyle) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadInlineBanner(mActivity, idBanner, inlineStyle, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                break;
        }
    }

    public void loadInlineBanner(final Activity mActivity, String idBanner, String inlineStyle, AdCallback adCallback) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadInlineBanner(mActivity, idBanner, inlineStyle, adCallback, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                break;
        }
    }
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback adCallback) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadBannerFragment(mActivity, id, rootView, adCallback, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadBannerFragment(mActivity, id, rootView, adCallback);
        }
    }

    public void loadBannerInlineFragment(final Activity mActivity, String id, final View rootView, String inlineStyle, final AdCallback adCallback) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadInlineBannerFragment(mActivity, id, rootView, inlineStyle, adCallback, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                break;
        }
    }

    public void loadBannerInlineFragment(final Activity mActivity, String id, final View rootView, String inlineStyle) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadInlineBannerFragment(mActivity, id, rootView, inlineStyle, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                break;
        }
    }

    public void loadCollapsibleBannerFragment(final Activity mActivity, String id, final View rootView, String gravity, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBannerFragment(mActivity, id, rootView, gravity, adCallback, adConfig.getAdjustTokenTiktok());
    }

    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, MKAdCallback adListener) {
        loadSplashInterstitialAds(context, id, timeOut, timeDelay, true, adListener);
    }

    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, MKAdCallback adListener) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, showSplashIfReady, new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        adListener.onAdClosed();
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adListener.onAdFailedToLoad(new ApAdError(i));

                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new ApAdError(adError));

                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adListener.onAdLoaded();
                    }

                    @Override
                    public void onAdSplashReady() {
                        super.onAdSplashReady();
                        adListener.onAdSplashReady();
                    }


                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (adListener != null) {
                            adListener.onAdClicked();
                        }
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, showSplashIfReady, new AppLovinCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        adListener.onAdFailedToLoad(new ApAdError(i));
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new ApAdError(adError));
                        adListener.onNextAction();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adListener.onAdLoaded();
                    }

                    @Override
                    public void onAdSplashReady() {
                        super.onAdSplashReady();
                        adListener.onAdSplashReady();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (adListener != null) {
                            adListener.onAdClicked();
                        }
                    }
                });
        }
    }


    public void onShowSplash(AppCompatActivity activity, MKAdCallback adListener) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().onShowSplash(activity, new AdCallback() {
                            @Override
                            public void onAdFailedToShow(@Nullable AdError adError) {
                                super.onAdFailedToShow(adError);
                                adListener.onAdFailedToShow(new ApAdError(adError));
                            }

                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                                adListener.onAdClosed();
                            }

                            @Override
                            public void onNextAction() {
                                super.onNextAction();
                                adListener.onNextAction();
                            }


                        }, adConfig.getAdjustTokenTiktok()
                );
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().onShowSplash(activity, new AppLovinCallback() {
                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new ApAdError(adError));
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }

                });

        }
    }

    /**
     * Called  on Resume - SplashActivity
     * It call reshow ad splash when ad splash show fail in background
     *
     * @param activity
     * @param callback
     * @param timeDelay time delay before call show ad splash (ms)
     */
    public void onCheckShowSplashWhenFail(AppCompatActivity activity, MKAdCallback callback,
                                          int timeDelay) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().onCheckShowSplashWhenFail(activity, new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onAdClosed();
                        callback.onNextAction();
                    }


                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new ApAdError(adError));
                    }
                }, timeDelay, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().onCheckShowSplashWhenFail(activity, new AppLovinCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        callback.onNextAction();
                    }


                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new ApAdError(adError));
                    }
                }, timeDelay);
        }
    }

    /**
     * Result a ApInterstitialAd in onInterstitialLoad
     *
     * @param context
     * @param id         admob or max mediation
     * @param adListener
     */
    public ApInterstitialAd getInterstitialAds(Context context, String id, MKAdCallback adListener) {
        ApInterstitialAd apInterstitialAd = new ApInterstitialAd();
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().getInterstitialAds(context, id, new AdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Admob onInterstitialLoad");
                        apInterstitialAd.setInterstitialAd(interstitialAd);
                        adListener.onInterstitialLoad(apInterstitialAd);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        adListener.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        adListener.onAdFailedToShow(new ApAdError(adError));
                    }

                }, adConfig.getAdjustTokenTiktok());
                return apInterstitialAd;

            case MKAdConfig.PROVIDER_MAX:
                MaxInterstitialAd maxInterstitialAd = AppLovin.getInstance().getInterstitialAds(context, id);
                maxInterstitialAd.setListener(new MaxAdListener() {

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, "Max onInterstitialLoad: ");
                        apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                        adListener.onInterstitialLoad(apInterstitialAd);
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {

                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                        adListener.onAdClosed();
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                        adListener.onAdClicked();
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        adListener.onAdFailedToLoad(new ApAdError(error));
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        adListener.onAdFailedToShow(new ApAdError(error));
                    }
                });
                apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                return apInterstitialAd;
            default:
                return apInterstitialAd;
        }
    }

    /**
     * Result a ApInterstitialAd in onInterstitialLoad
     *
     * @param context
     * @param id      admob or max mediation
     */
    public ApInterstitialAd getInterstitialAds(Context context, String id) {
        ApInterstitialAd apInterstitialAd = new ApInterstitialAd();
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().getInterstitialAds(context, id, new AdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Admob onInterstitialLoad: ");
                        apInterstitialAd.setInterstitialAd(interstitialAd);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                    }

                }, adConfig.getAdjustTokenTiktok());
                return apInterstitialAd;

            case MKAdConfig.PROVIDER_MAX:
                MaxInterstitialAd maxInterstitialAd = AppLovin.getInstance().getInterstitialAds(context, id);
                maxInterstitialAd.setListener(new MaxAdListener() {

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, "Max onInterstitialLoad: ");
                        apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {

                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    }
                });
                apInterstitialAd.setMaxInterstitialAd(maxInterstitialAd);
                return apInterstitialAd;
            default:
                return apInterstitialAd;
        }
    }

    /**
     * Called force show ApInterstitialAd when ready
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     */
    public void forceShowInterstitial(Context context, ApInterstitialAd mInterstitialAd,
                                      final MKAdCallback callback) {
        forceShowInterstitial(context, mInterstitialAd, callback, false);
    }

    /**
     * Called force show ApInterstitialAd when ready
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReloadAds auto reload ad when ad close
     */
    public void forceShowInterstitial(@NonNull Context context, ApInterstitialAd mInterstitialAd,
                                      @NonNull final MKAdCallback callback, boolean shouldReloadAds) {
        if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                < MKAd.getInstance().adConfig.getIntervalInterstitialAd() * 1000L
        ) {
            Log.i(TAG, "forceShowInterstitial: ignore by interval impression interstitial time");
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null || mInterstitialAd.isNotReady()) {
            Log.e(TAG, "forceShowInterstitial: ApInterstitialAd is not ready");
            callback.onNextAction();
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                AdCallback adCallback = new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        Log.d(TAG, "onAdClosed: ");
                        callback.onAdClosed();
                        if (shouldReloadAds) {
                            Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    mInterstitialAd.setInterstitialAd(null);
                                    callback.onAdFailedToLoad(new ApAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new ApAdError(adError));
                                }

                            }, adConfig.getAdjustTokenTiktok());
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Log.d(TAG, "onNextAction: ");
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.d(TAG, "onAdFailedToShow: ");
                        callback.onAdFailedToShow(new ApAdError(adError));
                        if (shouldReloadAds) {
                            Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    callback.onAdFailedToLoad(new ApAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new ApAdError(adError));
                                }

                            }, adConfig.getAdjustTokenTiktok());
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        callback.onInterstitialShow();
                    }
                };
                Admob.getInstance().forceShowInterstitial(context, mInterstitialAd.getInterstitialAd(), adCallback);
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().forceShowInterstitial(context, mInterstitialAd.getMaxInterstitialAd(), new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        callback.onAdClosed();
                        callback.onNextAction();
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();

                    }

                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Max inter onAdLoaded:");
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new ApAdError(adError));
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        callback.onInterstitialShow();
                    }
                }, false);
        }
    }

    /**
     * Called force show ApInterstitialAd when reach the number of clicks show ads
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReloadAds auto reload ad when ad close
     */
    public void showInterstitialAdByTimes(Context context, ApInterstitialAd mInterstitialAd,
                                          final MKAdCallback callback, boolean shouldReloadAds) {
        if (mInterstitialAd.isNotReady()) {
            Log.e(TAG, "forceShowInterstitial: ApInterstitialAd is not ready");
            callback.onAdFailedToShow(new ApAdError("ApInterstitialAd is not ready"));
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                AdCallback adCallback = new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        Log.d(TAG, "onAdClosed: ");
                        callback.onAdClosed();
                        if (shouldReloadAds) {
                            Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    mInterstitialAd.setInterstitialAd(null);
                                    callback.onAdFailedToLoad(new ApAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new ApAdError(adError));
                                }

                            }, adConfig.getAdjustTokenTiktok());
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Log.d(TAG, "onNextAction: ");
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        Log.d(TAG, "onAdFailedToShow: ");
                        callback.onAdFailedToShow(new ApAdError(adError));
                        if (shouldReloadAds) {
                            Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                                @Override
                                public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                                    super.onInterstitialLoad(interstitialAd);
                                    Log.d(TAG, "Admob shouldReloadAds success");
                                    mInterstitialAd.setInterstitialAd(interstitialAd);
                                    callback.onInterstitialLoad(mInterstitialAd);
                                }

                                @Override
                                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                                    super.onAdFailedToLoad(i);
                                    callback.onAdFailedToLoad(new ApAdError(i));
                                }

                                @Override
                                public void onAdFailedToShow(@Nullable AdError adError) {
                                    super.onAdFailedToShow(adError);
                                    callback.onAdFailedToShow(new ApAdError(adError));
                                }

                            }, adConfig.getAdjustTokenTiktok());
                        } else {
                            mInterstitialAd.setInterstitialAd(null);
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        if (callback != null) {
                            callback.onInterstitialShow();
                        }
                    }
                };
                Admob.getInstance().showInterstitialAdByTimes(context, mInterstitialAd.getInterstitialAd(), adCallback);
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().showInterstitialAdByTimes(context, mInterstitialAd.getMaxInterstitialAd(), new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        callback.onAdClosed();
                        callback.onNextAction();
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();

                    }

                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        Log.d(TAG, "Max inter onAdLoaded:");
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new ApAdError(adError));
                        if (shouldReloadAds)
                            mInterstitialAd.getMaxInterstitialAd().loadAd();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        if (callback != null) {
                            callback.onInterstitialShow();
                        }
                    }
                }, false);
        }
    }

    /**
     * Load native ad and auto populate ad to view in activity
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     */
    public void loadNativeAd(final Activity activity, String id, int layoutCustomNative) {
        FrameLayout adPlaceHolder = activity.findViewById(R.id.fl_adplaceholder);
        ShimmerFrameLayout containerShimmerLoading = activity.findViewById(R.id.shimmer_container_native);

        if (AppPurchase.getInstance().isPurchased()) {
            if (containerShimmerLoading != null) {
                containerShimmerLoading.stopShimmer();
                containerShimmerLoading.setVisibility(View.GONE);
            }
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                });
                break;
        }
    }

    /**
     * Load native ad and auto populate ad to adPlaceHolder and hide containerShimmerLoading
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param adPlaceHolder
     * @param containerShimmerLoading
     */
    public void loadNativeAd(final Activity activity, String id,
                             int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout
                                     containerShimmerLoading) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "onAdFailedToLoad : NativeAd");
                    }
                });
                break;
        }
    }

    /**
     * Load native ad and auto populate ad to adPlaceHolder and hide containerShimmerLoading
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param adPlaceHolder
     * @param containerShimmerLoading
     */
    public void loadNativeAd(final Activity activity, String id,
                             int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout
                                     containerShimmerLoading, MKAdCallback callback) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
                        populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        callback.onAdImpression();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new ApAdError(adError));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
                        populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
                        callback.onAdImpression();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                });
                break;
        }
    }

    /**
     * Result a ApNativeAd in onUnifiedNativeAdLoaded when native ad loaded
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param callback
     */
    public void loadNativeAdResultCallback(final Activity activity, String id,
                                           int layoutCustomNative, MKAdCallback callback) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(new ApAdError(adError));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().loadNativeAd(activity, id, layoutCustomNative, new AppLovinCallback() {
                    @Override
                    public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                        super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                        callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable MaxError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(new ApAdError(i));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        callback.onAdClicked();
                    }
                });
                break;
        }
    }

    /**
     * Populate Unified Native Ad to View
     *
     * @param activity
     * @param apNativeAd
     * @param adPlaceHolder
     * @param containerShimmerLoading
     */
    public void populateNativeAdView(Activity activity, ApNativeAd apNativeAd, FrameLayout
            adPlaceHolder, ShimmerFrameLayout containerShimmerLoading) {
        if (apNativeAd.getAdmobNativeAd() == null && apNativeAd.getNativeView() == null) {
            containerShimmerLoading.setVisibility(View.GONE);
            Log.e(TAG, "populateNativeAdView failed : native is not loaded ");
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(apNativeAd.getLayoutCustomNative(), null);
                containerShimmerLoading.stopShimmer();
                containerShimmerLoading.setVisibility(View.GONE);
                adPlaceHolder.setVisibility(View.VISIBLE);
                Admob.getInstance().populateUnifiedNativeAdView(apNativeAd.getAdmobNativeAd(), adView);
                adPlaceHolder.removeAllViews();
                adPlaceHolder.addView(adView);
                break;
            case MKAdConfig.PROVIDER_MAX:
                containerShimmerLoading.stopShimmer();
                containerShimmerLoading.setVisibility(View.GONE);
                adPlaceHolder.setVisibility(View.VISIBLE);
                adPlaceHolder.removeAllViews();
                if (apNativeAd.getNativeView().getParent() != null) {
                    ((ViewGroup) apNativeAd.getNativeView().getParent()).removeAllViews();
                }
                adPlaceHolder.addView(apNativeAd.getNativeView());
        }
    }


    public ApRewardAd getRewardAd(Activity activity, String id) {
        ApRewardAd apRewardAd = new ApRewardAd();
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().initRewardAds(activity, id, new AdCallback() {

                    @Override
                    public void onRewardAdLoaded(RewardedAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        Log.i(TAG, "getRewardAd AdLoaded: ");
                        apRewardAd.setAdmobReward(rewardedAd);
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
        }
        return apRewardAd;
    }

    public ApRewardAd getRewardAdInterstitial(Activity activity, String id) {
        ApRewardAd apRewardAd = new ApRewardAd();
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().getRewardInterstitial(activity, id, new AdCallback() {

                    @Override
                    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        Log.i(TAG, "getRewardAdInterstitial AdLoaded: ");
                        apRewardAd.setAdmobReward(rewardedAd);
                    }
                }, adConfig.getAdjustTokenTiktok());
                break;
            case MKAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
        }
        return apRewardAd;
    }

    public ApRewardAd getRewardAd(Activity activity, String id, MKAdCallback callback) {
        ApRewardAd apRewardAd = new ApRewardAd();
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().initRewardAds(activity, id, new AdCallback() {
                    @Override
                    public void onRewardAdLoaded(RewardedAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        apRewardAd.setAdmobReward(rewardedAd);
                        callback.onAdLoaded();
                    }
                }, adConfig.getAdjustTokenTiktok());
                return apRewardAd;
            case MKAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
                return apRewardAd;
        }
        return apRewardAd;
    }

    public ApRewardAd getRewardInterstitialAd(Activity activity, String id, MKAdCallback callback) {
        ApRewardAd apRewardAd = new ApRewardAd();
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                Admob.getInstance().getRewardInterstitial(activity, id, new AdCallback() {
                    @Override
                    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
                        super.onRewardAdLoaded(rewardedAd);
                        apRewardAd.setAdmobReward(rewardedAd);
                        callback.onAdLoaded();
                    }
                }, adConfig.getAdjustTokenTiktok());
                return apRewardAd;
            case MKAdConfig.PROVIDER_MAX:
                MaxRewardedAd maxRewardedAd = AppLovin.getInstance().getRewardAd(activity, id, new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        callback.onAdLoaded();
                    }
                });
                apRewardAd.setMaxReward(maxRewardedAd);
                return apRewardAd;
        }
        return apRewardAd;
    }

    public void forceShowRewardAd(Activity activity, ApRewardAd apRewardAd, MKAdCallback
            callback) {
        if (!apRewardAd.isReady()) {
            Log.e(TAG, "forceShowRewardAd fail: reward ad not ready");
            callback.onNextAction();
            return;
        }
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_ADMOB:
                if (apRewardAd.isRewardInterstitial()) {
                    Admob.getInstance().showRewardInterstitial(activity, apRewardAd.getAdmobRewardInter(), new RewardCallback() {

                        @Override
                        public void onUserEarnedReward(RewardItem var1) {
                            callback.onUserEarnedReward(new ApRewardItem(var1));
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            apRewardAd.clean();
                            callback.onNextAction();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int codeError) {
                            apRewardAd.clean();
                            callback.onAdFailedToShow(new ApAdError(new AdError(codeError, "note msg", "Reward")));
                        }

                        @Override
                        public void onAdClicked() {
                            if (callback != null) {
                                callback.onAdClicked();
                            }
                        }
                    }, adConfig.getAdjustTokenTiktok());
                } else {
                    Admob.getInstance().showRewardAds(activity, apRewardAd.getAdmobReward(), new RewardCallback() {

                        @Override
                        public void onUserEarnedReward(RewardItem var1) {
                            callback.onUserEarnedReward(new ApRewardItem(var1));
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            apRewardAd.clean();
                            callback.onNextAction();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int codeError) {
                            apRewardAd.clean();
                            callback.onAdFailedToShow(new ApAdError(new AdError(codeError, "note msg", "Reward")));
                        }

                        @Override
                        public void onAdClicked() {
                            if (callback != null) {
                                callback.onAdClicked();
                            }
                        }
                    }, adConfig.getAdjustTokenTiktok());
                }
                break;
            case MKAdConfig.PROVIDER_MAX:
                AppLovin.getInstance().showRewardAd(activity, apRewardAd.getMaxReward(), new AppLovinCallback() {
                    @Override
                    public void onUserRewarded(MaxReward reward) {
                        super.onUserRewarded(reward);
                        callback.onUserEarnedReward(new ApRewardItem(reward));
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        apRewardAd.clean();
                        callback.onNextAction();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable MaxError adError) {
                        super.onAdFailedToShow(adError);
                        apRewardAd.clean();
                        callback.onAdFailedToShow(new ApAdError(adError));
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                    }
                });
        }
    }

    /**
     * Result a MKAdAdapter with ad native repeating interval
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param layoutAdPlaceHolder
     * @param originalAdapter
     * @param listener
     * @param repeatingInterval
     * @return
     */
    public MKAdAdapter getNativeRepeatAdapter(Activity activity, String id, int layoutCustomNative, int layoutAdPlaceHolder, RecyclerView.Adapter originalAdapter,
                                              MKAdPlacer.Listener listener, int repeatingInterval) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_MAX:
                MaxAdPlacer.Listener maxListener = new MaxAdPlacer.Listener() {
                    @Override
                    public void onAdLoaded(int i) {
                        listener.onAdLoaded(i);
                        listener.onAdImpression();
                    }

                    @Override
                    public void onAdRemoved(int i) {
                        listener.onAdRemoved(i);
                    }

                    @Override
                    public void onAdClicked(MaxAd maxAd) {
                        MKLogEventManager.logClickAdsEvent(activity, maxAd.getAdUnitId());
                        listener.onAdClicked();
                    }

                    @Override
                    public void onAdRevenuePaid(MaxAd maxAd) {
                        listener.onAdRevenuePaid(new ApAdValue(maxAd));
                    }
                };
                MaxRecyclerAdapter adAdapter = AppLovin.getInstance().getNativeRepeatAdapter(activity, id, layoutCustomNative,
                        originalAdapter, maxListener, repeatingInterval);

                return new MKAdAdapter(adAdapter);
            default:
                return new MKAdAdapter(Admob.getInstance().getNativeRepeatAdapter(activity, id, layoutCustomNative, layoutAdPlaceHolder,
                        originalAdapter, listener, repeatingInterval));
        }

    }

    /**
     * Result a MKAdAdapter with ad native fixed in position
     *
     * @param activity
     * @param id
     * @param layoutCustomNative
     * @param layoutAdPlaceHolder
     * @param originalAdapter
     * @param listener
     * @param position
     * @return
     */
    public MKAdAdapter getNativeFixedPositionAdapter(Activity activity, String id, int layoutCustomNative, int layoutAdPlaceHolder, RecyclerView.Adapter originalAdapter,
                                                     MKAdPlacer.Listener listener, int position) {
        switch (adConfig.getMediationProvider()) {
            case MKAdConfig.PROVIDER_MAX:
                MaxAdPlacer.Listener maxListener = new MaxAdPlacer.Listener() {
                    @Override
                    public void onAdLoaded(int i) {
                        listener.onAdLoaded(i);
                        listener.onAdImpression();
                    }

                    @Override
                    public void onAdRemoved(int i) {
                        listener.onAdRemoved(i);
                    }

                    @Override
                    public void onAdClicked(MaxAd maxAd) {
                        MKLogEventManager.logClickAdsEvent(activity, maxAd.getAdUnitId());
                        listener.onAdClicked();
                    }

                    @Override
                    public void onAdRevenuePaid(MaxAd maxAd) {
                        listener.onAdRevenuePaid(new ApAdValue(maxAd));
                    }
                };
                MaxRecyclerAdapter adAdapter = AppLovin.getInstance().getNativeFixedPositionAdapter(activity, id, layoutCustomNative,
                        originalAdapter, maxListener, position);
                adAdapter.loadAds();
                return new MKAdAdapter(adAdapter);
            default:
                return new MKAdAdapter(Admob.getInstance().getNativeFixedPositionAdapter(activity, id, layoutCustomNative, layoutAdPlaceHolder,
                        originalAdapter, listener, position));
        }
    }
}
