package com.maggie.smarthelmet;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ColorMode extends Application {  //TODO: implement night mode
    public static final String TAG = "App";

    private boolean isNightModeEnabled = false;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isNightModeEnabled = mSharedPrefs.getBoolean("NIGHT_MODE", false);

    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setNightMode(boolean isEnabled) {
        this.isNightModeEnabled = isEnabled;
    }
}
