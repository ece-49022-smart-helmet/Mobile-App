package com.maggie.smarthelmet;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;


public class PrivacyPolicyActivity extends Activity {
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/privacy_policy.html");
    }

}
