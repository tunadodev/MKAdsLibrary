package com.ads.control.ads.nativeAds;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.control.R;
import com.ads.control.ads.MKAd;
import com.ads.control.ads.MKAdCallback;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.facebook.shimmer.ShimmerFrameLayout;

public class MKLargeNativeAdView extends RelativeLayout {

    private int layoutCustomNativeAd = 0;
    private ShimmerFrameLayout layoutLoading;
    private FrameLayout layoutPlaceHolder;
    private String TAG = "MKLargeNativeAdView";

    public MKLargeNativeAdView(@NonNull Context context) {
        super(context);
        init();
    }

    public MKLargeNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MKLargeNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    public MKLargeNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MKNativeAdView, 0, 0);
        // Get layout native view custom and  layout loading
        //layoutCustomNativeAd = typedArray.getResourceId(R.styleable.MKNativeAdView_layoutCustomNativeAd, 0);
        int idLayoutLoading = typedArray.getResourceId(R.styleable.MKNativeAdView_layoutLoading, 0);
        if (idLayoutLoading != 0)
            layoutLoading = (ShimmerFrameLayout) LayoutInflater.from(getContext()).inflate(idLayoutLoading, null);

        init();
    }

    private void init() {
        layoutPlaceHolder = new FrameLayout(getContext());
        addView(layoutPlaceHolder);
        if (layoutLoading != null)
            addView(layoutLoading);

    }

    public void setLayoutCustomNativeAd(int layoutCustomNativeAd) {
        this.layoutCustomNativeAd = layoutCustomNativeAd;
    }

    public void setLayoutLoading(int idLayoutLoading) {
        this.layoutLoading = (ShimmerFrameLayout) LayoutInflater.from(getContext()).inflate(idLayoutLoading, null);
        addView(layoutLoading);
    }

    public void populateNativeAdView(Activity activity, ApNativeAd nativeAd) {
        if (layoutLoading == null) {
            Log.e(TAG, "populateNativeAdView error : layoutLoading not set");
            return;
        }
        MKAd.getInstance().populateNativeAdView(activity, nativeAd, layoutPlaceHolder, layoutLoading);
    }

    public void loadNativeAd(Activity activity, String idAd) {
        loadNativeAd(activity, idAd, new MKAdCallback() {
        });
    }

    public void loadNativeAd(Activity activity, String idAd, MKAdCallback MKAdCallback) {
        if (layoutLoading == null) {
            setLayoutLoading(R.layout.layout_native_control);
        }
        if (layoutCustomNativeAd == 0) {
            layoutCustomNativeAd = R.layout.custom_native_admob_large;
            setLayoutCustomNativeAd(layoutCustomNativeAd);
        }
        MKAd.getInstance().loadNativeAd(activity, idAd, layoutCustomNativeAd, layoutPlaceHolder, layoutLoading, MKAdCallback);
    }

    public void loadNativeAd(Activity activity, String idAd, int layoutCustomNativeAd, int idLayoutLoading) {
        setLayoutLoading(idLayoutLoading);
        setLayoutCustomNativeAd(layoutCustomNativeAd);
        loadNativeAd(activity, idAd);
    }

    public void loadNativeAd(Activity activity, String idAd, int layoutCustomNativeAd, int idLayoutLoading, MKAdCallback MKAdCallback) {
        setLayoutLoading(idLayoutLoading);
        setLayoutCustomNativeAd(layoutCustomNativeAd);
        loadNativeAd(activity, idAd, MKAdCallback);
    }
}
