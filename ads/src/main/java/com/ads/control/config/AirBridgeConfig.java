package com.ads.control.config;
/**
 * Created by tunado
 */
public class AirBridgeConfig {
    private boolean enableAirBridge;
    private String appNameAirBridge;
    private String tokenAirBridge;
    private String eventAdClick;
    private String eventAdImpression;

    public boolean isEnableAirBridge() {
        return enableAirBridge;
    }

    public void setEnableAirBridge(boolean enableAirBridge) {
        this.enableAirBridge = enableAirBridge;
    }

    public String getTokenAirBridge() {
        return tokenAirBridge;
    }

    public void setTokenAirBridge(String tokenAirBridge) {
        this.tokenAirBridge = tokenAirBridge;
    }

    public String getEventAdImpression() {
        return eventAdImpression;
    }

    public void setEventAdImpression(String eventAdImpression) {
        this.eventAdImpression = eventAdImpression;
    }

    public String getEventAdClick() {
        return eventAdClick;
    }

    public void setEventAdClick(String eventAdClick) {
        this.eventAdClick = eventAdClick;
    }

    public String getAppNameAirBridge() {
        return appNameAirBridge;
    }

    public void setAppNameAirBridge(String appNameAirBridge) {
        this.appNameAirBridge = appNameAirBridge;
    }
}
