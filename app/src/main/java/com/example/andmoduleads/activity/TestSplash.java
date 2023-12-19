package com.example.andmoduleads.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ads.control.ads.MKAd;
import com.ads.control.funtion.AdCallback;
import com.ads.control.util.AppConstant;
import com.example.andmoduleads.R;

public class TestSplash extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MKAd.getInstance().loadCollapsibleBanner(this, getString(R.string.admod_banner_id), AppConstant.CollapsibleGravity.BOTTOM, new AdCallback());
    }
}
