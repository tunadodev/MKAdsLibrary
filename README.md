
# MKAdsLibrary
- Admob
- MAX Mediation(Applovin)
- Google Billing
- AirBridge
- Firebase auto log tracking event, tROAS

# Import Library
Thêm vào project_dir/build.gradle
~~~
allprojects {
    repositories {
	...
    	maven { url 'https://jitpack.io' }
    	maven { url "https://sdk-download.airbridge.io/maven" }
	...
    }
}
~~~
Thêm vào project_dir/app/build.gradle
~~~
    implementation 'com.github.tunadodev:MKAdsLibrary:1.0.0'
    implementation 'com.google.android.play:core:1.10.3'
    implementation 'com.facebook.shimmer:shimmer:0.5.0'
    implementation 'com.google.android.gms:play-services-ads:21.3.0'
    implementation 'androidx.multidex:multidex:2.0.1'
~~~  
# Summary
* [Setup MKAd](#setup_MKad)
    * [Setup id ads](#set_up_ads)
    * [Config ads](#config_ads)
    * [Ads Formats](#ads_formats)

* [Billing App](#billing_app)
* [Ads rule](#ads_rule)
* [FirebaseEvent](#firebase_event)

# <a id="setup_MKad"></a>Setup MKAd
## <a id="set_up_ads"></a>Setup enviroment with id ads for project

Tạo 2 môi trường:
* Config variant test and release in gradle
* appDev: chỉ dụng test id để chạy khi dev
* appProd: để build release

~~~    
    flavorDimensions "adIds"
    productFlavors {
        appDev {
            //use id test when dev
            manifestPlaceholders = [ ad_app_id:"ca-app-pub-3940256099942544~3347511713"]
            buildConfigField "String", "ad_interstitial_splash", "\"ca-app-pub-3940256099942544/1033173712\""
            buildConfigField "String", "ad_banner", "\"ca-app-pub-3940256099942544/6300978111\""
            buildConfigField "String", "ad_reward", "\"ca-app-pub-3940256099942544/5224354917\""
            buildConfigField "String", "ad_reward_inter", "\"ca-app-pub-3940256099942544/5354046379\""
            buildConfigField "String", "ad_appopen_resume", "\"ca-app-pub-3940256099942544/3419835294\""
            buildConfigField "String", "ad_native", "\"ca-app-pub-3940256099942544/2247696110\""
            buildConfigField "String", "ads_open_app", "\"ca-app-pub-3940256099942544/3419835294\""
            buildConfigField "Boolean", "env_dev", "true"

        }
        appProd {
            //add your id ad here
            manifestPlaceholders = [ ad_app_id:"ca-app-pub-3940256099942544~3347511713"]
            buildConfigField "String", "ad_interstitial_splash", "\"ca-app-pub-3940256099942544/1033173712\""
            buildConfigField "String", "ad_banner", "\"ca-app-pub-3940256099942544/6300978111\""
            buildConfigField "String", "ad_reward", "\"ca-app-pub-3940256099942544/5224354917\""
            buildConfigField "String", "ad_reward_inter", "\"ca-app-pub-3940256099942544/5354046379\""
            buildConfigField "String", "ad_appopen_resume", "\"ca-app-pub-3940256099942544/3419835294\""
            buildConfigField "String", "ad_native", "\"ca-app-pub-3940256099942544/2247696110\""
            buildConfigField "String", "ad_native", "\"ca-app-pub-3940256099942544/3419835294\""
            buildConfigField "String", "ads_open_app", "\"ca-app-pub-3940256099942544/3419835294\""
            buildConfigField "Boolean", "env_dev", "false"
        }
    }
~~~
Add element to AndroidManifest.xml
~~~
<meta-data
android:name="com.google.android.gms.ads.APPLICATION_ID"
android:value="${ad_app_id}" />
~~~

~~~
<application>
...
tools:replace="android:fullBackupContent"
...
</application>
~~~

## <a id="config_ads"></a>Config ads
Tạo class Application

Configure your mediation here. using PROVIDER_ADMOB or PROVIDER_MAX
Configure your app name, token for AirBridge config

*** Note:Cannot use id ad test for production enviroment 
~~~
public class App extends AdsMultiDexApplication(){
    @Override
    public void onCreate() {
        super.onCreate();
    ...
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
}
~~~
AndroidManifest.xml
~~~
<application
android:name=".App"
...
>
~~~

## <a id="ads_formats"></a>Ads formats
SplashActivity
### Ad Splash Interstitial
~~~ 
    MKAdCallback adCallback = new MKAdCallback() {
        @Override
        public void onNextAction() {
            super.onNextAction();
            Log.d(TAG, "onNextAction");
            startMain();
        }
    };
~~~
~~~
        MKAd.getInstance().setInitCallback(new MKInitCallback() {
            @Override
            public void initAdSuccess() {
                MKAd.getInstance().loadSplashInterstitialAds(SplashActivity.this, idAdSplash, TIME_OUT, TIME_DELAY_SHOW_AD, true, adCallback);
            }
        });
~~~
SplashActivity
### Ad Splash App Open High and Interstitial
~~~ 
    AppOpenManager.getInstance().loadSplashOpenAndInter(SplashActivity.class,SplashActivity.this, BuildConfig.open_lunch_high,BuildConfig.inter_splash,25000, new AdCallback(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                
                // startMain();
            
            }
        });

~~~ 

### Interstitial
Load ad interstital before show 
Check null when Load Inter
~~~
  private fun loadInterCreate() {
    ApInterstitialAd mInterstitialAd = MKAd.getInstance().getInterstitialAds(this, idInter);
  }
~~~
Show and auto release ad interstitial
~~~
         if (mInterstitialAd.isReady()) {
                MKAd.getInstance().forceShowInterstitial(this, mInterstitialAd, new MKAdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                Log.d(TAG, "onNextAction");
               startActivity(new Intent(MainActivity.this, MaxSimpleListActivity.class));
            }
                
                }, true);
            } else {
                loadAdInterstitial();
            }
~~~
### Ad Banner

#### Latest way:
~~~
    <com.ads.control.ads.bannerAds.MKBannerAdView
        android:id="@+id/bannerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        app:layout_constraintBottom_toBottomOf="parent" />
~~~
call load ad banner
~~~
    bannerAdView.loadBanner(this, idBanner);
~~~
#### The older way:
~~~
  <include
  android:id="@+id/include"
  layout="@layout/layout_banner_control"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:layout_alignParentBottom="true"
  app:layout_constraintBottom_toBottomOf="parent" />
~~~
call load ad banner
~~~
  MKAd.getInstance().loadBanner(this, idBanner);
~~~

### Ad Native
Load ad native before show
~~~
        MKAd.getInstance().loadNativeAdResultCallback(this,ID_NATIVE_AD, com.ads.control.R.layout.custom_native_max_small,new MKAdCallback(){
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
               //save or show native 
            }
        });
~~~
Populate native ad to view
~~~
    MKAd.getInstance().populateNativeAdView(MainApplovinActivity.this,nativeAd,flParentNative,shimmerFrameLayout);
~~~
auto load and show native contains loading

in layout XML
~~~
      <com.ads.control.ads.nativeAds.MKNativeAdView
        android:id="@+id/MKNativeAds"
        android:layout_width="match_parent"
        android:layout_height="@dimen/_150sdp"
        android:background="@drawable/bg_card_ads"
        app:layoutCustomNativeAd="@layout/custom_native_admod_medium_rate"
        app:layoutLoading="@layout/loading_native_medium"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
~~~
Call load native ad
~~~
 MKNativeAdView.loadNativeAd(this, idNative);
~~~
Load Ad native for recyclerView
~~~~
    // ad native repeating interval
    MKAdAdapter     adAdapter = MKAd.getInstance().getNativeRepeatAdapter(this, idNative, layoutCustomNative, com.ads.control.R.layout.layout_native_medium,
                originalAdapter, listener, 4);
    
    // ad native fixed in position
        MKAdAdapter   adAdapter = MKAd.getInstance().getNativeFixedPositionAdapter(this, idNative, layoutCustomNative, com.ads.control.R.layout.layout_native_medium,
                originalAdapter, listener, 4);
    
        recyclerView.setAdapter(adAdapter.getAdapter());
        adAdapter.loadAds();
~~~~
### Ad Reward
Get and show reward
~~~
  ApRewardAd rewardAd = MKAd.getInstance().getRewardAd(this, idAdReward);

   if (rewardAd != null && rewardAd.isReady()) {
                MKAd.getInstance().forceShowRewardAd(this, rewardAd, new MKAdCallback());
            }
});
~~~
### Ad resume
App
~~~ 
  override fun onCreate() {
    super.onCreate()
    AppOpenManager.getInstance().enableAppResume()
    MKAdConfig.setIdAdResume(AppOpenManager.AD_UNIT_ID_TEST);
    ...
  }
    

~~~


# <a id="billing_app"></a>Billing app
## Init Billing
Application
~~~
    @Override
    public void onCreate() {
        super.onCreate();
        AppPurchase.getInstance().initBilling(this,listINAPId,listSubsId);
    }
~~~
## Check status billing init
~~~
 if (AppPurchase.getInstance().getInitBillingFinish()){
            loadAdsPlash();
        }else {
            AppPurchase.getInstance().setBillingListener(new BillingListener() {
                @Override
                public void onInitBillingListener(int code) {
                         loadAdsPlash();
                }
            },7000);
        }
~~~
## Check purchase status
    //check purchase with PRODUCT_ID
     AppPurchase.getInstance().isPurchased(this,PRODUCT_ID);
     //check purchase all
     AppPurchase.getInstance().isPurchased(this);
##  Purchase
     AppPurchase.getInstance().purchase(this,PRODUCT_ID);
     AppPurchase.getInstance().subscribe(this,SUBS_ID);
## Purchase Listener
             AppPurchase.getInstance().setPurchaseListioner(new PurchaseListioner() {
                 @Override
                 public void onProductPurchased(String productId,String transactionDetails) {

                 }

                 @Override
                 public void displayErrorMessage(String errorMsg) {

                 }
             });

## Get id purchased
      AppPurchase.getInstance().getIdPurchased();
## Consume purchase
      AppPurchase.getInstance().consumePurchase(PRODUCT_ID);
## Get price
      AppPurchase.getInstance().getPrice(PRODUCT_ID)
      AppPurchase.getInstance().getPriceSub(SUBS_ID)
### Show iap dialog
    InAppDialog dialog = new InAppDialog(this);
    dialog.setCallback(() -> {
         AppPurchase.getInstance().purchase(this,PRODUCT_ID);
        dialog.dismiss();
    });
    dialog.show();



# <a id="ads_rule"></a>Ads rule
## Always add device test to idTestList with all of your team's device
To ignore invalid ads traffic
https://support.google.com/adsense/answer/16737?hl=en
## Before show full-screen ad (interstitial, app open ad), alway show a short loading dialog
To ignore accident click from user. This feature is existed in library
## Never reload ad on onAdFailedToLoad
To ignore infinite loop

# <a id="firebase_event"></a>Firebase event
## Tracking custom event

~~~
Bundle bundle = new Bundle();
bundle.putString("key", "value");
FirebaseAnalyticsUtil.logCustomEvent("test", this.getApplicationContext(), bundle);
~~~