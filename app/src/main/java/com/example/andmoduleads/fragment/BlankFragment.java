package com.example.andmoduleads.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ads.control.admob.Admob;
import com.ads.control.ads.MKAd;
import com.ads.control.config.MKAdConfig;
import com.ads.control.funtion.AdCallback;
import com.example.andmoduleads.R;
import com.example.andmoduleads.activity.ContentActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.interstitial.InterstitialAd;


public class BlankFragment extends Fragment {
    InterstitialAd mInterstitialAd;
    Button button;
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_blank, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button =    view.findViewById(R.id.btnNextFragment);
        button.setEnabled(false);
        View view1 = view.findViewById(R.id.include).getRootView();
        String idBanner;
        if (MKAd.getInstance().getMediationProvider() == MKAdConfig.PROVIDER_ADMOB) {
            idBanner = getString(R.string.admod_banner_id);
        } else {
            idBanner = getString(R.string.applovin_test_banner);
        }

        MKAd.getInstance().loadBannerFragment(requireActivity(), idBanner, view1, new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.e("TAG", "onAdClicked: BannerFragment");
            }
        });

        button.setOnClickListener(v -> {
            Admob.getInstance().forceShowInterstitial(getActivity(), mInterstitialAd, new AdCallback() {
                @Override
                public void onAdClosed() {
                    ((ContentActivity)getActivity()).showFragment(new InlineBannerFragment(),"BlankFragment2");
                }
            });
        });

        FrameLayout flPlaceHolder = view.findViewById(R.id.fl_adplaceholder);
        ShimmerFrameLayout shimmerFrameLayout = view.findViewById(R.id.shimmer_container_native);
        MKAd.getInstance().loadNativeAd(requireActivity(), getString(R.string.admod_native_id), com.ads.control.R.layout.custom_native_admob_free_size, flPlaceHolder, shimmerFrameLayout, null);
    }
}